package com.mars.qqbot.eventAck;

import com.mars.exception.QqBotException;
import com.mars.model.*;
import com.mars.qqbot.model.QqMedia;
import com.mars.qqbot.service.Text2MediaService;
import com.mars.qqbot.service.impl.*;
import com.mars.qqbot.util.ChatServiceProxy;
import com.mars.service.EventAckService;
import com.mars.service.QqOpenApiService;
import com.mars.util.RepeatPushHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Service
@Slf4j
public class EventAckServiceImpl implements EventAckService {

    @Autowired
    private VolcEngineServiceImpl volcEngineService;

    @Autowired
    private ChatGPTServiceImpl chatGPTService;

    @Autowired
    private SparkServiceImpl sparkService;

    @Autowired
    private ZhiPuServiceImpl zhiPuService;

    @Autowired
    private QwenServiceImpl qwenService;

    @Autowired
    private QqOpenApiService qqOpenApiService;

    @Autowired
    private BaiduImageServiceImpl baiduImageService;

    private HashMap<String, ChatServiceProxy<?>> chatServiceProxyMap = new HashMap<>();
    private HashMap<String, Text2MediaService> text2MediaServiceHashMap = new HashMap<>();

    private RepeatPushHelper repeatPushHelper = new RepeatPushHelper(10);

    private String currentMode = "/讯飞星火对话";

    @PostConstruct
    public void setUp() {
        chatServiceProxyMap.put("/豆包对话", new ChatServiceProxy<>(volcEngineService));
        chatServiceProxyMap.put("/ChatGPT对话", new ChatServiceProxy<>(chatGPTService));
        chatServiceProxyMap.put("/讯飞星火对话", new ChatServiceProxy<>(sparkService));
        chatServiceProxyMap.put("/智谱对话", new ChatServiceProxy<>(zhiPuService));
        chatServiceProxyMap.put("/通义千问对话", new ChatServiceProxy<>(qwenService));
        text2MediaServiceHashMap.put("/百度搜图", baiduImageService);
    }

    @SneakyThrows
    @Override
    public void handleEvent(QqWebhookEvent webhookEvent) {
        Event event = webhookEvent.getD();
        if (repeatPushHelper.isRepeat(event.getId())) {
            log.info("Message {} has already respond", event.getContent());
            return;
        }
        Message.MessageBuilder builder = new Message.MessageBuilder().eventId(webhookEvent.getId())
                .msgId(event.getId()).msgType(MessageType.TEXT);
        try {
            switch (webhookEvent.getT()) {
                case GROUP_AT_MESSAGE_CREATE:
                    builder = handleMessageBuilder(event.getGroup_openid(), event.getContent(), builder);
            }
        } catch (QqBotException e) {
            builder.media(null).content("出错了，请联系管理员qq2541884980\n" + e.getMessage()
                    + "\nError code: " + e.getErr_code() + "\nTrace id: " + e.getTrace_id());
            log.error("Create message failed", e);
        }catch (RuntimeException e) {
            builder.media(null).content("出错了，请联系管理员qq2541884980\n");
            log.error("Create message failed", e);
        } finally {
            qqOpenApiService.sendGroupMessage(event.getGroup_openid(), builder.build());
            repeatPushHelper.put(event.getId());
        }
    }

    private Message.MessageBuilder handleMessageBuilder(String groupId, String msg, Message.MessageBuilder builder) {

        String content = "";
        // need change mod
        if (chatServiceProxyMap.containsKey(msg.trim()) || text2MediaServiceHashMap.containsKey(msg.trim())) {
            currentMode = msg.trim();
            content = "已切换到" + currentMode.replace("/", "") + "模式";
            builder.content(content);
            return builder;
        }

        // need text to text
        if (chatServiceProxyMap.containsKey(currentMode)) {
            content = chatServiceProxyMap.get(currentMode).chat(msg);
            builder.content(content);
            return builder;
        }

        // need text to media
        QqMedia media = text2MediaServiceHashMap.get(currentMode).getMedia(msg);
        ResponseEntity<UploadedMedia> uploadedMedia = qqOpenApiService.uploadMedia(groupId, media.getMedia());
        log.info("upload file successfully, {}", media.getTitle());

        builder.msgType(MessageType.MEDIA).media(uploadedMedia.getBody()).content(media.getTitle());
        return builder;
    }
}
