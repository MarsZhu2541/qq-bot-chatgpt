package com.mars.novel.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pcdd
 * Created at 2024/3/10
 */
@Data
@Builder
@Slf4j
public class Chapter {

    private String url;
    private Integer chapterNo;
    private String title;
    private String content;


    public List<String> getContentMessages() {
        List<String> messages = new ArrayList<>();
        log.info("Before filter: {}", content);
        content = content.replace("www.", "")
                .replace(".com", "").replace(".net", "")
                .replace(".info", "").replace(".la", "").replace(".","");
        log.info("After filter: {}", content);
        int length = content.length();
        log.info(title + " 字数： " + length);
        int start = 0;
        while (start < length) {
            int end = Math.min(start + 2000, length);
            messages.add(String.format("(%d/%d)\n", start / 2000 + 1, length / 2000 + 1) + content.substring(start, end));
            start = end;
        }
        return messages;
    }

}
