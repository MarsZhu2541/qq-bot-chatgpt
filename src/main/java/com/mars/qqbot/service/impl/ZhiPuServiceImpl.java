package com.mars.qqbot.service.impl;


import com.mars.qqbot.service.ChatGPTService;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.api.VideosClientApiService;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import com.zhipu.oapi.service.v4.videos.VideoCreateParams;
import com.zhipu.oapi.service.v4.videos.VideosResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class ZhiPuServiceImpl implements ChatGPTService<ChatMessage> {

    private static ClientV4 client;

    public ZhiPuServiceImpl(@Value("${zhipu.api_key}") String api_key) {
        client = new ClientV4.Builder(api_key)
                .networkConfig(1,1,1,1, TimeUnit.MINUTES)
                .enableTokenCache().build();
    }

    @Override
    public String chat(List<ChatMessage> messages) {
        String requestId = "mars-" + System.currentTimeMillis();

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
    }

    @Override
    public void chatStream(String message) {

    }

    @Override
    public ChatMessage createUserMessage(String message) {
        return new ChatMessage(ChatMessageRole.USER.value(), message);
    }

    @Override
    public ChatMessage createAssistantMessage(String message) {
        return new ChatMessage(ChatMessageRole.ASSISTANT.value(), message);
    }

    @Override
    public boolean isUserMessage(ChatMessage message) {
        return ChatMessageRole.USER.value().equals(message.getRole());
    }

//    @Override
//    public ImageMessage getImage(Group subject, String keyword) {
//        String requestId = "mars-" + System.currentTimeMillis();
//
//        VideosClientApiService videosClientApiService = new VideosClientApiService(
//                client.getConfig().getHttpClient(), "https://open.bigmodel.cn/api/paas/v4/");
//        VideoCreateParams videoCreateParams = VideoCreateParams.builder()
//                .model("cogvideox")
//                .prompt(keyword)
//                .requestId(requestId)
//                .build();
//        VideosClientApiService.VideoGenerationChain task = videosClientApiService.videoGenerations(videoCreateParams);
//        VideosResponse response = task.apply(client);
//        videoCreateParams.setId(response.getData().getId());
//        log.info("task id: {}", response.getData().getId());
//        VideosClientApiService.VideoGenerationChain result = videosClientApiService.videoGenerationsResult(videoCreateParams);
//        VideosResponse videosResponse = result.apply(client);
//        String status = videosResponse.getData().getTaskStatus();
//        try {
//            while ("PROCESSING".equals(status)) {
//                log.info("视频生成中, " + response.getMsg());
//                Thread.sleep(5000L);
//                videosResponse = result.apply(client);
//                status = videosResponse.getData().getTaskStatus();
//            }
//            if ("FAIL".equals(status)) {
//                log.info("视频生成失败, " + response.getMsg());
//                throw new RuntimeException("视频生成失败: " + response.getMsg());
//            }
//
//            URL coverImageUrl = new URL(videosResponse.getData().getVideoResult().get(0).getCoverImageUrl());
//            String url = videosResponse.getData().getVideoResult().get(0).getUrl();
//            log.info("视频生成成功, " + response.getMsg());
//            return new ImageMessage(ExternalResource.uploadAsImage(coverImageUrl.openStream(), subject), "视频链接：\n" + url);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
