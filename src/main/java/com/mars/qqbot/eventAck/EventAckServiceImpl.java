package com.mars.qqbot.eventAck;

import com.mars.deltaforce.exception.PlayerNotLogin;
import com.mars.deltaforce.model.MapPassword;
import com.mars.deltaforce.service.DeltaForceService;
import com.mars.foundation.exception.QqBotException;
import com.mars.foundation.model.QqWebhookEvent;
import com.mars.foundation.model.*;
import com.mars.foundation.util.HandlingState;
import com.mars.novel.model.Book;
import com.mars.novel.model.Chapter;
import com.mars.novel.model.Rule;
import com.mars.novel.service.NovelServiceImpl;
import com.mars.qqbot.model.QqMedia;
import com.mars.qqbot.service.Text2MediaService;
import com.mars.qqbot.service.impl.*;
import com.mars.qqbot.util.ChatServiceProxy;
import com.mars.foundation.service.EventAckService;
import com.mars.foundation.service.QqOpenApiService;
import com.mars.foundation.util.RepeatPushHelper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mars.deltaforce.service.DeltaForceService.NOT_FOUND;

@Service
@Slf4j
public class EventAckServiceImpl implements EventAckService {

    public static final String IMAGE_HANDLING = "图像处理中，很快就好 \n⌯ᵔ⤙ᵔ⌯ಣ";
    public static final String NOVEL_LOADING = "小说加载中，很快就好 \n⌯ᵔ⤙ᵔ⌯ಣ";
    public static final String EMPTY_MESSAGE = "哑巴说话呀！\n⁽⁽(੭ꐦ •̀Д•́ )੭*⁾⁾";
    @Autowired
    private VolcEngineServiceImpl volcEngineService;

    @Autowired
    private ChatGPTServiceImpl chatGPTService;

    @Autowired
    private DeepSeekServiceImpl deepSeekService;

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

    @Autowired
    private NovelServiceImpl novelService;

    @Autowired
    private DeltaForceService deltaForceService;

    private HashMap<String, ChatServiceProxy<?>> chatServiceProxyMap = new HashMap<>();
    private HashMap<String, Text2MediaService> text2MediaServiceHashMap = new HashMap<>();
    private List<String> novelCommand = List.of("/查看书源", "/切换书源", "/小说搜索", "/选择小说", "/选择章节");

    private static RepeatPushHelper repeatPushHelper = new RepeatPushHelper(10);

    private String currentMode = "/豆包对话";

    private final ThreadLocal<Message.MessageBuilder> builder = new ThreadLocal<>();

    private int seqStart = 0;

    @PostConstruct
    public void setUp() {
        chatServiceProxyMap.put("/豆包对话", new ChatServiceProxy<>(volcEngineService));
        chatServiceProxyMap.put("/开关深度思考", new ChatServiceProxy<>(volcEngineService));
        chatServiceProxyMap.put("/ChatGPT对话", new ChatServiceProxy<>(chatGPTService));
        chatServiceProxyMap.put("/DeepSeek对话", new ChatServiceProxy<>(deepSeekService));
        chatServiceProxyMap.put("/讯飞星火对话", new ChatServiceProxy<>(sparkService));
        chatServiceProxyMap.put("/智谱对话", new ChatServiceProxy<>(zhiPuService));
        chatServiceProxyMap.put("/通义千问对话", new ChatServiceProxy<>(qwenService));
        text2MediaServiceHashMap.put("/百度搜图", baiduImageService);
        text2MediaServiceHashMap.put("/豆包文生图", volcEngineService);
        text2MediaServiceHashMap.put("/通义万相文生图", qwenService);
    }

