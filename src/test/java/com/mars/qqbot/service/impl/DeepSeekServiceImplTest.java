package com.mars.qqbot.service.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class DeepSeekServiceImplTest {
    @Test
    void test(){
        DeepSeekServiceImpl deepSeekService = new DeepSeekServiceImpl(List.of(""));
        String chat = deepSeekService.chat(List.of(deepSeekService.createUserMessage(("你是谁"))));
        System.out.println(chat);
    }

}
