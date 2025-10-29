package com.mars.deltaforce.service;

import com.mars.deltaforce.model.MapPassword;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DeltaForceServiceTest {

    @Test
    void test() {
        DeltaForceService deltaForceService = new DeltaForceService();
        MapPassword mapPassword = deltaForceService.getMapPassword();
        System.out.println(mapPassword);
    }

    @Test
    void test2() {
        DeltaForceService deltaForceService = new DeltaForceService();
        System.out.println(deltaForceService.searchPossibleItemPrice("腾龙突击"));
    }

    @Test
    void test3() throws InterruptedException {
        DeltaForceService deltaForceService = new DeltaForceService();
        deltaForceService.getPlayerData("1", "2541884980",null);
        TimeUnit.SECONDS.sleep(30);
        deltaForceService.getPlayerData("1", "2541884980",null);
        TimeUnit.SECONDS.sleep(40);
    }

}