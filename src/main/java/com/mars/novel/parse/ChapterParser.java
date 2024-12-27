package com.mars.novel.parse;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.mars.novel.core.ChapterConverter;
import com.mars.novel.core.Source;
import com.mars.novel.model.Chapter;
import com.mars.novel.model.ConfigBean;
import com.mars.novel.util.CrawlUtils;
import com.mars.novel.util.RandomUA;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class ChapterParser extends Source {

    private static final int TIMEOUT_MILLS = 15_000;
    private final ConfigBean config;
    private final ChapterConverter chapterConverter;

    public ChapterParser(ConfigBean config) {
        super(config.getSourceId());
        this.config = config;
        this.chapterConverter = new ChapterConverter(config);
    }



    public Chapter parse(String referrer, Chapter chapter) {
        try {
            Console.log("<== 正在下载: 【{}】", chapter.getTitle());
            // ExceptionUtils.randomThrow();
            chapter.setContent(crawl(referrer, chapter.getUrl(), false));
            return chapterConverter.convert(chapter, config.getExtName());

        } catch (Exception e) {
            return retry(chapter);
        }
    }

    private Chapter retry(Chapter chapter) {
        for (int attempt = 1; attempt <= config.getMaxRetryAttempts(); attempt++) {
            try {
                Console.log("==> 章节下载失败，正在重试: 【{}】，尝试次数: {}/{}", chapter.getTitle(), attempt, config.getMaxRetryAttempts());
                chapter.setContent(crawl("",chapter.getUrl(), true));
                Console.log("<== 重试成功: 【{}】", chapter.getTitle());
                return chapterConverter.convert(chapter, config.getExtName());

            } catch (Exception e) {
                Console.error("==> 第 {} 次重试失败: 【{}】，原因: {}", attempt, chapter.getTitle(), e.getMessage());
                if (attempt == config.getMaxRetryAttempts()) {
                    // 最终失败时记录日志
                }
            }
        }

        return null;
    }

    /**
     * 爬取正文内容
     */
    private String crawl(String referrer, String url, boolean isRetry) throws InterruptedException, IOException {
        boolean isPaging = this.rule.getChapter().getPagination();
        String nextUrl = url;
        StringBuilder sb = new StringBuilder();

        do {
            Map<String, String> cookies = CrawlUtils.buildCookies(this.rule.getSearch().getCookies());
            cookies.put("Hm_lpvt_07f72e367b2a5dc91d07af6c1ccc52ef", String.valueOf(Instant.now().getEpochSecond()));
            Document document = Jsoup.connect(nextUrl)
                    .timeout(TIMEOUT_MILLS)
                    .header("User-Agent", RandomUA.generate())
                    .cookies(cookies)
                    .referrer(referrer)
                    .get();
            Elements elContent = document.select(this.rule.getChapter().getContent());
            sb.append(elContent.html());
            // 章节不分页，只请求一次
            if (!isPaging) break;

            Elements elNextPage = document.select(this.rule.getChapter().getNextPage());
            // 章节最后一页 TODO 此处容易出错，先标记
            if (elNextPage.text().contains("下一章")) break;

            String href = elNextPage.attr("href");
            nextUrl = CrawlUtils.normalizeUrl(href, this.rule.getUrl());
            // 随机爬取间隔，建议重试间隔稍微长一点
            CrawlUtils.randomSleep(isRetry ? config.getRetryMinInterval() : config.getMinInterval(), isRetry ? config.getRetryMaxInterval() : config.getMaxInterval());
        } while (true);

        return sb.toString();
    }


}