package com.alex.speedup.core.speed;

import org.kohsuke.args4j.Option;

/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
 */
public class Options {
    @Option(
        name = "-p",
        usage = "Port to listen on"
    )
    private int port = 7890;

    public Options() {
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
