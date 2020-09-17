package com.snake.game.snake.listener;


import com.snake.game.snake.gameengine.NormalGameEngine;
import com.snake.game.snake.model.GameEvent;
import com.snake.game.snake.model.GameStatistic;
import com.snake.game.snake.model.VersionData;

public abstract class SnakeGameListener {

    public NormalGameEngine getGameEngine() {
        return gameEngine;
    }

    private NormalGameEngine gameEngine;

    public SnakeGameListener(NormalGameEngine gameEngine){
        this.gameEngine = gameEngine;
    }


    /**
     * 地图版本变更
     * @param changeData
     * @param currentData
     */
    public abstract void versionChange(VersionData changeData, VersionData currentData);

    /**
     * 积分变更
     * @param statistic
     */
    public abstract void statusChange(GameStatistic statistic);

    /**
     * 事件通知
     * @param events
     */
    public abstract void noticeEvent(GameEvent[] events);

}
