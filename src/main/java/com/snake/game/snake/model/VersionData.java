package com.snake.game.snake.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 版本数据
 */
@Getter
@Setter
@Data
public class VersionData implements Cloneable{

    /**
     * 版本号
     */
    private long version;
    /**
     * 版本构建时间
     */
    private long time;

    Boolean full;
    /**
     * 命令
     */
    private String cmds[];

    /**
     * 命令数据
     */
    private String cmdDatas[];

    public VersionData(long version, long time) {
        this.version = version;
        this.time = time;
    }

    public VersionData() {
    }
}
