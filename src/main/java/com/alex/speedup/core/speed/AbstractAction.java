package com.alex.speedup.core.speed;

import java.io.IOException;

/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
 */
public abstract class AbstractAction {
    public AbstractAction() {
    }

    public abstract void onJvmStart() throws IOException;

    public abstract void onJvmExit() throws IOException;
}