    @SneakyThrows
    @Override
    public void handleEvent(QqWebhookEvent webhookEvent) {
        Event event = webhookEvent.getD();
        int count = 0;
        if (repeatPushHelper.isRepeat(event.getId())) {

            while (repeatPushHelper.isInProgress(event.getId())) {
                log.info("Message {} is in progress, need wait", event.getContent());
                TimeUnit.SECONDS.sleep(5);
            }
            //will become to done or error
            if (repeatPushHelper.isDone(event.getId())) {
                log.info("Message {} has already respond", event.getContent());
                return;
            }
            //error should try again
        }
        builder.set(new Message.MessageBuilder().msgSeq(seqStart++).eventId(webhookEvent.getId())
                .msgId(event.getId()).msgType(MessageType.TEXT));
        repeatPushHelper.put(event.getId(), HandlingState.IN_PROGRESS);
        try {
            switch (webhookEvent.getT()) {
                case GROUP_AT_MESSAGE_CREATE:
                    handleMessageBuilder(event);
            }
            qqOpenApiService.sendGroupMessage(event.getGroup_openid(), builder.get().build());
            repeatPushHelper.put(event.getId(), HandlingState.DONE);
        } catch (QqBotException e) {
            builder.get().msgSeq(seqStart++).media(null).content(
                    "出错了，请联系管理员qq2541884980\n" + e.getMessage().replace(".", "_"));
            log.error("Create message failed" + e.getMessage(), e);
            qqOpenApiService.sendGroupMessage(event.getGroup_openid(), builder.get().build());
            repeatPushHelper.put(event.getId(), HandlingState.ERROR);
        } catch (Exception e) {
            builder.get().msgSeq(seqStart++).media(null).content("出错了，请联系管理员qq2541884980\n");
            log.error("Create message failed", e);
            qqOpenApiService.sendGroupMessage(event.getGroup_openid(), builder.get().build());
            repeatPushHelper.put(event.getId(), HandlingState.ERROR);
        } finally {
            builder.remove();
            seqStart = 0;
        }
    }

    private void handleMessageBuilder(Event event) {
        String groupId = event.getGroup_openid();
        String msg = event.getContent();
        String authorId = event.getAuthor().getMember_openid();
        try {
            if (msg.trim().contains("/三角洲玩家信息")) {
                QqMedia playerData = deltaForceService.getPlayerData(groupId, authorId, builder.get());
                ResponseEntity<UploadedMedia> uploadedMedia = qqOpenApiService.uploadMedia(groupId, playerData.getMedia());
                builder.get().msgType(MessageType.MEDIA).media(uploadedMedia.getBody()).
                        content(playerData.getTitle());
            } else if (msg.trim().contains("/三角洲玩家成就")) {
                String achievement = deltaForceService.getAchievement(authorId);
                log.info("achievement: {}", achievement);
                builder.get().msgType(MessageType.TEXT).media(null).content(achievement);
            } else {
                handleMessageBuilder(groupId, msg);
            }
        } catch (PlayerNotLogin e) {
            QqMedia qrCode = deltaForceService.getLoginQrCode(groupId, authorId, builder.get());
            ResponseEntity<UploadedMedia> uploadedMedia = qqOpenApiService.uploadMedia(groupId, qrCode.getMedia());
            builder.get().msgType(MessageType.MEDIA).media(uploadedMedia.getBody()).
                    content(qrCode.getTitle());
        } catch (RuntimeException e) {
            log.error("Unexpected error", e);
            builder.get().msgType(MessageType.TEXT).media(null)
                    .content(e.getMessage().replace(".", " "));
        }
    }

    private void handleMessageBuilder(String groupId, String msg) {

        String content;
        if (msg.isBlank()) {
            content = EMPTY_MESSAGE;
        } else if (novelCommand.stream().anyMatch(msg.trim()::contains)) {
            content = handleNovelRequest(groupId, msg.trim()).replace(".", "_");
            ;
        } else if (msg.trim().contains("/开关深度思考")) {
            content = volcEngineService.swichThinking();
        }
        // need change mode
        else if (chatServiceProxyMap.containsKey(msg.trim()) || text2MediaServiceHashMap.containsKey(msg.trim())) {
            currentMode = msg.trim();
            content = "已切换到" + currentMode.replace("/", "") + "模式";
        } else if (msg.trim().contains("/三角洲今日密码")) {
            content = deltaForceService.getMapPassword().toString();
        } else if (msg.trim().contains("/三角洲实时价格")) {
            Matcher searchMatcher = Pattern.compile("/三角洲实时价格\\s*(.*)").matcher(msg);
            if (searchMatcher.find()) {
                content = deltaForceService.searchPossibleItemPrice(searchMatcher.group(1));
            } else {
                content = NOT_FOUND;
            }
        }
        // need text to text
        else if (chatServiceProxyMap.containsKey(currentMode)) {
            content = chatServiceProxyMap.get(currentMode).chat(msg);
        }
        // need text to image
        else {
            QqMedia media = text2MediaServiceHashMap.get(currentMode).getMedia(msg);
            sendMediaMessage(media, groupId, builder.get(), seqStart);
            content = IMAGE_HANDLING;
        }
        log.info("Ready to send content: {}", content);
        builder.get().content(content);
    }

