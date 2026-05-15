package com.ticket.service;

import com.ticket.entity.Tag;

import java.util.List;

public interface TagService {

    /**
     * 获取所有标签
     */
    List<Tag> getAllTags();

    /**
     * 根据ID获取标签
     */
    Tag getTagById(Long id);

    /**
     * 创建标签
     */
    Tag createTag(Tag tag);

    /**
     * 更新标签
     */
    Tag updateTag(Long id, Tag tag);

    /**
     * 删除标签
     */
    void deleteTag(Long id);

    /**
     * 获取工单的标签列表
     */
    List<Tag> getTagsByTicketId(Long ticketId);

    /**
     * 设置工单的标签
     */
    void setTicketTags(Long ticketId, List<Long> tagIds);
}
