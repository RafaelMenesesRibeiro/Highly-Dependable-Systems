package hds.server.helpers;

import hds.security.msgtypes.BasicMessage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class LogManager {
	private Logger logger;
	private FileOutputStream outputStream;

	public LogManager(String serverPort) {
		String logFilename = "log_" + serverPort + ".log";
		logger = Logger.getAnonymousLogger();
		try {
			outputStream = new FileOutputStream(logFilename, false);
		}
		catch (FileNotFoundException e) {
			logger.warning("Cannot open log file. Exiting");
			System.exit(-1);
		}
	}

	public void log(String msg, BasicMessage basicMessage) {
		logToLogger(msg, basicMessage);
		logToFile(msg, basicMessage);
	}

	public void log(String msg) {
		logToLogger(msg);
		logToFile(msg);
	}

	private void logToFile(String msg, BasicMessage basicMessage) {
		try {
			outputStream.write((msg + basicMessage.toString() + "\n").getBytes(StandardCharsets.UTF_8));
		}
		catch (IOException ioex) { /* Do nothing */ }
	}

	private void logToFile(String msg) {
		try {
			outputStream.write((msg + "\n").getBytes(StandardCharsets.UTF_8));
		}
		catch (IOException ioex) { /* Do nothing */ }
	}

	private void logToLogger(String msg, BasicMessage basicMessage) {
		logger.info(msg + basicMessage.toString());
	}

	private void logToLogger(String msg) {
		logger.info(msg);
	}
}
