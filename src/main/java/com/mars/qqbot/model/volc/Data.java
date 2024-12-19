package com.mars.qqbot.model.volc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Data {
    private List<String> image_urls;
    private String llm_result;
}
