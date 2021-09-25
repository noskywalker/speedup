package com.alex.speedup;

import com.alex.speedup.core.SpeedUp;
import com.alex.speedup.core.speed.AbstractAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SpeedupApplication {

	public static void main(String[] args) {
		try {
			CWAction action = new CWAction();
			System.out.println("");
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
