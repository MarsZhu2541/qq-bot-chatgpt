package com.mars.qqbot.service.impl;

import java.util.List;

import com.mars.qqbot.model.QqMedia;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VolcEngineServiceImplTest {

    @Test
    void getMedia() {
        final VolcEngineServiceImpl volc = new VolcEngineServiceImpl("", "", "", "");
        System.out.printf(volc.swichThinking());
        final String chat = volc.chat(List.of(ChatMessage.builder().role(
                ChatMessageRole.USER).content("你好").build()));
        System.out.println(chat);
    }
}
