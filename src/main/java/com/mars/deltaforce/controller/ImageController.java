package com.mars.deltaforce.controller;

import com.mars.deltaforce.service.SimpleImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private SimpleImageService imageService;

    /*
     * 获取图片 - 返回图片流
     */
    @GetMapping(value = "/{fileId}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Resource getImage(@PathVariable String fileId) {

        File file = imageService.getFile(fileId);
        if (file == null || !file.exists()) {
            throw new RuntimeException("图片不存在");
        }
        return new FileSystemResource(file);
    }
}