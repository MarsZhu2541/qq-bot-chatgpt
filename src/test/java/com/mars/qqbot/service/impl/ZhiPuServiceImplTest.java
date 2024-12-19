package com.mars.qqbot.service.impl;

import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZhiPuServiceImplTest {

    @Test
    void chat() {
        String chat = new ZhiPuServiceImpl("").chat(List.of(new ChatMessage(ChatMessageRole.USER.value(), "你好")));
        System.out.println(chat);
    }
}