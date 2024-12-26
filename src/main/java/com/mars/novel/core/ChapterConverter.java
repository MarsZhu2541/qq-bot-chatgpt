package com.mars.novel.core;

import com.mars.novel.model.Chapter;
import com.mars.novel.model.ConfigBean;
import lombok.AllArgsConstructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author pcdd
 * Created at 2024/3/17
 */
@AllArgsConstructor
public class ChapterConverter {

    private final ConfigBean config;

    public Chapter convert(Chapter chapter, String extName) {
        String filteredContent = new ChapterFilter(config.getSourceId()).filter(chapter);
        String content = new ChapterFormatter(config).format(filteredContent);

        if ("txt".equals(extName)) {
            // 全角空格，用于首行缩进
            String ident = "\u3000".repeat(2);
            Matcher matcher = Pattern.compile("<p>(.*?)</p>").matcher(content);
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                result.append(ident)
                        .append(matcher.group(1))
                        .append("\n");
            }

            content = chapter.getTitle() + "\n".repeat(2) + result;
        }

        chapter.setContent(content);
        return chapter;
    }
}
