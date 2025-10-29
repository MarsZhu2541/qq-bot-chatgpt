package com.mars.deltaforce.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.mars.deltaforce.provider.DeltaForceProvider.getMapper;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CookieData {
    private String pt_login_sig;
    private String pt_clientip;
    private String pt_serverip;
    private String pt_local_token;
    private String pt_guid_sig;
    private String qrsig;
    private String uikey;
    private String pt2gguin;
    private String superuin;
    private String supertoken;
    private String superkey;
    private String pt_recent_uins;
    private String RK;
    private String ptnick_6462175;
    private String ptcz;
    private String p_uin;
    private String pt4_token;
    private String p_skey;
    private String pt_oauth_token;
    private String pt_login_type;

    public String toJson() {
        try {
            return getMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
