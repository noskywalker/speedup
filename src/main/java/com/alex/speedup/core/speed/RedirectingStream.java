package com.alex.speedup.core.speed;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class RedirectingStream extends OutputStream {
    private final PrintStream delegate;
    private OutputStream redirector;

    public RedirectingStream(PrintStream delegate) {
        this.delegate = delegate;
    }

    public void write(int b) throws IOException {
        this.delegate.write(b);
        if (this.redirector != null) {
            this.redirector.write(b);
        }

    }

    public void setRedirector(OutputStream redirector) {
        this.redirector = redirector;
    }
}
