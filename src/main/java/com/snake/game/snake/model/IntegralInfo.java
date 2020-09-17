package com.snake.game.snake.model;


import lombok.Getter;
import lombok.Setter;

/**
 * 玩家积分信息
 */
@Getter
@Setter
public class IntegralInfo {

    private String accountId;
    private String gameName;
    /**
     * 死亡积分
     */
    private int dieIntegral;

    /**
     * 击杀角色说
     */
    private int killIntegral;

    /**
     * 对应版本
     */
    private long lastVersion;
}
