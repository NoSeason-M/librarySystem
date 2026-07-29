package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.entity.Publisher;
import com.library.librarysystem.mapper.PublisherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {
    private final PublisherMapper publisherMapper;

    @GetMapping
    public Result<List<Publisher>> list() {
        return Result.success(publisherMapper.selectList(null));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<List<Publisher>> page(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(publisherMapper.selectList(null));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> create(@RequestBody Publisher publisher) {
        publisherMapper.insert(publisher);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Publisher publisher) {
        publisher.setId(id);
        publisherMapper.updateById(publisher);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> delete(@PathVariable Long id) {
        publisherMapper.deleteById(id);
        return Result.success();
    }
}
