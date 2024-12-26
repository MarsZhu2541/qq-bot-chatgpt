package com.mars.novel.service;

import com.mars.novel.model.Book;
import com.mars.novel.model.Chapter;
import com.mars.novel.model.ConfigBean;
import com.mars.novel.model.SearchResult;
import com.mars.novel.parse.BookParser;
import com.mars.novel.parse.CatalogParser;
import com.mars.novel.parse.ChapterParser;
import com.mars.novel.parse.SearchResultParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * @author pcdd
 * Created at 2021/6/10
 */
@Service
@Slf4j
public class NovelServiceImpl {


    private SearchResultParser searchResultParser;
    private BookParser bookParser;
    private CatalogParser catalogParser;
    private ChapterParser chapterParser;

    private List<SearchResult> searchResults = Collections.emptyList();
    private List<Chapter> catalog = Collections.emptyList();
    private Book book = null;


    public NovelServiceImpl(ConfigBean config) {
        searchResultParser =  new SearchResultParser(config.getSourceId());
        bookParser = new BookParser(config.getSourceId());
        catalogParser = new CatalogParser(config.getSourceId());
        chapterParser = new ChapterParser(config);
    }


    public void setSourceId(int sourceId) {
        searchResultParser.setSourceId(sourceId);
        bookParser.setSourceId(sourceId);
        catalogParser.setSourceId(sourceId);
        chapterParser.setSourceId(sourceId);
    }

    /**
     * 搜索小说
     *
     * @param keyword 关键字
     * @return 匹配的小说列表
     */
    public String searchBooks(String keyword) {
        log.info("<== 正在搜索...");

        searchResults = searchResultParser.parse(keyword);

        log.info("搜索到 {} 条记录", searchResults.size());

        searchResults = searchResults.subList(0, Math.min(5, searchResults.size()));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < searchResults.size(); i++) {
            sb.append("\n序号: ").append(i + 1);
            sb.append(searchResults.get(i).toString());
            sb.append("\n");
        }

        return sb.toString();
    }


    public Book getBookDetail(int i){
        String url = searchResults.get(i).getUrl();
        book = bookParser.parse(url);
        catalog = catalogParser.parse(url, 1, Integer.MAX_VALUE);
        book.setCatalog(catalog);
        return book;
    }

    public Chapter getChapterContent(int i){
        return chapterParser.parse(book.getUrl(), catalog.get(i-1));
    }

}
