package com.mars.deltaforce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SimpleImageService {

    private final Map<String, File> imageCache = new HashMap<>();
    /**
     * 保存 Base64 图片并返回文件ID
     */
    public String save(String base64Image) throws IOException {
        String fileId = java.util.UUID.randomUUID().toString();

        // 创建 PNG 文件
        File pngFile = SimpleImageUtils.base64ToPngFile(base64Image);

        // 存储到缓存
        imageCache.put(fileId, pngFile);
        log.info("File saved {}, path :{}:",fileId, pngFile.getPath());

        return fileId;
    }

    /**
     * 根据文件ID获取文件
     */
    public File getFile(String fileId) {
        return imageCache.get(fileId);
    }

    public void deleteFile(String fileId) {
        if (SimpleImageUtils.deleteFile(getFile(fileId))){
            log.info("File {} deleted successfully,", fileId);
        }else {
            log.warn("File {} deleted failed,", fileId);
        }
        imageCache.remove(fileId);
    }
}