package com.mars.novel.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author pcdd
 * Created at 2022/5/23
 */
@Data
@Builder
public class SearchResult {

    private String url;
    private String bookName;
    private String author;
    private String intro;
    private String latestChapter;
    private String latestUpdate;

    @Override
    public String toString() {
        return "\n书名: " + bookName + "\n作者: " + author + "\n最新章节: " + latestChapter + "\n最后更新时间: " + latestUpdate;
    }
}
