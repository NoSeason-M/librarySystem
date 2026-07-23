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

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cancelExpiredReservations() {
        log.info("[ScheduledTask] cancelExpiredReservations");
        if ("0".equals(getConfig("scheduled.reservation.auto-cancel"))) return;

        List<Reservation> waitList = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>().eq(Reservation::getStatus, "waiting")
                        .lt(Reservation::getExpireDate, LocalDateTime.now()));
        for (Reservation r : waitList) {
            r.setStatus("cancelled");
            reservationMapper.updateById(r);
            notify(r.getReaderId(), "Reservation Cancelled", "Auto-cancelled due to expiration.", "cancel", r.getId());
        }

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
            notify(r.getReaderId(), "Reservation Expired", "Did not pick up in time.", "cancel", r.getId());
        }
        if (!waitList.isEmpty() || !readyList.isEmpty())
            log.info("Processed {} waiting + {} ready reservations", waitList.size(), readyList.size());
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void generateOverdueFines() {
        log.info("[ScheduledTask] generateOverdueFines");
        if ("0".equals(getConfig("scheduled.fine.auto-generate"))) return;

        LocalDate today = LocalDate.now();
        List<BorrowRecord> list = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "borrowed")
                        .lt(BorrowRecord::getDueDate, today));
        for (BorrowRecord rec : list) {
            if (fineRecordMapper.selectCount(new LambdaQueryWrapper<FineRecord>()
                    .eq(FineRecord::getBorrowRecordId, rec.getId()).eq(FineRecord::getFineType, "overdue")) > 0) continue;
            long days = ChronoUnit.DAYS.between(rec.getDueDate(), today);
            if (days <= 0) continue;
            BigDecimal rate = BigDecimal.valueOf(0.50);
            Reader r = readerMapper.selectById(rec.getReaderId());
            if (r != null && r.getReaderTypeId() != null) {
                ReaderType rt = readerTypeMapper.selectById(r.getReaderTypeId());
                if (rt != null && rt.getOverdueFineRate() != null) rate = rt.getOverdueFineRate();
            }
            BigDecimal amount = rate.multiply(BigDecimal.valueOf(days));
            FineRecord f = new FineRecord();
            f.setReaderId(rec.getReaderId()); f.setBorrowRecordId(rec.getId());
            f.setFineType("overdue"); f.setAmount(amount); f.setPaid(0); f.setWaive(0);
            fineRecordMapper.insert(f);
            if (r != null) {
                r.setTotalFines(r.getTotalFines() != null ? r.getTotalFines().add(amount) : amount);
                readerMapper.updateById(r);
            }
            notify(rec.getReaderId(), "Overdue Fine", "Fine: " + amount + " yuan.", "fine", f.getId());
        }
        if (!list.isEmpty()) log.info("Generated fines for {} records", list.size());
    }

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void sendOverdueReminders() {
        log.info("[ScheduledTask] sendOverdueReminders");
        if ("0".equals(getConfig("scheduled.notify.overdue-check"))) return;

        LocalDate today = LocalDate.now();
        int adv = 3;
        try { adv = Integer.parseInt(getConfig("notify.overdue_advance_days")); } catch (Exception e) {}

        for (BorrowRecord rec : borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, "borrowed").lt(BorrowRecord::getDueDate, today))) {
            if (notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getReaderId, rec.getReaderId()).eq(Notification::getType, "overdue_due")
                    .eq(Notification::getRelatedId, rec.getId()).ge(Notification::getCreateTime, today.atStartOfDay())) == 0)
                notify(rec.getReaderId(), "Overdue Reminder", "Book is overdue.", "overdue_due", rec.getId());
        }
        for (BorrowRecord rec : borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getStatus, "borrowed").eq(BorrowRecord::getDueDate, today.plusDays(adv)))) {
            if (notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getReaderId, rec.getReaderId()).eq(Notification::getType, "due_soon")
                    .eq(Notification::getRelatedId, rec.getId()).ge(Notification::getCreateTime, today.atStartOfDay())) == 0)
                notify(rec.getReaderId(), "Due Reminder", "Due in " + adv + " days.", "due_soon", rec.getId());
        }
    }

    private void notify(Long rid, String t, String c, String type, Long relId) {
        Notification n = new Notification();
        n.setReaderId(rid); n.setTitle(t); n.setContent(c);
        n.setType(type); n.setRelatedId(relId); n.setReadFlag(0);
        notificationMapper.insert(n);
    }

    private String getConfig(String key) {
        SysConfig c = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return c != null ? c.getConfigValue() : "1";
    }
}
