package com.mars.deltaforce.provider;

import com.mars.qqbot.model.QqMedia;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DeltaForceProviderTest {

    @Test
    void getQrLoginCode() throws InterruptedException {
        DeltaForceProvider deltaForceProvider = new DeltaForceProvider();
        System.out.println(deltaForceProvider.getAchievement("2541884980").getData().getAchievement());

    }



}