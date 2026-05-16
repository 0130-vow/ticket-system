package com.ticket.controller;

import com.ticket.entity.Attachment;
import com.ticket.service.AttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 上传附件
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ticketId") Long ticketId,
            @RequestParam(value = "replyId", required = false) Long replyId,
            HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Attachment attachment = attachmentService.upload(ticketId, replyId, file, userId);
            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("data", attachment);
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件上传失败");
        }
        return result;
    }

    /**
     * 获取工单附件列表
     */
    @GetMapping("/ticket/{ticketId}")
    public Map<String, Object> getByTicketId(@PathVariable Long ticketId) {
        Map<String, Object> result = new HashMap<>();
        List<Attachment> attachments = attachmentService.getAttachmentsByTicketId(ticketId);
        result.put("code", 200);
        result.put("data", attachments);
        return result;
    }

    /**
     * 下载附件
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment attachment = attachmentService.getAttachmentById(id);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    /**
     * 删除附件
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            attachmentService.deleteAttachment(id, userId);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件删除失败");
        }
        return result;
    }
}
