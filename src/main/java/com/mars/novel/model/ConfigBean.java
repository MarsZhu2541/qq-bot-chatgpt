package com.mars.novel.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author pcdd
 * Created at 2024/11/30
 */
@Data
@Component
@ConfigurationProperties("novel")
public class ConfigBean {

    private Integer sourceId;

    // crawl
    private Integer threads;
    private Integer minInterval;
    private Integer maxInterval;

    // retry
    private Integer maxRetryAttempts;
    private Integer retryMinInterval;
    private Integer retryMaxInterval;

    private String extName;

}