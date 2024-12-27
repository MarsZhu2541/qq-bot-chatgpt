package com.mars.novel.service;

import com.mars.novel.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class NovelServiceImplTest {

    private NovelServiceImpl novelServiceImpl = new NovelServiceImpl(getConfigBean());

    @Test
    void searchTest() {
        log.info(Rule.initRules().get(3).getName());
        novelServiceImpl.searchBooks("魔女，火球与蒸汽邪神");
        Book bookDetail = novelServiceImpl.getBookDetail(0);
        log.info("bookDetail: {}", bookDetail.toString());
        log.info("getCatalogMessages: {}", bookDetail.getCatalogMessages().toString());
        Chapter chapterContent = novelServiceImpl.getChapterContent(150);
        List<String> contentMessages = chapterContent.getContentMessages();
        log.info("contentMessages: {}", contentMessages);

    }

    private ConfigBean getConfigBean() {
        ConfigBean configBean = new ConfigBean();
        configBean.setSourceId(3);
        configBean.setExtName("txt");
        configBean.setThreads(-1);
        configBean.setMinInterval(100);
        configBean.setMaxInterval(300);
        configBean.setRetryMaxInterval(2000);
        configBean.setRetryMinInterval(500);
        configBean.setMaxRetryAttempts(3);
        return configBean;
    }
}