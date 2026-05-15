package com.ticket.service;

import com.ticket.entity.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    /**
     * 获取所有分类（树形结构）
     */
    List<Map<String, Object>> getCategoryTree();

    /**
     * 获取所有分类（平铺）
     */
    List<Category> getAllCategories();

    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Long id);

    /**
     * 创建分类
     */
    Category createCategory(Category category);

    /**
     * 更新分类
     */
    Category updateCategory(Long id, Category category);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
