package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.entity.Category;
import com.library.librarysystem.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;

    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        List<Category> all = categoryMapper.selectList(null);
        List<Map<String, Object>> result = buildTree(all, 0L);
        return Result.success(result);
    }

    private List<Map<String, Object>> buildTree(List<Category> all, Long parentId) {
        return all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().longValue() == parentId.longValue())
                .map(c -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", c.getId());
                    node.put("name", c.getName());
                    node.put("code", c.getCode());
                    node.put("level", c.getLevel());
                    node.put("children", buildTree(all, c.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }
}
