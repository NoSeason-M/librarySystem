package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.entity.Location;
import com.library.librarysystem.mapper.LocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationMapper locationMapper;

    @GetMapping
    public Result<List<Location>> list() {
        return Result.success(locationMapper.selectList(null));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<List<Location>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return Result.success(locationMapper.selectList(null));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> create(@RequestBody Location location) {
        locationMapper.insert(location);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Location location) {
        location.setId(id);
        locationMapper.updateById(location);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER')")
    public Result<Void> delete(@PathVariable Long id) {
        locationMapper.deleteById(id);
        return Result.success();
    }
}
