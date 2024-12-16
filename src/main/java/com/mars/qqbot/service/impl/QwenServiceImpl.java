package com.mars.qqbot.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.mars.qqbot.service.ChatGPTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QwenServiceImpl implements ChatGPTService<Message> {

    private String apiKey;
    public QwenServiceImpl(@Value("${qwen.api_key}") String apiKey){
        this.apiKey = apiKey;
    }
    @Override
    public String chat(List<Message> messages) {
        Generation gen = new Generation();

        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        try {
            return gen.call(param).getOutput().getChoices().get(0).getMessage().getContent();
        } catch (NoApiKeyException | InputRequiredException | ApiException e) {
            log.error("invoke tong yi api failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void chatStream(String message) {

    }

    @Override
    public Message createUserMessage(String message) {
        return Message.builder().role(Role.USER.getValue()).content(message).build();
    }

    @Override
    public Message createAssistantMessage(String message) {
        return Message.builder().role(Role.ASSISTANT.getValue()).content(message).build();
    }

    @Override
    public boolean isUserMessage(Message message) {
        return message.getRole().equals(Role.USER.getValue());
    }
}
