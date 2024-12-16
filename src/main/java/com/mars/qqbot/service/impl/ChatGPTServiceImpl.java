package com.mars.qqbot.service.impl;

import com.mars.qqbot.service.ChatGPTService;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.ChatGPTStream;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;


@Service
public class ChatGPTServiceImpl implements ChatGPTService<Message> {

    @Value("${openai.secret_key}")
    private List<String> token;
    ChatGPT chatGPT;

    @PostConstruct
    public void setUp() {
        chatGPT = ChatGPT.builder()
//                .proxy(Proxys.http(proxyIp, proxyPort))
                .apiKeyList(token)
                .timeout(900)
                .apiHost("https://api.gpts.vin") //反向代理地址
                .build()
                .init();
    }


    @Override
    public String chat(List<Message> messages) {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(ChatCompletion.Model.GPT_3_5_TURBO_0613.getName())
                .messages(messages)
                .temperature(0.9)
                .build();

        ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);
        return response.getChoices().get(0).getMessage().getContent();
    }

    @Override
    public void chatStream(String message) {

        ChatGPTStream chatGPTStream = ChatGPTStream.builder()
                .timeout(600)
                .apiKeyList(token)
//                .proxy(Proxys.http(proxyIp, proxyPort))
                .apiHost("https://api.openai-forward.com/")
                .build()
                .init();

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(ChatCompletion.Model.GPT_3_5_TURBO_0613.getName())
                .messages(Arrays.asList(Message.of(message)))
                .build();
    }

    @Override
    public Message createUserMessage(String message) {
        return Message.of(message);
    }

    @Override
    public Message createAssistantMessage(String message) {
        return Message.ofAssistant(message);
    }

    @Override
    public boolean isUserMessage(Message message) {
        return "user".equals(message.getRole());
    }
}
