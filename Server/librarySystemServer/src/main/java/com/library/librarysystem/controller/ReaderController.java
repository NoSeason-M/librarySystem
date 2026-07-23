package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.service.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderController {
    private final ReaderService readerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long readerTypeId,
            @RequestParam(required = false) Integer cardStatus,
            @RequestParam(required = false) String readerNo,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String registerDateStart,
            @RequestParam(required = false) String registerDateEnd,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(readerService.listReaders(keyword, readerTypeId, cardStatus, readerNo, email, registerDateStart, registerDateEnd, page, size));
    }

    @GetMapping("/types") public Result<List<Map<String, Object>>> types() {
        return Result.success(readerService.getReaderTypes());
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(readerService.getReaderDetail(id));
    }
    @PostMapping @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> req) {
        return Result.success(readerService.createReader(req));
    }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        readerService.updateReader(id, req); return Result.success();
    }
    @PutMapping("/{id}/card") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Void> cardAction(@PathVariable Long id, @RequestBody Map<String, String> req) {
        readerService.updateCardStatus(id, req.get("action")); return Result.success();
    }
}
