package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.security.UserDetailsImpl;
import com.library.librarysystem.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

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
}
