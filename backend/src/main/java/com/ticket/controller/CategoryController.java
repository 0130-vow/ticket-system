package com.ticket.controller;

import com.ticket.entity.Category;
import com.ticket.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Boolean tree) {
        Map<String, Object> result = new HashMap<>();
        if (Boolean.TRUE.equals(tree)) {
            result.put("data", categoryService.getCategoryTree());
        } else {
            result.put("data", categoryService.getAllCategories());
        }
        result.put("code", 200);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            result.put("code", 404);
            result.put("message", "分类不存在");
        } else {
            result.put("code", 200);
            result.put("data", category);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Category category) {
        Map<String, Object> result = new HashMap<>();
        try {
            Category created = categoryService.createCategory(category);
            result.put("code", 200);
            result.put("message", "分类创建成功");
            result.put("data", created);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Category category) {
        Map<String, Object> result = new HashMap<>();
        try {
            Category updated = categoryService.updateCategory(id, category);
            result.put("code", 200);
            result.put("message", "分类更新成功");
            result.put("data", updated);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            categoryService.deleteCategory(id);
            result.put("code", 200);
            result.put("message", "分类删除成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
