package com.mars.deltaforce.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mars.deltaforce.client.DeltaForceClient;
import com.mars.deltaforce.exception.PlayerNotLogin;
import com.mars.deltaforce.model.*;
import com.mars.deltaforce.service.SimpleImageService;
import com.mars.deltaforce.service.SimpleImageUtils;
import com.mars.foundation.model.*;
import com.mars.foundation.service.QqOpenApiService;
import com.mars.qqbot.model.QqMedia;
import feign.Feign;
import feign.Logger;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DeltaForceProvider {

    private static final String baseUrl = "http://localhost:6379";
    private static final String picBaseUrl = "https://playerhub.df.qq.com/playerhub/60004/object/";
    private static final Map<String, AuthParam> tokenMap = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DeltaForceClient client = deltaForceClient();
    private Map<String, String> uid2QqNumber = new HashMap();
    @Autowired
    private QqOpenApiService qqOpenApiService;

    @Autowired
    private SimpleImageService imageService;

    public QqMedia getQrLoginCode(String groupId, String playerId, Message.MessageBuilder builder) {
        DfResponse qrCode = client.getQrCode();
        DfData data = qrCode.getData();
        try {
            final String fileId = imageService.save(data.getImage());
            CompletableFuture.runAsync(() -> {
                try {
                    queryLoginState(fileId, data.getToken(), data.getQrSig(), data.getLoginSig(), data.getCookie(), groupId, playerId, builder);
                } catch (Exception e) {
                    log.error("异步查询登录状态失败", e);
                }
            });
            String url = String.format("http://139.196.93.191:8080/api/image/%s", fileId);
            log.info("getQrLoginCode url:\n {}", url);
            return new QqMedia(new Media(MediaType.IMAGE, url, false), "尚未登陆或令牌过期, 请扫码登陆");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public QqMedia getPlayerData(String qq) {
        AuthParam authParam = getAuth(qq);
        DfData data = client.getPlayerData(authParam.getOpenid(), authParam.getAccess_token()).getData();
        String picurl = picBaseUrl + data.getPlayer().getPicurl() + ".png";
        log.info("player data: {}", data.getPlayerData());
        log.info("picurl: {}", picurl);
        return new QqMedia(new Media(MediaType.IMAGE, picurl, false), data.getPlayerData());
    }

    public void clearToken(String qq) {
        tokenMap.remove(qq);
    }

    private void queryLoginState(String fileId, long qrToken, String qrSig, String loginSig, CookieData cookieData,
                                 String groupId, String playerId, Message.MessageBuilder builder) throws InterruptedException {
        DfResponse dfResponse = client.queryLoginState(qrToken, qrSig, loginSig, cookieData.toJson());
        DfData data = dfResponse.getData();
        if (dfResponse.getCode() == 0) {
            String pUin = data.getCookie().getP_uin();
            String qq = removeLeadingOOrZero(pUin);
            uid2QqNumber.put(playerId, qq);
            log.info("Try get access token: qq: {}, cookie: {}", qq, data.getCookie().toJson());
            DfResponse accessToken = client.getAccessToken(qq, data.getCookie().toJson());
            tokenMap.put(qq, new AuthParam(accessToken.getData().getAccess_token(), accessToken.getData().getOpenid()));
            log.info("Login successful, qq: {}, access token: {}, id: {}", qq,
                    accessToken.getData().getAccess_token(), accessToken.getData().getOpenid());
            imageService.deleteFile(fileId);
        } else if (dfResponse.getCode() >= 1) {
            log.info("Login state: {}", dfResponse.getMsg());
            TimeUnit.SECONDS.sleep(3);
            queryLoginState(fileId, qrToken, qrSig, loginSig, cookieData, groupId, playerId, builder);
        } else {
            imageService.deleteFile(fileId);
            qqOpenApiService.sendGroupMessage(groupId, builder.msgType(MessageType.TEXT)
                    .media(null).content("二维码已失效").msgSeq(100).build());
            log.info("The qrcode can be ignore, code: {}, message: {}", dfResponse.getCode(),
                    dfResponse.getMsg());
        }
    }

    public DfResponse getAchievement(String qq) {
        AuthParam auth = getAuth(qq);
        return client.getPlayerAchievement(auth.getOpenid(), auth.getAccess_token());
    }

    private AuthParam getAuth(String qq) {
        if (tokenMap.containsKey(qq)) {
            return tokenMap.get(qq);
        } else {
            log.warn("qq {} did not login", qq);
            throw new PlayerNotLogin(qq);
        }
    }
    private static String removeLeadingOOrZero(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 去除开头的一个或多个o或0（不区分大小写）
        return input.replaceFirst("^[oO0]+", "");
    }

    public DfResponseWithDataList getItems(String type) {
        return client.getItems(type);
    }

    private static DeltaForceClient deltaForceClient() {

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        return Feign.builder().encoder(new FormEncoder()).decoder(new JacksonDecoder(mapper))
                .logger(new Slf4jLogger(DeltaForceClient.class)).logLevel(Logger.Level.FULL)
                .target(DeltaForceClient.class, baseUrl);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public String getBindQq(String playerId) {
        if (!uid2QqNumber.containsKey(playerId)) {
            throw new PlayerNotLogin();
        } else {
            return uid2QqNumber.get(playerId);
        }
    }
}
