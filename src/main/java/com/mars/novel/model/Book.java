package com.mars.novel.model;

import cn.hutool.core.lang.ConsoleTable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
@Data
public class Book {

    private String url;
    private String bookName;
    private String author;
    private String intro;
    private String category;
    private String coverUrl;
    private String latestChapter;
    private String latestUpdate;
    private String isEnd;
    private List<Chapter> catalog;

    @Override
    public String toString() {
        Chapter latestChapter = catalog.get(catalog.size() - 1);
        String book = bookName + "\n作者：" + author + "\n分类: " + category + "\n简介： " + intro + "\n最新章节: "
                + latestChapter.getTitle() + "\n最后更新时间: " + latestUpdate + "\n是否完结：" + isEnd;
        return book.replace("www.", "").replace(".com", "").replace(".net", "");
    }

    public List<String> getCatalogMessages() {
        List<String> catalogMessages = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = catalog.size(); i >= 1; i--) {
            Chapter r = catalog.get(i - 1);
            stringBuilder.append(i).append(": ").append(r.getTitle()).append("\n");
            if (i % 200 == 1) {
                String format = String.format("\n" + bookName + " 章节目录(%d-%d):\n", Math.min(i + 200, catalog.size()), i);
                catalogMessages.add(format + stringBuilder);
                stringBuilder = new StringBuilder();
            }
        }
        return catalogMessages;
    }

    public String getLatestCatalog() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= Math.min(catalog.size(), 20); i++) {
            Chapter r = catalog.get(catalog.size() - i);
            stringBuilder.append(catalog.size() - i + 1).append(": ").append(r.getTitle()).append("\n");
        }
        String format = String.format("\n" + bookName + " 章节目录(%d-%d):\n", catalog.size(), Math.max(catalog.size() - 19, 1));
        return format + stringBuilder;
    }
}
