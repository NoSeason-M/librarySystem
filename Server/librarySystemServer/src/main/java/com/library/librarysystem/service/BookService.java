package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.librarysystem.common.exception.BusinessException;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookInfoMapper bookInfoMapper;
    private final BookCopyMapper bookCopyMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;

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
        book.setPublishDate(req.get("publishDate") != null ? java.time.LocalDate.parse((String) req.get("publishDate")) : null);
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
        if (req.containsKey("publishDate")) book.setPublishDate(req.get("publishDate") != null ? java.time.LocalDate.parse((String) req.get("publishDate")) : null);
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

    // ==================== Public ====================

    public Map<String, Object> searchBooks(String keyword, int page, int size) {
        LambdaQueryWrapper<BookInfo> qw = new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(BookInfo::getTitle, keyword)
                    .or().like(BookInfo::getAuthor, keyword)
                    .or().like(BookInfo::getIsbn, keyword));
        }
        qw.orderByDesc(BookInfo::getBorrowCount);
        IPage<BookInfo> p = bookInfoMapper.selectPage(new Page<>(page, size), qw);
        List<Map<String, Object>> records = p.getRecords().stream().map(this::toBookItem).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        return result;
    }

    public Map<String, Object> getBookDetail(Long id) {
        BookInfo book = bookInfoMapper.selectById(id);
        if (book == null) throw new BusinessException("Book not found");
        return toBookDetail(book);
    }

    public List<Map<String, Object>> getBookCopies(Long bookId) {
        List<BookCopy> copies = bookCopyMapper.selectList(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, bookId));
        return copies.stream().map(copy -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", copy.getId());
            item.put("barcode", copy.getBarcode());
            item.put("status", copy.getStatus());
            item.put("statusLabel", getStatusLabel(copy.getStatus()));
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
        List<BookInfo> books = bookInfoMapper.selectList(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getStatus, 1)
                        .orderByDesc(BookInfo::getBorrowCount)
                        .last("LIMIT " + limit));
        return books.stream().map(this::toBookItem).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewArrivals(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<BookInfo> books = bookInfoMapper.selectList(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getStatus, 1)
                        .ge(BookInfo::getCreateTime, since)
                        .orderByDesc(BookInfo::getCreateTime)
                        .last("LIMIT " + limit));
        return books.stream().map(this::toBookItem).collect(Collectors.toList());
    }

    // ==================== Private helpers ====================

    private Map<String, Object> toBookItem(BookInfo book) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", book.getId());
        item.put("isbn", book.getIsbn());
        item.put("title", book.getTitle());
        item.put("author", book.getAuthor());
        item.put("coverUrl", book.getCoverUrl());
        item.put("publishDate", book.getPublishDate() != null ? book.getPublishDate().toString() : null);
        item.put("price", book.getPrice());
        item.put("totalCopies", book.getTotalCopies());
        item.put("availableCopies", book.getAvailableCopies());
        item.put("borrowCount", book.getBorrowCount());
        item.put("rating", book.getRating());
        item.put("summary", book.getSummary());
        if (book.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(book.getCategoryId());
            item.put("categoryName", cat != null ? cat.getName() : null);
        }
        return item;
    }

    private Map<String, Object> toAdminItem(BookInfo book) {
        Map<String, Object> item = new HashMap<>(toBookItem(book));
        long totalCopies = bookCopyMapper.selectCount(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, book.getId()));
        long availCopies = bookCopyMapper.selectCount(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, book.getId()).eq(BookCopy::getStatus, "in"));
        item.put("totalCopies", totalCopies);
        item.put("availableCopies", availCopies);
        item.put("status", book.getStatus());
        return item;
    }

    private Map<String, Object> toBookDetail(BookInfo book) {
        Map<String, Object> item = new HashMap<>(toBookItem(book));
        item.put("subTitle", book.getSubTitle());
        item.put("translator", book.getTranslator());
        item.put("publisherId", book.getPublisherId());
        item.put("categoryId", book.getCategoryId());
        item.put("pages", book.getPages());
        item.put("binding", book.getBinding());
        item.put("language", book.getLanguage());
        item.put("ratingCount", book.getRatingCount());
        item.put("status", book.getStatus());
        item.put("createTime", book.getCreateTime() != null ? book.getCreateTime().toString() : null);
        return item;
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "in" -> "Available";
            case "borrowed" -> "Borrowed";
            case "reserved" -> "Reserved";
            case "maintenance" -> "Maintenance";
            case "lost" -> "Lost";
            case "discarded" -> "Discarded";
            default -> status;
        };
    }
}
