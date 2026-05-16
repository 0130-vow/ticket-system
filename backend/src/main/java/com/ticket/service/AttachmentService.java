package com.ticket.service;

import com.ticket.entity.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {

    /**
     * 上传附件
     */
    Attachment upload(Long ticketId, Long replyId, MultipartFile file, Long uploaderId) throws IOException;

    /**
     * 获取工单附件列表
     */
    List<Attachment> getAttachmentsByTicketId(Long ticketId);

    /**
     * 获取回复附件列表
     */
    List<Attachment> getAttachmentsByReplyId(Long replyId);

    /**
     * 根据ID获取附件
     */
    Attachment getAttachmentById(Long id);

    /**
     * 删除附件
     */
    void deleteAttachment(Long id, Long operatorId) throws IOException;
}
