package com.mars.deltaforce.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GunPlay implements Comparable<GunPlay>{
    private String name;
    private String desc;
    private long objectID;
    private long escapeCount;
    private long fightCount;
    private long totalPrice;
    private long killCount;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append(": ").append(desc).append("\n")
                .append("携带入局次数: ").append(fightCount).append("\n")
                .append("成功撤离次数: ").append(escapeCount).append("\n")
                .append("击杀次数: ").append(killCount).append("\n")
                .append("总价值: ").append(totalPrice).append("\n\n");
        return stringBuilder.toString();
    }

    @Override
    public int compareTo(GunPlay other) {
        return Long.compare(other.fightCount, this.fightCount); // 降序
    }
}


