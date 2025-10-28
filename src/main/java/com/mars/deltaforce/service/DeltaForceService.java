package com.mars.deltaforce.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mars.deltaforce.model.ItemPrice;
import com.mars.deltaforce.model.MapPassword;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeltaForceService {

    public static final String MAP_PASSWD_URL = "https://deltaforce.hackbit.space/api/map/passwd";
    public static final String ITEM_PRICE_URL = "https://cdn.gh-proxy.com/https://raw.githubusercontent.com/orzice/DeltaForcePrice/master/price.json";
    public static final String NOT_FOUND = "没查到捏 ꒦ິᯅ꒦ີ";
    private OkHttpClient client = new OkHttpClient().newBuilder().build();
    private Gson gson = new Gson();
    private List<ItemPrice> itemPrices = new ArrayList<>();
    private Instant lastLoadPriceTime;

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
        }else if (itemPrices.size() > 10) {
            itemPrices = itemPrices.subList(0, 10);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("您可能要查询的是：\n");
        for (ItemPrice itemPrice : itemPrices) {
            stringBuilder.append(itemPrice.toString());
        }
        return stringBuilder.toString();
    }

}
