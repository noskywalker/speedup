package com.alex.speedup;

import com.alex.speedup.core.SpeedUp;
import com.alex.speedup.core.speed.AbstractAction;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author ruxing.wrx
 * @date 2020-11-26 17:24
 */
public class SpeedUpServer {

    public static void main(String[] args) {
        try {
            CWAction action = new CWAction();
            SpeedUp.run(args, action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Slf4j
    static class CWAction extends AbstractAction {

        @Override
        public void onJvmStart() throws IOException {
            log.info("CWAction - onJvmStart");
        }

        @Override
        public void onJvmExit() throws IOException {
            log.info("CWAction - onJvmExit");
        }
    }

}