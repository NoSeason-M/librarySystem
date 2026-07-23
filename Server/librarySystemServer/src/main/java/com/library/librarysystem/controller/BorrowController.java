package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.security.UserDetailsImpl;
import com.library.librarysystem.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // ==================== Reader-facing ====================

    @GetMapping("/current")
    public Result<List<Map<String, Object>>> currentBorrowing(
            @RequestParam String readerNo) {
        return Result.success(borrowService.getCurrentBorrowing(readerNo));
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(
            @RequestParam String readerNo) {
        return Result.success(borrowService.getReaderSummary(readerNo));
    }

    // ==================== Admin: Borrow flow ====================

    /**
     * Look up reader by reader card number.
     */
    @PostMapping("/check-reader")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> checkReader(@RequestBody Map<String, String> req) {
        return Result.success(borrowService.checkReader(req.get("readerNo")));
    }

    /**
     * Check a book barcode is available for borrowing.
     */
    @PostMapping("/check-barcode")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> checkBarcode(@RequestBody Map<String, String> req) {
        return Result.success(borrowService.checkBarcode(req.get("barcode")));
    }

    /**
     * Execute borrowing for one or more barcodes.
     * Matches API.md 5.1: POST /borrow
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> borrow(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal UserDetailsImpl user) {
        String readerNo = (String) req.get("readerNo");
        List<String> barcodes = (List<String>) req.get("barcodes");
        return Result.success(borrowService.borrowBooks(readerNo, barcodes, user.getId()));
    }

    // ==================== Admin: Return flow ====================

    /**
     * Look up an active borrow record by barcode for return.
     */
    @PostMapping("/find-return")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> findReturn(@RequestBody Map<String, String> req) {
        return Result.success(borrowService.findReturnByBarcode(req.get("barcode")));
    }

    /**
     * Execute return for borrowed books (batch).
     * Matches API.md 5.2: PUT /borrow/return {barcodes: [...]}
     */
    @PutMapping("/return")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> doReturn(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<String> barcodes = (List<String>) req.get("barcodes");
        return Result.success(borrowService.returnBooks(barcodes, user.getId()));
    }
}
