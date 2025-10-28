package com.mars.deltaforce.model;


import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapPassword {
    private String chinese_date;
    private Map<String, String> maps;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s 三角洲行动今日密码\n",chinese_date));
        maps.entrySet().forEach((entry) -> {
            stringBuilder.append(String.format("%s: %s\n",entry.getKey(),entry.getValue()));
        });
        return stringBuilder.toString();
    }
}
