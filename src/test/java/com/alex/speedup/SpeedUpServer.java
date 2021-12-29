package com.alex.speedup;

import com.alex.speedup.core.SpeedUp;
import com.alex.speedup.core.speed.AbstractAction;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
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