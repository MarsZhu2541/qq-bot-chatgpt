package com.mars.deltaforce.client;

import com.mars.deltaforce.model.DfResponse;
import com.mars.deltaforce.model.DfResponseWithDataList;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


@Headers({"accept: application/json", "accept-encoding: UTF-8"})
public interface DeltaForceClient {

    @RequestLine("GET /qq/sig")
    DfResponse getQrCode();

    @RequestLine("POST /qq/status?qrToken={qrToken}&qrSig={qrSig}&loginSig={loginSig}")
    @Headers({"Content-Type: multipart/form-data"})
    DfResponse queryLoginState(@Param("qrToken") final long qrToken, @Param("qrSig") final String qrSig,
                               @Param("loginSig") final String loginSig, @Param("cookie") final String cookie);

    @RequestLine("POST /qq/access")
    @Headers({"Content-Type: multipart/form-data"})
    DfResponse getAccessToken(@Param("qq") String qq, @Param("cookie") String cookie);


    @RequestLine("POST /game/bind?openid={openid}&access_token={accessToken}")
    @Headers("acctype: qc")
    DfResponse bindPlayer(@Param("openid") String openid, @Param("accessToken") String accessToken);

    @RequestLine("GET /game/player?openid={openid}&access_token={accessToken}")
    @Headers("acctype: qc")
    DfResponse getPlayerData(@Param("openid") String openid, @Param("accessToken") String accessToken);

    @RequestLine("GET /game/items?type={type}")
    DfResponseWithDataList getItems(@Param("type")String type);

    @RequestLine("GET /game/achievement?openid={openid}&access_token={accessToken}")
    DfResponse getPlayerAchievement(@Param("openid") String openid, @Param("accessToken") String accessToken);

}
