package hds.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class ServerApplication {
	private static String driver;
	private static String url;
	private static String user;
	private static String password;

	public static void main(String[] args) {
		try {
			FetchProperties();
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
		SpringApplication.run(ServerApplication.class, args);
	}

	private static void FetchProperties() throws Exception {
		Properties properties = new Properties();
		try (InputStream input = ServerApplication.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (input == null) {
				System.out.println("INPUT IS NULL");
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
