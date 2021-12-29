package com.alex.speedup.core.common;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;


/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
 */
public class ConnectUtils {
    public ConnectUtils() {
    }

    public static boolean shouldCallRemote(String endpoint) {
        return !Thread.currentThread().getName().startsWith("qtp") && isAnyRemoteUp(endpoint);
    }

    public static boolean isAnyRemoteUp(String eps) {
        String[] var2 = eps.split(",");
        int var3 = var2.length;
        int var4 = 0;

        while(var4 < var3) {
            String ep = var2[var4];
            URI uri = URI.create(ep.trim());

            try {
                Socket s = new Socket(uri.getHost(), uri.getPort());
                s.close();
                return true;
            } catch (IOException var7) {
                ++var4;
            }
        }

        return false;
    }
}
