package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final ReservationMapper reservationMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final FineRecordMapper fineRecordMapper;
    private final NotificationMapper notificationMapper;
    private final BookCopyMapper bookCopyMapper;
    private final BookInfoMapper bookInfoMapper;
    private final ReaderMapper readerMapper;
    private final ReaderTypeMapper readerTypeMapper;
    private final SysConfigMapper configMapper;

    // ==================== 1. 预约超时自动取消 ====================

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cancelExpiredReservations() {
        log.info("[定时任务] 预约超时自动取消 - 开始");
        if ("0".equals(getConfig("scheduled.reservation.auto-cancel"))) return;

        // 等待中的预约超过保留时间 → 自动取消
        List<Reservation> waitList = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>().eq(Reservation::getStatus, "waiting")
                        .lt(Reservation::getExpireDate, LocalDateTime.now()));
        for (Reservation r : waitList) {
            r.setStatus("cancelled");
            reservationMapper.updateById(r);
            notify(r.getReaderId(), "预约已取消", "您预约的图书超时未处理，预约已自动取消。", "cancel", r.getId());
        }

        // 待取书的预约超过保留时间 → 自动过期，释放副本
        List<Reservation> readyList = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>().eq(Reservation::getStatus, "ready")
                        .lt(Reservation::getExpireDate, LocalDateTime.now()));
        for (Reservation r : readyList) {
            r.setStatus("expired");
            reservationMapper.updateById(r);
            if (r.getBookCopyId() != null) {
                BookCopy copy = bookCopyMapper.selectById(r.getBookCopyId());
                if (copy != null) {
                    copy.setStatus("in");
                    bookCopyMapper.updateById(copy);
                    BookInfo bi = bookInfoMapper.selectById(r.getBookInfoId());
                    if (bi != null) {
                        bi.setAvailableCopies(bi.getAvailableCopies() + 1);
                        bookInfoMapper.updateById(bi);
                    }
                }
            }
            notify(r.getReaderId(), "预约已过期", "您预约的图书已超过取书时限，预约已过期，图书已释放。", "cancel", r.getId());
        }
        if (!waitList.isEmpty() || !readyList.isEmpty())
            log.info("处理完成: {} 个等待中 + {} 个待取书预约", waitList.size(), readyList.size());
    }

    // ==================== 2. 逾期自动生成罚款 ====================

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void generateOverdueFines() {
        log.info("[定时任务] 逾期自动生成罚款 - 开始");
        if ("0".equals(getConfig("scheduled.fine.auto-generate"))) return;

        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdueList = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "borrowed")
                        .lt(BorrowRecord::getDueDate, today));

        for (BorrowRecord rec : overdueList) {
            long days = ChronoUnit.DAYS.between(rec.getDueDate(), today);
            if (days <= 0) continue;

            // 获取读者类型的逾期费率
            BigDecimal rate = BigDecimal.valueOf(0.50);
            Reader r = readerMapper.selectById(rec.getReaderId());
            if (r != null && r.getReaderTypeId() != null) {
                ReaderType rt = readerTypeMapper.selectById(r.getReaderTypeId());
                if (rt != null && rt.getOverdueFineRate() != null) rate = rt.getOverdueFineRate();
            }

            // 教师免逾期费
            if (rate.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal amount = rate.multiply(BigDecimal.valueOf(days));

            // 查找是否已有该借阅记录的逾期罚款
            List<FineRecord> existing = fineRecordMapper.selectList(
                    new LambdaQueryWrapper<FineRecord>()
                            .eq(FineRecord::getBorrowRecordId, rec.getId())
                            .eq(FineRecord::getFineType, "overdue"));

            if (existing.isEmpty()) {
                // 首次生成
                FineRecord f = new FineRecord();
                f.setReaderId(rec.getReaderId());
                f.setBorrowRecordId(rec.getId());
                f.setFineType("overdue");
                f.setAmount(amount);
                f.setPaid(0);
                f.setWaive(0);
                fineRecordMapper.insert(f);
                if (r != null) {
                    r.setTotalFines(r.getTotalFines() != null ? r.getTotalFines().add(amount) : amount);
                    readerMapper.updateById(r);
                }
                notify(rec.getReaderId(), "逾期罚款通知", "图书已逾期" + days + "天，产生罚款 ¥" + amount + "，请尽快归还。", "fine", f.getId());
            } else {
                // 已有罚款记录，更新金额（每天递增）
                for (FineRecord f : existing) {
                    if (f.getPaid() == 0 && f.getWaive() == 0) {
                        BigDecimal oldAmount = f.getAmount() != null ? f.getAmount() : BigDecimal.ZERO;
                        f.setAmount(amount);
                        fineRecordMapper.updateById(f);
                        // 更新读者总罚款（差额）
                        if (r != null) {
                            BigDecimal diff = amount.subtract(oldAmount);
                            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                                r.setTotalFines(r.getTotalFines() != null ? r.getTotalFines().add(diff) : diff);
                                readerMapper.updateById(r);
                            }
                        }
                    }
                }
            }
        }
        if (!overdueList.isEmpty())
            log.info("处理完成: {} 条逾期记录", overdueList.size());
    }

    // ==================== 3. 每日逾期提醒通知 ====================

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void sendOverdueReminders() {
        log.info("[定时任务] 逾期提醒 - 开始");
        if ("0".equals(getConfig("scheduled.notify.overdue-check"))) return;

        LocalDate today = LocalDate.now();
        int adv = 3;
        try { adv = Integer.parseInt(getConfig("notify.overdue_advance_days")); } catch (Exception ignored) {}

        // 逾期提醒：每天发送一次
        for (BorrowRecord rec : borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, "borrowed").lt(BorrowRecord::getDueDate, today))) {
            long overdueDays = ChronoUnit.DAYS.between(rec.getDueDate(), today);
            if (notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getReaderId, rec.getReaderId())
                    .eq(Notification::getType, "overdue_due")
                    .eq(Notification::getRelatedId, rec.getId())
                    .ge(Notification::getCreateTime, today.atStartOfDay())) == 0) {
                notify(rec.getReaderId(), "逾期提醒",
                        "您借阅的图书已逾期 " + overdueDays + " 天，请尽快归还以避免产生更多罚款。",
                        "overdue_due", rec.getId());
            }
        }

        // 即将到期提醒
        for (BorrowRecord rec : borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, "borrowed")
                .eq(BorrowRecord::getDueDate, today.plusDays(adv)))) {
            if (notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getReaderId, rec.getReaderId())
                    .eq(Notification::getType, "due_soon")
                    .eq(Notification::getRelatedId, rec.getId())
                    .ge(Notification::getCreateTime, today.atStartOfDay())) == 0) {
                notify(rec.getReaderId(), "即将到期提醒",
                        "您借阅的图书将于 " + adv + " 天后到期，请及时归还或续借。",
                        "due_soon", rec.getId());
            }
        }
    }

    private void notify(Long readerId, String title, String content, String type, Long relatedId) {
        Notification n = new Notification();
        n.setReaderId(readerId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setRelatedId(relatedId);
        n.setReadFlag(0);
        notificationMapper.insert(n);
    }

    private String getConfig(String key) {
        SysConfig c = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return c != null ? c.getConfigValue() : "1";
    }
}
