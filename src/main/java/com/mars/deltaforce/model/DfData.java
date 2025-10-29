package com.mars.deltaforce.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DfData {
    private String redTotalMoney;
    private String redTotalCount;
    private String totalGainedPrice;
    private List<GunPlay> gunPlayList;
    private List<RedCollectionDetail> redCollectionDetail;
    private long objectID;
    private String objectName;
    private String desc;
    private String access_token;
    private String expires_in;
    private String openid;
    private String qrSig;
    private String image;
    private long token;
    private String loginSig;
    private CookieData cookie;
    private Player player;
    private Game game;
    private long money;

    public String getPlayerData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n玩家信息:\n");
        stringBuilder.append("角色昵称: " + player.getCharac_name());
        stringBuilder.append("\n");
        stringBuilder.append("现金哈夫币: " + money);
        stringBuilder.append("\n");
        stringBuilder.append(game.toString());
        return stringBuilder.toString();
    }

    public String getAchievement() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n玩家成就信息:\n")
                .append("总资产: " + totalGainedPrice).append("\n")
                .append("------大红成就------ \n")
                .append("大红总数量: ").append(redTotalCount).append("\n")
                .append("大红总价值: " + redTotalMoney).append("\n");

        redCollectionDetail.forEach(item -> stringBuilder.append(item.toString()));
        stringBuilder.append("------枪械成就------: \n");
        gunPlayList.forEach(item -> stringBuilder.append(item.toString()));

        return stringBuilder.toString();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Player {
        private String picurl;
        private String charac_name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Game {
        private String rankpoint;
        private String tdmrankpoint;
        private String soltotalfght;
        private String solttotalescape;
        private String solduration;
        private String soltotalkill;
        private String solescaperatio;
        private String avgkillperminute;
        private String tdmduration;
        private String tdmsuccessratio;
        private String tdmtotalfight;
        private String totalwin;
        private String tdmtotalkill;

        @Override
        public String toString() {
            return "烽火排位分: " + rankpoint + "\n" + "总战斗场次: " + soltotalfght + "\n" + "成功撤离次数: " + solttotalescape + "\n" + "总游戏时长: " + solduration + "\n" + "总击杀数: " + soltotalkill + "\n" + "撤离成功率: " + solescaperatio + "\n" + "分钟伤害: " + avgkillperminute + "\n" + "------------------\n" + "战场排位分: " + tdmrankpoint + "\n" + "总游戏时长: " + tdmduration + "\n" + "胜率: " + tdmsuccessratio + "\n" + "总场次: " + tdmtotalfight + "\n" + "胜利场次: " + totalwin + "\n" + "总击杀数: " + tdmtotalkill + "\n";
        }
    }
}
