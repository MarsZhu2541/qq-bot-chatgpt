package com.mars.deltaforce.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class SimpleImageUtils {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * Base64 转 PNG 文件
     */
    public static File base64ToPngFile(String base64Image) throws IOException {
        // 清理 Base64 前缀
        String cleanBase64 = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");

        // 解码 Base64
        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

        // 创建 PNG 文件
        String fileName = "img_" + UUID.randomUUID() + ".png";
        File pngFile = new File(TEMP_DIR, fileName);
        pngFile.deleteOnExit();
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(pngFile)) {
            fos.write(imageBytes);
        }

        return pngFile;
    }

    public static boolean deleteFile(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }
}