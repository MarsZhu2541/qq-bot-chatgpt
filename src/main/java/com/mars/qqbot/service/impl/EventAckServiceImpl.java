package com.mars.qqbot.service.impl;

import com.mars.model.Event;
import com.mars.model.Message;
import com.mars.model.QqWebhookEvent;
import com.mars.qqbot.util.ChatServiceProxy;
import com.mars.service.EventAckService;
import com.mars.service.QqOpenApiService;
import com.mars.util.RepeatPushHelper;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class EventAckServiceImpl implements EventAckService {

    @Autowired
    private VolcEngineServiceImpl volcEngineService;

    @Autowired
    private QqOpenApiService qqOpenApiService;

    private ChatServiceProxy<ChatMessage> volcChatServiceProxy;

    private RepeatPushHelper repeatPushHelper = new RepeatPushHelper(10);

    @PostConstruct
    public void setUp() {
        volcChatServiceProxy = new ChatServiceProxy<>(volcEngineService);
    }

    @SneakyThrows
    @Override
    public void handleEvent(QqWebhookEvent webhookEvent) {
        Event event = webhookEvent.getD();
        if (repeatPushHelper.isRepeat(event.getId())) {
            log.info("Event {} already has pushed", event.getId());
            return;
        }
        Message.MessageBuilder builder = new Message.MessageBuilder()
                .msgType(0)
                .eventId(webhookEvent.getId())
                .msgId(event.getId());
        try {
            switch (webhookEvent.getT()) {
                case GROUP_AT_MESSAGE_CREATE:
                    builder.content(generateMessageContent(event.getContent()));
            }
        } catch (RuntimeException e) {
            builder.content("出错了，请联系管理员qq2541884980\n" + e.getMessage());
        }
        qqOpenApiService.sendGroupMessage(event.getGroup_openid(), builder.build());
        repeatPushHelper.put(event.getId());
    }

    public String generateMessageContent(String content) {
        return volcChatServiceProxy.chat(content);
    }
}
