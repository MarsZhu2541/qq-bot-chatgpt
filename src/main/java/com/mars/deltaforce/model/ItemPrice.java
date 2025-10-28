package com.mars.deltaforce.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemPrice {
    private String name;
    private String price;

    @Override
    public String toString() {
        return name + ": " + price + "\n";

    }
}
