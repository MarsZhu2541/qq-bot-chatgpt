package com.mars.deltaforce.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mars.deltaforce.model.DfData;
import com.mars.deltaforce.model.GunPlay;
import com.mars.deltaforce.model.ItemPrice;
import com.mars.deltaforce.model.MapPassword;
import com.mars.deltaforce.provider.DeltaForceProvider;
import com.mars.foundation.model.Message;
import com.mars.qqbot.model.QqMedia;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DeltaForceService {

    public static final String MAP_PASSWD_URL = "https://deltaforce.hackbit.space/api/map/passwd";
    public static final String ITEM_PRICE_URL = "https://cdn.gh-proxy.com/https://raw.githubusercontent.com/orzice/DeltaForcePrice/master/price.json";
    public static final String NOT_FOUND = "没查到捏 ꒦ິᯅ꒦ີ";
    private static final String QQ_REGEX = "^[1-9]\\d{4,10}$";
    private static final Pattern QQ_PATTERN = Pattern.compile(QQ_REGEX);
    private OkHttpClient client = new OkHttpClient().newBuilder().build();
    private Gson gson = new Gson();
    private List<ItemPrice> itemPrices = new ArrayList<>();
    private Instant lastLoadPriceTime;
    private static final Map<Long, DfData> itemMap = new HashMap();

    @Autowired
    private DeltaForceProvider deltaForceProvider;

    public MapPassword getMapPassword() {

        try {
            Response response = client.newCall(new Builder().url(MAP_PASSWD_URL).build()).execute();
            return gson.fromJson(response.body().string(), MapPassword.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ItemPrice> loadItemPrices() {
        try {
            Response response = client.newCall(new Builder().url(ITEM_PRICE_URL).build()).execute();
            return gson.fromJson(response.body().string(),
                    TypeToken.getParameterized(List.class, ItemPrice.class).getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ItemPrice> searchItemPrice(final String name) {
        Instant now = Instant.now();
        if (lastLoadPriceTime == null || itemPrices.isEmpty() || now.isAfter(lastLoadPriceTime.plus(Duration.ofMinutes(10)))) {
            itemPrices = loadItemPrices();
            lastLoadPriceTime = now;
        }
        return itemPrices.stream().filter(item -> item.getName().toLowerCase()
                .contains(name.toLowerCase())).toList();
    }

    public String searchPossibleItemPrice(final String name) {
        List<ItemPrice> itemPrices = searchItemPrice(name);
        if (itemPrices.isEmpty()) {
            return NOT_FOUND;
        } else if (itemPrices.size() > 10) {
            itemPrices = itemPrices.subList(0, 10);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("您可能要查询的是：\n");
        for (ItemPrice itemPrice : itemPrices) {
            stringBuilder.append(itemPrice.toString());
        }
        return stringBuilder.toString();
    }

    public QqMedia getLoginQrCode(String groupId, String playerId, Message.MessageBuilder builder) {
        return deltaForceProvider.getQrLoginCode(groupId, playerId, builder);
    }

    public QqMedia getPlayerData(String groupId, String playerId, Message.MessageBuilder builder) {
        String qq = deltaForceProvider.getBindQq(playerId);
        try {
            return deltaForceProvider.getPlayerData(qq);
        } catch (RuntimeException e) {
            deltaForceProvider.clearToken(playerId);
            return getLoginQrCode(groupId, playerId, builder);
        }

    }

    public String getAchievement(String playerId) {
        String qq = deltaForceProvider.getBindQq(playerId);
        DfData data = deltaForceProvider.getAchievement(qq).getData();
        data.getRedCollectionDetail().forEach(item -> {
            DfData dfData = getItem(item.getObjectID());
            item.setName(dfData.getObjectName());
            item.setDesc(dfData.getDesc());
        });
        List<GunPlay> topGunPlays = data.getGunPlayList().stream()
                .sorted(Comparator.comparingLong(GunPlay::getFightCount).reversed()) // 降序排序
                .limit(3)
                .peek(item -> {
                    DfData dfData = getItem(item.getObjectID());
                    item.setName(dfData.getObjectName());
                    item.setDesc(dfData.getDesc());
                })
                .collect(Collectors.toList());

        data.setGunPlayList(topGunPlays);
        return data.getAchievement();
    }


    private DfData getItem(long objectID) {
        if (itemMap.isEmpty()) {
            deltaForceProvider.getItems("gun").getData().forEach(item -> itemMap.put(item.getObjectID(), item));
            deltaForceProvider.getItems("props").getData().forEach(item -> itemMap.put(item.getObjectID(), item));
        }
        return itemMap.get(objectID);
    }

}
