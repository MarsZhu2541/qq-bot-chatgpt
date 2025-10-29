package com.mars.deltaforce.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RedCollectionDetail {
    private String name;
    private String desc;
    private long objectID;
    private long count;
    private long price;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name + ": " + count).append("\n")
                .append("单价: " + price + "\n")
                .append(desc).append("\n\n");
        return stringBuilder.toString();
    }
}
