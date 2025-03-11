package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ObjectController {
    @Resource
    private ImageService service;

    @GetMapping("/images/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestPath = request.getServletPath();
        String prefix = "/images/";
        ServletOutputStream stream = null;

        try {
            if (!requestPath.startsWith(prefix)) {
                response.setStatus(400);
                stream = response.getOutputStream();
                stream.println(RestBean.failure(400, "Invalid path").toString());
                return;
            }

            String imagePath = requestPath.substring(prefix.length());
            if (imagePath.isEmpty() || imagePath.length() <= 13) { // 根据实际需求调整长度校验
                response.setStatus(404);
                stream = response.getOutputStream();
                stream.println(RestBean.failure(404, "Not found").toString());
                return;
            }

            // 动态设置 Content-Type
            String extension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
            String contentType = "image/" + extension;
            response.setHeader("Content-Type", contentType);

            stream = response.getOutputStream();
            service.fetchImageFromMinio(stream, imagePath);
            response.setHeader("Cache-Control", "max-age=2592000");

        } catch (ErrorResponseException e) {
            int code = e.response().code();
            response.setStatus(code);
            stream.println(RestBean.failure(code, "Failed to fetch image").toString());
        } catch (Exception e) {
            log.error("MinIO 请求失败：" + e.getMessage(), e);
            response.setStatus(500);
            stream.println(RestBean.failure(500, "Internal server error").toString());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
