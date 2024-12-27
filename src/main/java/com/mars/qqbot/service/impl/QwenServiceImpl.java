package com.mars.qqbot.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.mars.foundation.model.Media;
import com.mars.foundation.model.MediaType;
import com.mars.qqbot.model.QqMedia;
import com.mars.qqbot.service.ChatGPTService;
import com.mars.qqbot.service.Text2MediaService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class QwenServiceImpl implements ChatGPTService<Message>, Text2MediaService {

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
    public QqMedia getMedia(final String msg) {
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                                   .apiKey(apiKey)
                                   .model("wanx2.1-t2i-turbo")
                                   .prompt(msg)
                                   .n(1)
                                   .size("1024*1024")
                                   .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            log.info("---sync call, please wait a moment----");
            result = imageSynthesis.call(param);
            final Map<String, String> resultMap = result.getOutput().getResults().get(0);
            return new QqMedia(new Media(MediaType.IMAGE, resultMap.get("url"), false), resultMap.get("actual_prompt"));
        } catch (ApiException | NoApiKeyException e){
            log.error("Invoke Ali text2Media failed", e);
            throw new RuntimeException(e.getMessage());
        }
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
