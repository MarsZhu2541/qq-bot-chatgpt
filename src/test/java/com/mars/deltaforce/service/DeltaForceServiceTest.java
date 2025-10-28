package com.mars.deltaforce.service;

import com.mars.deltaforce.model.MapPassword;
import org.junit.jupiter.api.Test;

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

}