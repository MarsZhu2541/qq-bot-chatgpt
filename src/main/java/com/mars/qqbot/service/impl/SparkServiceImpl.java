package com.mars.qqbot.service.impl;

import com.google.gson.Gson;
import com.mars.qqbot.service.ChatGPTService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SparkServiceImpl implements ChatGPTService<SparkServiceImpl.Text> {
    OkHttpClient client = new OkHttpClient().newBuilder().build();

    Gson gson = new Gson();

    private String apiKey;
    private String secretKey;

    public SparkServiceImpl(@Value("${spark.api_key}") String apiKey, @Value("${spark.secret_key}") String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    @Override
    public String chat(List<Text> messages) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, gson.toJson(new SparkRequestBody(messages)));
        Request request = new Request.Builder()
                .url("https://spark-api-open.xf-yun.com/v1/chat/completions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", String.format("Bearer %s:%s", apiKey, secretKey))
                .build();
        try {
            Response response = client.newCall(request).execute();
            return gson.fromJson(response.body().string(), SparkResponse.class).choices.get(0).message.content;
        } catch (IOException e) {
            throw new RuntimeException("Invoke spark api failed: ", e);
        }
    }

    @Override
    public void chatStream(String message) {

    }

    @Override
    public Text createUserMessage(String message) {
        return new Text(message, "user");
    }

    @Override
    public Text createAssistantMessage(String message) {
        return new Text(message, "assistant");
    }

    @Override
    public boolean isUserMessage(Text message) {
        return "user".equals(message.role);
    }

    @Getter
    class SparkRequestBody {
        private final String model = "general";
        private List<Text> messages;

        public SparkRequestBody(List<Text> messages) {
            this.messages = messages;
        }
    }

    class SparkResponse {
        int code;
        int status;
        String sid;
        List<Choice> choices;
    }

    class Choice{
        Text message;
    }

    @AllArgsConstructor
    public class Text {
        String content;
        String role;
    }
}