    public static String removeUrls(String input) {
        // 正则表达式匹配WWW.xxx.xxx或者xxx.xxx形式的URL
        String regex = "\\b(www\\.[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b";
        return input.replaceAll(regex, "");
    }

    private String handleNovelRequest(String groupId, String msg) {
        Matcher searchMatcher = Pattern.compile("/小说搜索\\s*(.*)").matcher(msg);
        Matcher switchMatcher = Pattern.compile("/切换书源\\s*(\\d+)").matcher(msg);
        Matcher selectNovel = Pattern.compile("/选择小说\\s*(\\d+)").matcher(msg);
        Matcher selectChapter = Pattern.compile("/选择章节\\s*(\\d+)").matcher(msg);

        if (msg.startsWith("/查看书源")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < NovelServiceImpl.rules.size(); i++) {
                Rule rule = NovelServiceImpl.rules.get(i);
                sb.append(i + 1).append(". ").append(rule.getName()).append(" ").append(rule.getComment()).append("\n");
            }
            return "书源列表： \n" + sb;
        } else if (switchMatcher.find()) {
            int i = Integer.parseInt(switchMatcher.group(1));
            novelService.setSourceId(i - 1);
            return "已切换到书源： " + NovelServiceImpl.rules.get(i - 1).getName();
        } else if (searchMatcher.find()) {
            return novelService.searchBooks(searchMatcher.group(1));
        } else if (selectNovel.find()) {
            sendNovelMessage(groupId, Integer.parseInt(selectNovel.group(1)), builder.get());
            return NOVEL_LOADING;
        } else if (selectChapter.find()) {
            Chapter chapter = novelService.getChapterContent(Integer.parseInt(selectChapter.group(1)));
            List<String> contentMessages = chapter.getContentMessages();
            for (int i = 0; i < contentMessages.size() - 1; i++) {
                builder.get().msgSeq(seqStart++);
                builder.get().media(null).content(chapter.getTitle() + "\n" + contentMessages.get(i)).msgType(
                        MessageType.TEXT);
                qqOpenApiService.sendGroupMessage(groupId, builder.get().build());
            }
            builder.get().msgSeq(seqStart++);
            return contentMessages.get(contentMessages.size() - 1);
        }
        return "你进入了未知领域";
    }

    private void sendMediaMessage(QqMedia media, String groupId, Message.MessageBuilder messageBuilder, int seqStart) {
        new Thread(() -> {
            int seqStart1 = seqStart;
            try {
                messageBuilder.msgSeq(seqStart1++);
                ResponseEntity<UploadedMedia> uploadedMedia = qqOpenApiService.uploadMedia(groupId, media.getMedia());
                log.info("upload file successfully, {}", media.getTitle());
                messageBuilder.msgType(MessageType.MEDIA).media(uploadedMedia.getBody()).content(media.getTitle());
            } catch (RuntimeException e) {
                messageBuilder.msgSeq(seqStart1++);
                messageBuilder.media(null).content(
                        "出错了，请联系管理员qq2541884980\n" + e.getLocalizedMessage().replace(".", "_"));
                log.error("Create message failed", e);
            } finally {
                qqOpenApiService.sendGroupMessage(groupId, messageBuilder.build());
            }
        }).start();
    }

    private void sendNovelMessage(String groupId, int index, Message.MessageBuilder messageBuilder) {
        new Thread(() -> {
            try {
                Book book = novelService.getBookDetail(index - 1);
                messageBuilder.msgSeq(100 + seqStart++);
                ResponseEntity<UploadedMedia> uploadedMedia = qqOpenApiService.uploadMedia(groupId,
                        new Media(MediaType.IMAGE,
                                book.getCoverUrl(),
                                false));
                log.info("upload book cover successfully, {}", book.getBookName());
                messageBuilder.msgType(MessageType.MEDIA).media(uploadedMedia.getBody()).content(book.toString());
                qqOpenApiService.sendGroupMessage(groupId, messageBuilder.build());

                messageBuilder.msgSeq(100 + seqStart++);
                messageBuilder.media(null).content(book.getLatestCatalog()).msgType(MessageType.TEXT);
                qqOpenApiService.sendGroupMessage(groupId, messageBuilder.build());
            } catch (RuntimeException e) {
                messageBuilder.msgSeq(100 + seqStart++).media(null).content("出错了，请联系管理员qq2541884980\n")
                        .msgType(MessageType.TEXT);
                log.error("Create message failed", e);
                qqOpenApiService.sendGroupMessage(groupId, messageBuilder.build());
            }
        }).start();
    }
}
