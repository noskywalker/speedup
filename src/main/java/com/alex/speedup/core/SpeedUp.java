package com.alex.speedup.core;


import com.alex.speedup.core.speed.AbstractAction;
import com.alex.speedup.core.speed.RemoteServer;


/**
 * xianshuangzhang@gmail.com
 */
public class SpeedUp {
    public SpeedUp() {
    }

    public static void run(String[] args, AbstractAction callback) {
        try {
            RemoteServer.run(args, callback);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }
}
