package com.campus.recruitment.controller;

import com.campus.recruitment.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    /** 上传文件，返回可访问的URL */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 获取原始文件后缀
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成UUID文件名避免冲突
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        // 确保上传目录存在
        File dest = new File(uploadPath, newFilename);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        // 保存文件
        file.transferTo(dest);

        // 返回访问URL
        String url = urlPrefix + newFilename;
        return Result.success(url);
    }
}
