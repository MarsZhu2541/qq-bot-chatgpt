package com.mars.qqbot.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
    String tkEpId;
    boolean isThinking = false;

    public VolcEngineServiceImpl(
            @Value("${volc.secret_key}")
            String secretKey,
            @Value("${volc.access_key}")
            String accessKey,
            @Value("${volc.ep_id}")
            String epId,
            @Value("${volc.thinking_ep_id}")
            String tkEpId) {
        visualService.setAccessKey(accessKey);
        visualService.setSecretKey(secretKey);
        service = ArkService.builder().ak(accessKey).sk(secretKey)
                            .baseUrl("https://ark.cn-beijing.volces.com/api/v3/").region("cn-beijing").build();
        this.epId = epId;
        this.tkEpId = tkEpId;
    }

    @Override
    public String chat(List<ChatMessage> messages) {
        chatCompletionRequest = ChatCompletionRequest.builder()
                                                     .model(isThinking ? tkEpId : epId)
                                                     .messages(messages)
                                                     .build();
        final ChatMessage message = service.createChatCompletion(chatCompletionRequest).getChoices().get(0)
                                           .getMessage();
        final String reasoningContent = message.getReasoningContent();
        final String content = message.stringContent();
        if (isThinking) {
            return "\n------思考过程:------\n" + reasoningContent + "\n\n------最终回答:------\n" + content;
        }
        return content;
    }

    public String swichThinking() {
        isThinking =! isThinking;
        return isThinking ? "深度思考已开启" : "深度思考已关闭";
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
        req.put("req_key", "img2img_real_mix_style_usage");
        req.put("prompt", msg);
        req.put("model_version", "general_v2.0_L");
        req.put("return_url", true);
        String jsonString = "";
        Data data = null;
        try {
            jsonString = JSON.toJSONString(visualService.cvProcess(req));
            log.info("jsonString: {}", jsonString);
            CvProcess cvProcess = JSON.parseObject(jsonString, CvProcess.class);
            data = cvProcess.getData();
            return new QqMedia(new Media(MediaType.IMAGE, data.getImage_urls().get(0), false),
                               data.getLlm_result());
        } catch (Exception e) {
            log.error("Create media failed", e);
            throw new RuntimeException(e);
        }
    }
}
