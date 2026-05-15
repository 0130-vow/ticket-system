package com.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticket.entity.Tag;
import com.ticket.entity.TicketTagRelation;
import com.ticket.mapper.TagMapper;
import com.ticket.mapper.TicketTagRelationMapper;
import com.ticket.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private TicketTagRelationMapper ticketTagRelationMapper;

    @Override
    public List<Tag> getAllTags() {
        return tagMapper.selectList(null);
    }

    @Override
    public Tag getTagById(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public Tag createTag(Tag tag) {
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public Tag updateTag(Long id, Tag tag) {
        Tag existing = tagMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("标签不存在");
        }
        tag.setId(id);
        tagMapper.updateById(tag);
        return tagMapper.selectById(id);
    }

    @Override
    public void deleteTag(Long id) {
        // 先删除关联关系
        ticketTagRelationMapper.delete(
            new LambdaQueryWrapper<TicketTagRelation>()
                .eq(TicketTagRelation::getTagId, id)
        );
        tagMapper.deleteById(id);
    }

    @Override
    public List<Tag> getTagsByTicketId(Long ticketId) {
        List<TicketTagRelation> relations = ticketTagRelationMapper.selectList(
            new LambdaQueryWrapper<TicketTagRelation>()
                .eq(TicketTagRelation::getTicketId, ticketId)
        );

        if (relations.isEmpty()) {
            return List.of();
        }

        List<Long> tagIds = relations.stream()
            .map(TicketTagRelation::getTagId)
            .collect(Collectors.toList());

        return tagMapper.selectBatchIds(tagIds);
    }

    @Override
    @Transactional
    public void setTicketTags(Long ticketId, List<Long> tagIds) {
        // 先删除原有关系
        ticketTagRelationMapper.delete(
            new LambdaQueryWrapper<TicketTagRelation>()
                .eq(TicketTagRelation::getTicketId, ticketId)
        );

        // 添加新关系
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                TicketTagRelation relation = new TicketTagRelation();
                relation.setTicketId(ticketId);
                relation.setTagId(tagId);
                ticketTagRelationMapper.insert(relation);
            }
        }
    }
}
