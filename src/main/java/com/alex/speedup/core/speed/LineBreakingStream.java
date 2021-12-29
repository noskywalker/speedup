package com.alex.speedup.core.speed;

import java.io.IOException;
import java.io.OutputStream;


/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
 */
public class LineBreakingStream extends OutputStream {
    private boolean newline = true;
    private OutputStream delegate;
    private final char prefix;

    public LineBreakingStream(char prefix, OutputStream delegate) {
        this.prefix = prefix;
        this.delegate = delegate;
    }

    public void write(int b) throws IOException {
        if (this.newline && b != 10 && b != 13) {
            this.newline = false;
            this.delegate.write(this.prefix);
        }

        this.delegate.write(b);
        if (b == 10 || b == 13) {
            this.newline = true;
            this.delegate.flush();
        }

    }
}
