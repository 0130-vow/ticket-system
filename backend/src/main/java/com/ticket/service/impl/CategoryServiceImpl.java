package com.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticket.entity.Category;
import com.ticket.mapper.CategoryMapper;
import com.ticket.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
        );

        // 按 parentId 分组
        Map<Long, List<Category>> parentMap = allCategories.stream()
            .filter(c -> c.getParentId() != null && c.getParentId() > 0)
            .collect(Collectors.groupingBy(Category::getParentId));

        // 构建树形结构
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Category category : allCategories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                Map<String, Object> node = buildNode(category);
                List<Category> children = parentMap.getOrDefault(category.getId(), new ArrayList<>());
                if (!children.isEmpty()) {
                    List<Map<String, Object>> childNodes = children.stream()
                        .map(this::buildNode)
                        .collect(Collectors.toList());
                    node.put("children", childNodes);
                }
                tree.add(node);
            }
        }
        return tree;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
        );
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public Category createCategory(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        category.setLevel(category.getParentId() == 0 ? 1 : 2);
        category.setStatus(1);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        category.setId(id);
        categoryMapper.updateById(category);
        return categoryMapper.selectById(id);
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        Long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            throw new RuntimeException("该分类下有子分类，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private Map<String, Object> buildNode(Category category) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", category.getId());
        node.put("name", category.getName());
        node.put("parentId", category.getParentId());
        node.put("level", category.getLevel());
        node.put("sortOrder", category.getSortOrder());
        node.put("status", category.getStatus());
        return node;
    }
}
