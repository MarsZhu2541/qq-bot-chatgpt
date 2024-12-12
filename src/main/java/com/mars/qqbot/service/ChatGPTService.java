package com.mars.qqbot.service;


import java.util.List;

public interface ChatGPTService<T> {

    //add user message, get assistant message, add assistant message
    public String chat(List<T> messages);

    public void chatStream(String message);

    T createUserMessage(String message);
    T createAssistantMessage(String message);

    boolean isUserMessage(T message);
}
