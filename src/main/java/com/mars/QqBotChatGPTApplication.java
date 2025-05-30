package com.mars;

import com.mars.foundation.config.QqBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@Import(QqBotConfig.class)
@SpringBootApplication
public class QqBotChatGPTApplication {

    public static void main(String[] args) {
        SpringApplication.run(QqBotChatGPTApplication.class, args);
    }

}
