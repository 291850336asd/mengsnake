package com.snake.game.snake.gameengine;


import java.util.concurrent.ConcurrentHashMap;

//房间管理
public class EngineManger {


    //所有游戏房间
    public ConcurrentHashMap<String, NormalGameEngine> groupEngine = new ConcurrentHashMap<>();
    //所有玩家集合
    public ConcurrentHashMap<String, String> gamerEngine = new ConcurrentHashMap<>();


    public static EngineManger INSTANCE = new EngineManger();

    private EngineManger(){}




}
