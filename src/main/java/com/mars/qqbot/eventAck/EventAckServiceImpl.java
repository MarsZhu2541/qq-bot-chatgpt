package com.mars.qqbot.eventAck;

import cn.hutool.core.lang.ConsoleTable;
import com.mars.foundation.exception.QqBotException;
import com.mars.foundation.model.QqWebhookEvent;
import com.mars.foundation.model.*;
import com.mars.novel.model.Book;
import com.mars.novel.model.Chapter;
import com.mars.novel.model.Rule;
import com.mars.novel.model.SearchResult;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private HashMap<String, ChatServiceProxy<?>> chatServiceProxyMap = new HashMap<>();
    private HashMap<String, Text2MediaService> text2MediaServiceHashMap = new HashMap<>();
    private List<String> novelCommand = List.of("/查看书源", "/切换书源", "/小说搜索", "/选择小说", "/选择章节");

    private RepeatPushHelper repeatPushHelper = new RepeatPushHelper(10);

    private String currentMode = "/讯飞星火对话";

    private final ThreadLocal<Message.MessageBuilder> builder = new ThreadLocal<>();

    @PostConstruct
    public void setUp() {
        chatServiceProxyMap.put("/豆包对话", new ChatServiceProxy<>(volcEngineService));
        chatServiceProxyMap.put("/ChatGPT对话", new ChatServiceProxy<>(chatGPTService));
        chatServiceProxyMap.put("/讯飞星火对话", new ChatServiceProxy<>(sparkService));
        chatServiceProxyMap.put("/智谱对话", new ChatServiceProxy<>(zhiPuService));
        chatServiceProxyMap.put("/通义千问对话", new ChatServiceProxy<>(qwenService));
        text2MediaServiceHashMap.put("/百度搜图", baiduImageService);
        text2MediaServiceHashMap.put("/豆包文生图", volcEngineService);
    }

    @SneakyThrows
    @Override
    public void handleEvent(QqWebhookEvent webhookEvent) {
        Event event = webhookEvent.getD();
        if (repeatPushHelper.isRepeat(event.getId())) {
            log.info("Message {} has already respond", event.getContent());
            return;
        }
        builder.set(new Message.MessageBuilder().eventId(webhookEvent.getId())
                .msgId(event.getId()).msgType(MessageType.TEXT));
        try {
            switch (webhookEvent.getT()) {
                case GROUP_AT_MESSAGE_CREATE:
                    handleMessageBuilder(event.getGroup_openid(), event.getContent());
            }
        } catch (QqBotException e) {
            builder.get().media(null).content("出错了，请联系管理员qq2541884980\n" + e.getMessage().replace(".", "_"));
            log.error("Create message failed" + e.getMessage(), e);
        } catch (RuntimeException e) {
            builder.get().media(null).content("出错了，请联系管理员qq2541884980\n");
            log.error("Create message failed", e);
        } finally {
            qqOpenApiService.sendGroupMessage(event.getGroup_openid(), builder.get().build());
            repeatPushHelper.put(event.getId());
            builder.remove();
        }
    }

    private void handleMessageBuilder(String groupId, String msg) {

        String content;
        if (msg.isBlank()) {
            content = EMPTY_MESSAGE;
        } else if (novelCommand.stream().anyMatch(msg.trim()::contains)) {
            content = handleNovelRequest(groupId, msg.trim());
        }
        // need change mod
        else if (chatServiceProxyMap.containsKey(msg.trim()) || text2MediaServiceHashMap.containsKey(msg.trim())) {
            currentMode = msg.trim();
            content = "已切换到" + currentMode.replace("/", "") + "模式";
        }
        // need text to text
        else if (chatServiceProxyMap.containsKey(currentMode)) {
            content = chatServiceProxyMap.get(currentMode).chat(msg);
        }
        // need text to image
        else {
            sendMediaMessage(groupId, msg, builder.get());
            content = IMAGE_HANDLING;
        }
        log.info("Ready to send content: {}",content);
        content = removeUrls(content);
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
            for (int i = 0; i < Rule.initRules().size(); i++) {
                Rule rule = Rule.initRules().get(i);
                sb.append(i + 1).append(". ").append(rule.getName() + " " + rule.getComment() + "\n");
            }
            return "书源列表： \n" + sb;
        } else if (switchMatcher.find()) {
            int i = Integer.parseInt(switchMatcher.group(1));
            novelService.setSourceId(i - 1);
            return "已切换到书源： " + Rule.initRules().get(i - 1).getName();
        } else if (searchMatcher.find()) {
            return novelService.searchBooks(searchMatcher.group(1));
        } else if (selectNovel.find()) {
            sendNovelMessage(groupId, Integer.parseInt(selectNovel.group(1)), builder.get());
            return NOVEL_LOADING;
        } else if (selectChapter.find()) {
            Chapter chapter = novelService.getChapterContent(Integer.parseInt(selectChapter.group(1)));
            List<String> contentMessages = chapter.getContentMessages();
            for (int i = 0; i < contentMessages.size() - 1; i++) {
                if (i == 0) {
                    builder.get().msgSeq(100 + i);
                    builder.get().media(null).content(chapter.getTitle() + "\n" + contentMessages.get(i)).msgType(MessageType.TEXT);
                    qqOpenApiService.sendGroupMessage(groupId, builder.get().build());
                }
            }
            return contentMessages.get(contentMessages.size() - 1);
        }
        return "你进入了未知领域";
    }


    private void sendMediaMessage(String groupId, String msg, Message.MessageBuilder messageBuilder) {
        new Thread(() -> {
            try {
                messageBuilder.msgSeq(2);
                QqMedia media = text2MediaServiceHashMap.get(currentMode).getMedia(msg);
                ResponseEntity<UploadedMedia> uploadedMedia = qqOpenApiService.uploadMedia(groupId, media.getMedia());
                log.info("upload file successfully, {}", media.getTitle());
                messageBuilder.msgType(MessageType.MEDIA).media(uploadedMedia.getBody()).content(media.getTitle());
            } catch (RuntimeException e) {
                messageBuilder.media(null).content("出错了，请联系管理员qq2541884980\n");
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
                messageBuilder.msgSeq(2);
                ResponseEntity<UploadedMedia> uploadedMedia = qqOpenApiService.uploadMedia(groupId,
                        new Media(MediaType.IMAGE, book.getCoverUrl(), false));
                log.info("upload book cover successfully, {}", book.getBookName());
                messageBuilder.msgType(MessageType.MEDIA).media(uploadedMedia.getBody()).content(book.toString());
                qqOpenApiService.sendGroupMessage(groupId, messageBuilder.build());

                for (int seq = 0; seq < book.getCatalogMessages().size(); seq++) {

                    messageBuilder.msgSeq(100 + seq);
                    messageBuilder.media(null).content(book.getCatalogMessages().get(seq)).msgType(MessageType.TEXT);
                    qqOpenApiService.sendGroupMessage(groupId, messageBuilder.build());
                }
            } catch (RuntimeException e) {
                messageBuilder.media(null).content("出错了，请联系管理员qq2541884980\n").msgType(MessageType.TEXT);
                log.error("Create message failed", e);
                qqOpenApiService.sendGroupMessage(groupId, messageBuilder.build());
            }
        }).start();
    }
}
