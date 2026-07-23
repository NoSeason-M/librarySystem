package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.librarysystem.common.exception.BusinessException;
import com.library.librarysystem.dto.BookImportRow;
import com.library.librarysystem.dto.BookExportRow;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookInfoMapper bookInfoMapper;
    private final BookCopyMapper bookCopyMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;
    private final PublisherMapper publisherMapper;

    @Value("${app.upload-dir:${user.dir}/uploads}")
    private String uploadDir;

    // ==================== Admin CRUD ====================

    public Map<String, Object> listAdminBooks(String keyword, int page, int size) {
        LambdaQueryWrapper<BookInfo> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(BookInfo::getTitle, keyword)
                    .or().like(BookInfo::getAuthor, keyword)
                    .or().like(BookInfo::getIsbn, keyword));
        }
        qw.orderByDesc(BookInfo::getCreateTime);
        IPage<BookInfo> p = bookInfoMapper.selectPage(new Page<>(page, size), qw);
        List<Map<String, Object>> records = p.getRecords().stream().map(this::toAdminItem).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        return result;
    }

    @Transactional
    public Long createBook(Map<String, Object> req) {
        BookInfo book = new BookInfo();
        book.setIsbn((String) req.get("isbn"));
        book.setTitle((String) req.get("title"));
        book.setAuthor((String) req.get("author"));
        book.setPublisherId(req.get("publisherId") != null ? ((Number) req.get("publisherId")).longValue() : null);
        book.setCategoryId(req.get("categoryId") != null ? ((Number) req.get("categoryId")).longValue() : null);
        book.setPublishDate(req.get("publishDate") != null ? LocalDate.parse((String) req.get("publishDate")) : null);
        book.setPrice(req.get("price") != null ? java.math.BigDecimal.valueOf(((Number) req.get("price")).doubleValue()) : null);
        book.setPages(req.get("pages") != null ? ((Number) req.get("pages")).intValue() : null);
        book.setBinding((String) req.get("binding"));
        book.setLanguage((String) req.get("language"));
        book.setSummary((String) req.get("summary"));
        book.setTotalCopies(0);
        book.setAvailableCopies(0);
        book.setBorrowCount(0);
        book.setStatus(1);
        bookInfoMapper.insert(book);
        return book.getId();
    }

    @Transactional
    public void updateBook(Long id, Map<String, Object> req) {
        BookInfo book = bookInfoMapper.selectById(id);
        if (book == null) throw new BusinessException("Book not found");
        if (req.containsKey("title")) book.setTitle((String) req.get("title"));
        if (req.containsKey("author")) book.setAuthor((String) req.get("author"));
        if (req.containsKey("isbn")) book.setIsbn((String) req.get("isbn"));
        if (req.containsKey("publisherId")) book.setPublisherId(req.get("publisherId") != null ? ((Number) req.get("publisherId")).longValue() : null);
        if (req.containsKey("categoryId")) book.setCategoryId(req.get("categoryId") != null ? ((Number) req.get("categoryId")).longValue() : null);
        if (req.containsKey("publishDate")) book.setPublishDate(req.get("publishDate") != null ? LocalDate.parse((String) req.get("publishDate")) : null);
        if (req.containsKey("price")) book.setPrice(req.get("price") != null ? java.math.BigDecimal.valueOf(((Number) req.get("price")).doubleValue()) : null);
        if (req.containsKey("pages")) book.setPages(req.get("pages") != null ? ((Number) req.get("pages")).intValue() : null);
        if (req.containsKey("binding")) book.setBinding((String) req.get("binding"));
        if (req.containsKey("language")) book.setLanguage((String) req.get("language"));
        if (req.containsKey("summary")) book.setSummary((String) req.get("summary"));
        bookInfoMapper.updateById(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        BookInfo book = bookInfoMapper.selectById(id);
        if (book == null) throw new BusinessException("Book not found");
        bookInfoMapper.deleteById(id);
        bookCopyMapper.delete(new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, id));
    }

    // ==================== Cover Upload ====================

    public String uploadCover(Long bookId, MultipartFile file) throws IOException {
        BookInfo book = bookInfoMapper.selectById(bookId);
        if (book == null) throw new BusinessException("Book not found");

        if (file.isEmpty()) throw new BusinessException("File is empty");
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new BusinessException("Only JPG, PNG, WEBP images are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("File size must be less than 5MB");
        }

        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = bookId + "_" + System.currentTimeMillis() + ext;
        String relativePath = "/uploads/covers/" + datePath + "/" + fileName;

        Path fullPath = Paths.get(uploadDir, "covers", datePath, fileName);
        Files.createDirectories(fullPath.getParent());
        Files.write(fullPath, file.getBytes());

        book.setCoverUrl(relativePath);
        bookInfoMapper.updateById(book);

        return relativePath;
    }

    // ==================== Import / Export ====================

    public Map<String, Object> importBooks(MultipartFile file) throws IOException {
        List<BookImportRow> rows = EasyExcel.read(file.getInputStream()).head(BookImportRow.class).sheet().doReadSync();
        int success = 0, fail = 0;
        List<Map<String, Object>> failDetails = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            try { importSingleBook(rows.get(i)); success++; }
            catch (Exception e) {
                fail++; Map<String, Object> d = new HashMap<>();
                d.put("row", i + 2); d.put("reason", e.getMessage()); failDetails.add(d);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", rows.size()); result.put("success", success);
        result.put("fail", fail); result.put("failDetails", failDetails);
        return result;
    }

    @Transactional
    public void importSingleBook(BookImportRow row) {
        if (row.getTitle() == null || row.getTitle().isEmpty()) throw new BusinessException("Title required");
        if (row.getAuthor() == null || row.getAuthor().isEmpty()) throw new BusinessException("Author required");
        BookInfo book = new BookInfo();
        book.setIsbn(row.getIsbn()); book.setTitle(row.getTitle()); book.setAuthor(row.getAuthor());
        if (row.getPublisher() != null && !row.getPublisher().isEmpty()) {
            List<Publisher> pubs = publisherMapper.selectList(
                    new LambdaQueryWrapper<Publisher>().eq(Publisher::getName, row.getPublisher()));
            if (!pubs.isEmpty()) book.setPublisherId(pubs.get(0).getId());
        }
        if (row.getCategory() != null && !row.getCategory().isEmpty()) {
            List<Category> cats = categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>().eq(Category::getName, row.getCategory()));
            if (!cats.isEmpty()) book.setCategoryId(cats.get(0).getId());
        }
        if (row.getPublishDate() != null && !row.getPublishDate().isEmpty()) {
            try { book.setPublishDate(LocalDate.parse(row.getPublishDate())); } catch (Exception ignored) {}
        }
        if (row.getPrice() != null && !row.getPrice().isEmpty()) {
            try { book.setPrice(new java.math.BigDecimal(row.getPrice())); } catch (Exception ignored) {}
        }
        book.setBinding(row.getBinding()); book.setLanguage(row.getLanguage() != null ? row.getLanguage() : "中文");
        book.setSummary(row.getSummary()); book.setTotalCopies(0); book.setAvailableCopies(0); book.setBorrowCount(0); book.setStatus(1);
        bookInfoMapper.insert(book);
    }

    public void exportBooks(HttpServletResponse response, String keyword) throws IOException {
        LambdaQueryWrapper<BookInfo> qw = new LambdaQueryWrapper<BookInfo>().eq(BookInfo::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(BookInfo::getTitle, keyword).or().like(BookInfo::getAuthor, keyword).or().like(BookInfo::getIsbn, keyword));
        }
        qw.orderByDesc(BookInfo::getBorrowCount);
        List<BookInfo> books = bookInfoMapper.selectList(qw);
        List<BookExportRow> exportData = books.stream().map(b -> {
            BookExportRow r = new BookExportRow();
            r.setTitle(b.getTitle()); r.setAuthor(b.getAuthor()); r.setIsbn(b.getIsbn());
            if (b.getPublisherId() != null) {
                Publisher p = publisherMapper.selectById(b.getPublisherId());
                r.setPublisher(p != null ? p.getName() : "");
            }
            if (b.getCategoryId() != null) {
                Category c = categoryMapper.selectById(b.getCategoryId());
                r.setCategory(c != null ? c.getName() : "");
            }
            long total = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, b.getId()));
            long avail = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, b.getId()).eq(BookCopy::getStatus, "in"));
            r.setTotalCopies((int) total); r.setAvailableCopies((int) avail);
            r.setBorrowCount(b.getBorrowCount() != null ? b.getBorrowCount() : 0);
            r.setPrice(b.getPrice() != null ? b.getPrice().doubleValue() : 0);
            r.setLanguage(b.getLanguage()); r.setBinding(b.getBinding()); r.setPages(b.getPages());
            return r;
        }).collect(Collectors.toList());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=books_export.xlsx");
        EasyExcel.write(response.getOutputStream(), BookExportRow.class).sheet("Books").doWrite(exportData);
    }

    // ==================== Public ====================

    public Map<String, Object> searchBooks(String keyword, int page, int size) {
        LambdaQueryWrapper<BookInfo> qw = new LambdaQueryWrapper<BookInfo>().eq(BookInfo::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(BookInfo::getTitle, keyword).or().like(BookInfo::getAuthor, keyword).or().like(BookInfo::getIsbn, keyword));
        }
        qw.orderByDesc(BookInfo::getBorrowCount);
        IPage<BookInfo> p = bookInfoMapper.selectPage(new Page<>(page, size), qw);
        List<Map<String, Object>> records = p.getRecords().stream().map(this::toBookItem).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records); result.put("total", p.getTotal());
        result.put("size", p.getSize()); result.put("current", p.getCurrent()); result.put("pages", p.getPages());
        return result;
    }

    public Map<String, Object> getBookDetail(Long id) {
        BookInfo book = bookInfoMapper.selectById(id);
        if (book == null) throw new BusinessException("Book not found");
        return toBookDetail(book);
    }

    public List<Map<String, Object>> getBookCopies(Long bookId) {
        return bookCopyMapper.selectList(new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, bookId))
                .stream().map(copy -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", copy.getId()); item.put("barcode", copy.getBarcode());
                    item.put("status", copy.getStatus()); item.put("statusLabel", getStatusLabel(copy.getStatus()));
                    item.put("price", copy.getPrice());
                    item.put("purchaseDate", copy.getPurchaseDate() != null ? copy.getPurchaseDate().toString() : null);
                    if (copy.getLocationId() != null) {
                        Location loc = locationMapper.selectById(copy.getLocationId());
                        item.put("locationName", loc != null ? loc.getName() : null);
                    }
                    return item;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getHotBooks(int limit) {
        return bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>().eq(BookInfo::getStatus, 1)
                .orderByDesc(BookInfo::getBorrowCount).last("LIMIT " + limit))
                .stream().map(this::toBookItem).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewArrivals(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return bookInfoMapper.selectList(new LambdaQueryWrapper<BookInfo>().eq(BookInfo::getStatus, 1)
                        .ge(BookInfo::getCreateTime, since).orderByDesc(BookInfo::getCreateTime).last("LIMIT " + limit))
                .stream().map(this::toBookItem).collect(Collectors.toList());
    }

    // ==================== Private helpers ====================

    private Map<String, Object> toBookItem(BookInfo book) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", book.getId()); item.put("isbn", book.getIsbn());
        item.put("title", book.getTitle()); item.put("author", book.getAuthor());
        item.put("coverUrl", book.getCoverUrl());
        item.put("publishDate", book.getPublishDate() != null ? book.getPublishDate().toString() : null);
        item.put("price", book.getPrice()); item.put("totalCopies", book.getTotalCopies());
        item.put("availableCopies", book.getAvailableCopies()); item.put("borrowCount", book.getBorrowCount());
        item.put("rating", book.getRating()); item.put("summary", book.getSummary());
        if (book.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(book.getCategoryId());
            item.put("categoryName", cat != null ? cat.getName() : null);
        }
        return item;
    }

    private Map<String, Object> toAdminItem(BookInfo book) {
        Map<String, Object> item = new HashMap<>(toBookItem(book));
        long totalCopies = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, book.getId()));
        long availCopies = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, book.getId()).eq(BookCopy::getStatus, "in"));
        item.put("totalCopies", totalCopies); item.put("availableCopies", availCopies);
        item.put("status", book.getStatus());
        return item;
    }

    private Map<String, Object> toBookDetail(BookInfo book) {
        Map<String, Object> item = new HashMap<>(toBookItem(book));
        item.put("subTitle", book.getSubTitle()); item.put("translator", book.getTranslator());
        item.put("publisherId", book.getPublisherId()); item.put("categoryId", book.getCategoryId());
        item.put("pages", book.getPages()); item.put("binding", book.getBinding());
        item.put("language", book.getLanguage()); item.put("ratingCount", book.getRatingCount());
        item.put("status", book.getStatus());
        item.put("createTime", book.getCreateTime() != null ? book.getCreateTime().toString() : null);
        return item;
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "in" -> "Available"; case "borrowed" -> "Borrowed";
            case "reserved" -> "Reserved"; case "maintenance" -> "Maintenance";
            case "lost" -> "Lost"; case "discarded" -> "Discarded";
            default -> status;
        };
    }
}
