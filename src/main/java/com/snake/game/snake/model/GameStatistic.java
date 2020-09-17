package com.snake.game.snake.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 游戏统计
 */
@Getter
@Setter
public class GameStatistic {


    private String sendAccountId;

    /**
     * 在线人数
     */
    private int onlineCount;

    /**
     *  版本
     */
    private long lastVersion;

    /**
     * 积分排行榜
     */
    private List<IntegralInfo> rankingList;

    /**
     * 当前玩家积分
     */
    private IntegralInfo current;

}
