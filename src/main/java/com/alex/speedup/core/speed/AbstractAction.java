package com.alex.speedup.core.speed;

import java.io.IOException;

public abstract class AbstractAction {
    public AbstractAction() {
    }

    public abstract void onJvmStart() throws IOException;

    public abstract void onJvmExit() throws IOException;
}
