package hds.server;

import hds.security.ResourceManager;
import hds.server.helpers.ServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

@SpringBootApplication
public class ServerApplication {
	private static String port;
	private static String driver;
	private static String url;
	private static String user;
	private static String password;

	public static void main(String[] args) {
		Logger logger = Logger.getAnonymousLogger();
		int serverPort = 9000;
		try {
			String port = args[0];
			ServerApplication.port = port;
			serverPort = Integer.parseInt(port);
			ResourceManager.setServerPort(serverPort);
			int maxClientID = Integer.parseInt(args[1]);
			ResourceManager.setMaxClientId(maxClientID);

			fetchProperties();
			// If it's the main server, starts with the Citizen Card feature.
			// TODO - Change this to final value. //
			if (serverPort == 9005) {
				ServerProperties.bootstrap();
			}

			logger.info("Started server in port " + serverPort + " and max client id " + maxClientID);
		}
		catch (Exception ex) {
			logger.warning("Exiting:\n" + ex.getMessage());
			System.exit(1);
		}
		SpringApplication app = new SpringApplication(ServerApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", serverPort));
		app.run(args);
	}

	private static void fetchProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream input = ServerApplication.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (input == null) {
				throw new IOException("application.properties not found.");
			}
			properties.load(input);
			driver = properties.getProperty("spring.datasource.driverClassName");
			url = properties.getProperty("spring.datasource.url");
			user = properties.getProperty("spring.datasource.username");
			password = properties.getProperty("spring.datasource.password");
		}
	}

	public static String getPort() {
		return port;
	}

	public static String getDriver() {
		return driver;
	}

	public static String getUrl() {
		return url;
	}

	public static String getUser() {
		return user;
	}

	public static String getPassword() {
		return password;
	}
}
