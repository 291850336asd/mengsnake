package com.snake.game.snake.handler;

import com.alibaba.fastjson.JSON;
import com.snake.game.snake.gameengine.EngineManger;
import com.snake.game.snake.gameengine.NormalGameEngine;
import com.snake.game.snake.listener.SnakeGameListener;
import com.snake.game.snake.model.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 蛇 通信逻辑处理
 */
public class SnakeGameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    static final Logger logger = LoggerFactory.getLogger(NormalGameEngine.class);

    private final ChannelGroup channels;

    public SnakeGameHandler(ChannelGroup channels) {
        this.channels = channels;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel incoming = ctx.channel();
        logger.debug("接收数据  地址:{},id:{},文本:{}", incoming.remoteAddress(), incoming.id().asLongText(), msg.text());
        String cmdText = msg.text().trim();
        int splitIndex;
        if((splitIndex = cmdText.indexOf(":")) < 0){
            logger.error("异常指令:{}", cmdText);
            return;
        }
        String cmd = cmdText.substring(0, splitIndex);
        String cmdData = cmdText.substring(splitIndex + 1);
        if(cmd.equals("JOIN")){
            if(!EngineManger.INSTANCE.gamerEngine.containsKey(incoming.id().asLongText())){
                if(EngineManger.INSTANCE.groupEngine.size() == 0){
                    crateNewEngine(incoming, cmdData);
                } else {
                    for (NormalGameEngine item: EngineManger.INSTANCE.groupEngine.values()) {
                        if(item.snakes.size() <30){
                            EngineManger.INSTANCE.gamerEngine.put(incoming.id().asLongText(),item.getUuid());
                            item.newSnake(incoming.id().asLongText(), cmdData,incoming);
                            return;
                        }
                    }
                    crateNewEngine(incoming, cmdData);
                }
            }
           
        } else if(cmd.equals("CONTROL")){
            EngineManger.INSTANCE.groupEngine.get(EngineManger.INSTANCE.gamerEngine.get(incoming.id().asLongText())).controlSnake(incoming.id().asLongText(), Integer.parseInt(cmdData));
        } else if(cmd.equals("FULL")){
            String fullData = JSON.toJSONString(EngineManger.INSTANCE.groupEngine.get(EngineManger.INSTANCE.gamerEngine.get(incoming.id().asLongText())).getCurrentMapData(false));
            fullData = "version\r\n" + fullData;
            incoming.writeAndFlush(new TextWebSocketFrame(fullData));
        } else if(cmd.equals("QUANTITATIVE")){
            String[] vTexts = cmdData.split(",");
            Long versions[] = new Long[vTexts.length];
            for (int i = 0; i < vTexts.length; i++) {
                versions[i] = Long.parseLong(vTexts[i]);
            }
            for (VersionData s : EngineManger.INSTANCE.groupEngine.get(EngineManger.INSTANCE.gamerEngine.get(incoming.id().asLongText())).getVersion(versions)) {
                incoming.writeAndFlush(new TextWebSocketFrame("version\r\n"+JSON.toJSONString(s)));
            }
        } else if(cmd.equals("RESURGENCE")){
            //复活
            EngineManger.INSTANCE.groupEngine.get(EngineManger.INSTANCE.gamerEngine.get(incoming.id().asLongText())).doResurgence(incoming.id().asLongText());
        }
    }

    private void crateNewEngine(Channel incoming, String cmdData) {
        String uuid = UUID.randomUUID().toString();
        EngineManger.INSTANCE.gamerEngine.put(incoming.id().asLongText(),uuid);
        NormalGameEngine engine = new NormalGameEngine(uuid);
        EngineManger.INSTANCE.groupEngine.put(uuid, engine);
        engine.newSnake(incoming.id().asLongText(), cmdData,incoming);
        startGame(engine);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        logger.info("Client:" + incoming.remoteAddress() + "在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        logger.info("Client:" + incoming.remoteAddress() + "掉线");
        String accountId = incoming.id().asLongText();
        String engineUUId = EngineManger.INSTANCE.gamerEngine.get(accountId);
        if(!StringUtils.isEmpty(engineUUId)){
            EngineManger.INSTANCE.groupEngine.get(engineUUId).snakes.get(accountId).dying();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        logger.error("Client:" + incoming.remoteAddress() + "异常", cause);
        String accountId = incoming.id().asLongText();
        String engineUUId = EngineManger.INSTANCE.gamerEngine.get(accountId);
        if(!StringUtils.isEmpty(engineUUId)){
            EngineManger.INSTANCE.groupEngine.get(engineUUId).snakes.get(accountId).dying();
        }
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        logger.info("[SERVER] - " + incoming.remoteAddress() + "加入");
        channels.add(incoming);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        String accountId = incoming.id().asLongText();
        String engineUUId = EngineManger.INSTANCE.gamerEngine.get(accountId);
        if(!StringUtils.isEmpty(engineUUId)){
            EngineManger.INSTANCE.groupEngine.get(engineUUId).snakes.get(accountId).dying();
        }
        logger.info("Client:" + incoming.remoteAddress() + "离开");
    }

    private void startGame(NormalGameEngine gameEngine){
        gameEngine.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gameEngine.setListener(new SnakeGameListener(gameEngine) {
            @Override
            public void versionChange(VersionData changeData, VersionData currentData) {
                sendVersionData(changeData, gameEngine);
            }

            @Override
            public void statusChange(GameStatistic statistics) {
                sendStatusData(statistics, gameEngine);
            }

            @Override
            public void noticeEvent(GameEvent[] events) {
                sendEvent(events);
            }
        });
    }


    /**
     * 发送事件
     * @param events
     */
    public void sendEvent(GameEvent[] events){
        String prefix = "event\r\n";
        for (Channel channel : channels) {
            for (GameEvent event : events) {
                if (event.getAccountId() == null ||
                        event.getAccountId().equals(channel.id().asLongText())) {
                    channel.writeAndFlush(new TextWebSocketFrame(prefix+JSON.toJSONString(event)));
                }
            }
        }
    }


    /**
     * 发送统计信息
     * @param statistics
     */
    public void sendStatusData(GameStatistic statistics, NormalGameEngine gameEngine){
        String prefix = "status\r\n";
        for (Channel channel : gameEngine.snakes.values().stream().map(itemi -> itemi.getChannel()).collect(Collectors.toList())) {
            IntegralInfo info = gameEngine.getIntegralInfoByAccountId(channel.id().asLongText());
            statistics.setCurrent(info);
            channel.writeAndFlush(new TextWebSocketFrame(prefix + JSON.toJSONString(statistics)));
        }
    }


    /**
     * 发送画面信息
     * @param data
     */
    public void sendVersionData(VersionData data, NormalGameEngine gameEngine){
        VersionData copy=new VersionData(); // 副本
        BeanUtils.copyProperties(data,copy);
        String str = JSON.toJSONString(data);
        String prefix = "version\r\n";
        String[] cmds, cmdDatas;
        for(Channel channel : gameEngine.snakes.values().stream().map(item -> item.getChannel()).collect(Collectors.toList())){
            DrawingCommand cmd = gameEngine.getDrawingCommand(channel.id().asLongText());
            if(cmd != null){
                // 基于当前角色通道的 特殊作画指令
                cmds = Arrays.copyOf(data.getCmds(), data.getCmds().length + 1);
                cmds[cmds.length - 1] = cmd.getCmd();
                cmdDatas = Arrays.copyOf(data.getCmdDatas(), data.getCmdDatas().length + 1);
                cmdDatas[cmdDatas.length - 1] = cmd.getCmdData();
                copy.setCmds(cmds);
                copy.setCmdDatas(cmdDatas);
                channel.writeAndFlush(new TextWebSocketFrame(prefix + JSON.toJSONString(copy)));
            } else {
                channel.writeAndFlush(new TextWebSocketFrame(str));
            }
        }

    }

}
