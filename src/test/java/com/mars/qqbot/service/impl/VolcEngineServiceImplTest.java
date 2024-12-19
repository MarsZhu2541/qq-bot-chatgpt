package com.mars.qqbot.service.impl;

import com.mars.qqbot.model.QqMedia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VolcEngineServiceImplTest {

    @Test
    void getMedia() {
        QqMedia media = new VolcEngineServiceImpl("","","").getMedia("牛吃草");
        String url = media.getMedia().getUrl();
        System.out.println(url);
    }
}