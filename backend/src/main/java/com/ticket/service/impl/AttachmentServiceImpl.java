package com.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticket.entity.Attachment;
import com.ticket.mapper.AttachmentMapper;
import com.ticket.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.allowed-types:image/jpeg,image/png,image/gif,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/plain}")
    private String allowedTypes;

    @Value("${file.max-size:10485760}")
    private long maxSize;

    @Override
    public Attachment upload(Long ticketId, Long replyId, MultipartFile file, Long uploaderId) throws IOException {
        // 验证文件大小
        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小超过限制（最大10MB）");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes.split(",")).contains(contentType)) {
            throw new RuntimeException("不支持的文件类型");
        }

        // 生成存储路径：uploads/2026/05/uuid.ext
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String ext = getExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
        Path relativePath = Paths.get(datePath, storedName);
        Path absolutePath = Paths.get(uploadDir).resolve(relativePath);

        // 创建目录
        Files.createDirectories(absolutePath.getParent());

        // 保存文件
        file.transferTo(absolutePath.toFile());

        // 保存记录
        Attachment attachment = new Attachment();
        attachment.setTicketId(ticketId);
        attachment.setReplyId(replyId);
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setStoredName(storedName);
        attachment.setFilePath(relativePath.toString().replace("\\", "/"));
        attachment.setFileSize(file.getSize());
        attachment.setFileType(contentType);
        attachment.setUploaderId(uploaderId);
        attachment.setCreatedAt(java.time.LocalDateTime.now());

        attachmentMapper.insert(attachment);
        return attachment;
    }

    @Override
    public List<Attachment> getAttachmentsByTicketId(Long ticketId) {
        return attachmentMapper.selectList(
            new LambdaQueryWrapper<Attachment>()
                .eq(Attachment::getTicketId, ticketId)
                .orderByDesc(Attachment::getCreatedAt)
        );
    }

    @Override
    public List<Attachment> getAttachmentsByReplyId(Long replyId) {
        return attachmentMapper.selectList(
            new LambdaQueryWrapper<Attachment>()
                .eq(Attachment::getReplyId, replyId)
                .orderByDesc(Attachment::getCreatedAt)
        );
    }

    @Override
    public Attachment getAttachmentById(Long id) {
        return attachmentMapper.selectById(id);
    }

    @Override
    public void deleteAttachment(Long id, Long operatorId) throws IOException {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }

        // 删除物理文件
        Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath());
        Files.deleteIfExists(filePath);

        // 删除数据库记录
        attachmentMapper.deleteById(id);
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
