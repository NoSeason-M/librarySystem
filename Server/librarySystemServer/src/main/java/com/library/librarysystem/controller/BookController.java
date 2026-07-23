package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.service.BookService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> adminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String binding,
            @RequestParam(required = false) String yearStart,
            @RequestParam(required = false) String yearEnd,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bookService.listAdminBooks(keyword, author, isbn, categoryId, publisher, language, binding, yearStart, yearEnd, statusFilter, page, size));
    }

    @PostMapping @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> req) {
        return Result.success(Map.of("bookId", bookService.createBook(req)));
    }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        bookService.updateBook(id, req); return Result.success();
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id); return Result.success();
    }
    @PostMapping("/{id}/cover") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Map<String, String>> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(Map.of("coverUrl", bookService.uploadCover(id, file)));
    }
    @PostMapping("/import") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> importBooks(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(bookService.importBooks(file));
    }
    @GetMapping("/export") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public void exportBooks(@RequestParam(required = false) String keyword, HttpServletResponse response) throws Exception {
        bookService.exportBooks(response, keyword);
    }

    @GetMapping public Result<Map<String, Object>> search(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(bookService.searchBooks(keyword, page, size));
    }
    @GetMapping("/hot") public Result<List<Map<String, Object>>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getHotBooks(limit));
    }
    @GetMapping("/new-arrivals") public Result<List<Map<String, Object>>> newArrivals(@RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getNewArrivals(days, limit));
    }
    @GetMapping("/{id}") public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(bookService.getBookDetail(id));
    }
    @GetMapping("/{id}/copies") public Result<List<Map<String, Object>>> copies(@PathVariable Long id) {
        return Result.success(bookService.getBookCopies(id));
    }
}
