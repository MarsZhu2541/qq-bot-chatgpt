package com.mars.qqbot.util;

import com.mars.qqbot.service.ChatGPTService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ChatServiceProxy<T> {

    private ChatGPTService<T> realService;

    protected final List<T> messages = new ArrayList<>();

    private static final int MAX = 20;
    private static final int TO_REMOVE = 4;

    private void messagesSizeCheck() {
        if (messages.size() >= MAX) {
            messages.subList(0, TO_REMOVE).clear();
        }
    }

    protected void beforeChat(T message) {
        messagesSizeCheck();
        if(!messages.isEmpty()){
            waitLastChatFinished();
        }
        messages.add(message);
    }

    private synchronized void waitLastChatFinished() {
        while (realService.isUserMessage(messages.get(messages.size() - 1))) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    protected void afterChat(T message) {
        messages.add(message);
    }

    public ChatServiceProxy(ChatGPTService<T> realService) {
        this.realService = realService;
    }

    public void setRealService(ChatGPTService<T> realService) {
        this.realService = realService;
    }

    public String chat(String message) {

        try {
            log.info("Received chat message: {} ", message);
            beforeChat(createUserMessage(message));
            String answer = realService.chat(messages);
            log.info("Answered chat message: {} ", answer);
            afterChat(createAssistantMessage(answer));
            return answer;
        } catch (Exception e) {
            messages.clear();
            log.error("Chat error", e);
            return "出错了，上下文已清除, "+ e.getMessage();
        }
    }

    public T createUserMessage(String message) {
        return realService.createUserMessage(message);
    }

    public T createAssistantMessage(String message) {
        return realService.createAssistantMessage(message);
    }

}
