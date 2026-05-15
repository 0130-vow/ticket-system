package com.ticket.controller;

import com.ticket.entity.Tag;
import com.ticket.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", tagService.getAllTags());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Tag tag = tagService.getTagById(id);
        if (tag == null) {
            result.put("code", 404);
            result.put("message", "标签不存在");
        } else {
            result.put("code", 200);
            result.put("data", tag);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Tag tag) {
        Map<String, Object> result = new HashMap<>();
        try {
            Tag created = tagService.createTag(tag);
            result.put("code", 200);
            result.put("message", "标签创建成功");
            result.put("data", created);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Tag tag) {
        Map<String, Object> result = new HashMap<>();
        try {
            Tag updated = tagService.updateTag(id, tag);
            result.put("code", 200);
            result.put("message", "标签更新成功");
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
            tagService.deleteTag(id);
            result.put("code", 200);
            result.put("message", "标签删除成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
