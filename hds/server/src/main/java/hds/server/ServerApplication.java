package hds.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

@SpringBootApplication
public class ServerApplication {
	private static String driver;
	private static String url;
	private static String user;
	private static String password;

	public static void main(String[] args) {
		try {
			fetchProperties();
		}
		catch (Exception ex) {
			Logger logger = Logger.getAnonymousLogger();
			logger.warning(ex.getMessage());
			logger.warning("Exiting.");
			System.exit(1);
		}
		SpringApplication.run(ServerApplication.class, args);
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
