package com.mars.qqbot.service.impl;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mars.foundation.model.Media;
import com.mars.foundation.model.MediaType;
import com.mars.qqbot.model.QqMedia;
import com.mars.qqbot.model.volc.CvProcess;
import com.mars.qqbot.model.volc.Data;
import com.mars.qqbot.service.ChatGPTService;
import com.mars.qqbot.service.Text2MediaService;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import com.volcengine.service.visual.IVisualService;
import com.volcengine.service.visual.impl.VisualServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class VolcEngineServiceImpl implements ChatGPTService<ChatMessage>, Text2MediaService {
    IVisualService visualService = VisualServiceImpl.getInstance();
    ArkService service;
    ChatCompletionRequest chatCompletionRequest;
    String epId;

    public VolcEngineServiceImpl(@Value("${volc.secret_key}") String secretKey, @Value("${volc.access_key}") String accessKey,
                                 @Value("${volc.ep_id}") String epId) {
        visualService.setAccessKey(accessKey);
        visualService.setSecretKey(secretKey);
        service = ArkService.builder().ak(accessKey).sk(secretKey)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3/").region("cn-beijing").build();
        this.epId = epId;
    }

    @Override
    public String chat(List<ChatMessage> messages) {
        chatCompletionRequest = ChatCompletionRequest.builder()
                .model(epId)
                .messages(messages)
                .build();
        return service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().stringContent();
    }


    @Override
    public void chatStream(String message) {
    }

    @Override
    public ChatMessage createUserMessage(String message) {
        return ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
    }

    @Override
    public ChatMessage createAssistantMessage(String message) {
        return ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(message).build();
    }

    @Override
    public boolean isUserMessage(ChatMessage message) {
        return (ChatMessageRole.USER.equals(message.getRole()));
    }

    @Override
    public QqMedia getMedia(String msg) {
        JSONObject req = new JSONObject();
        req.put("req_key", "high_aes_general_v20_L");
        req.put("prompt", msg);
        req.put("model_version", "general_v2.0_L");
        req.put("return_url", true);
        String jsonString = "";
        Data data =null;
        try {
            jsonString = JSON.toJSONString(visualService.cvProcess(req));
            CvProcess cvProcess = JSON.parseObject(jsonString, CvProcess.class);
            data = cvProcess.getData();
            return new QqMedia(new Media(MediaType.IMAGE, data.getImage_urls().get(0), false),
                    data.getLlm_result());
        } catch (Exception e) {
            log.info(jsonString);
            return new QqMedia(new Media(MediaType.IMAGE, data.getImage_urls().get(0), false), " ");
        }
    }
}
