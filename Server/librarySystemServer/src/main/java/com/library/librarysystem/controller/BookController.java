package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // ==================== Admin CRUD ====================

    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> adminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bookService.listAdminBooks(keyword, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Map<String, Long>> create(@RequestBody Map<String, Object> req) {
        Long id = bookService.createBook(req);
        return Result.success(Map.of("bookId", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        bookService.updateBook(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    // ==================== Public ====================

    @GetMapping
    public Result<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bookService.searchBooks(keyword, page, size));
    }

    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getHotBooks(limit));
    }

    @GetMapping("/new-arrivals")
    public Result<List<Map<String, Object>>> newArrivals(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getNewArrivals(days, limit));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(bookService.getBookDetail(id));
    }

    @GetMapping("/{id}/copies")
    public Result<List<Map<String, Object>>> copies(@PathVariable Long id) {
        return Result.success(bookService.getBookCopies(id));
    }
}
