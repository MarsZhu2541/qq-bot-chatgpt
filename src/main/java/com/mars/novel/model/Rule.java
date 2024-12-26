package com.mars.novel.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author pcdd
 * Created at 2024/3/10
 */
@Data
@Slf4j
public class Rule {

    private int id;
    private String url;
    private String name;
    private String comment;
    private String type;

    private Search search;
    private Book book;
    private Chapter chapter;

    @Data
    public static class Search {
        private String url;
        private String method;
        private String param;
        private String body;
        private String cookies;
        // 搜索结果是否有分页
        private Boolean pagination;
        private String nextPage;
        // 以下字段不同书源可能不同
        private String result;
        private String bookName;
        private String latestChapter;
        private String author;
        private String update;
    }

    @Data
    public static class Book {
        private String url;
        private String bookName;
        private String author;
        private String intro;
        private String category;
        private String coverUrl;
        private String latestChapter;
        private String latestUpdate;
        private String isEnd;
        private String catalog;
        private Integer catalogOffset;
    }

    @Data
    public static class Chapter {
        private String url;
        // 章节是否有分页
        private Boolean pagination;
        private String nextPage;
        private Integer chapterNo;
        private String title;
        private String content;
        private Boolean paragraphTagClosed;
        private String paragraphTag;
        private String filterTxt;
        private String filterTag;
    }

    @Bean
    @SneakyThrows
    public static List<Rule> initRules() {

        List<Rule> rules = new ArrayList<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:" + "/rule/*.json");

        for (Resource resource : resources) {
            if (resource.isReadable()) {
                Rule rule = new ObjectMapper().readValue(resource.getInputStream(), Rule.class);
                rules.add(rule);
            }
        }
        return rules;
    }
}
