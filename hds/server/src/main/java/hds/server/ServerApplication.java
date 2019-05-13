package hds.server;

import hds.security.ResourceManager;
import hds.server.exception.DBInitException;
import hds.server.helpers.LogManager;
import hds.server.helpers.PSQLServerSetup;
import hds.server.helpers.ServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

@SpringBootApplication
public class ServerApplication {
	public static final int HDS_NOTARY_CLIENTS_FIRST_PORT = 8000;
	public static final int HDS_NOTARY_REPLICAS_FIRST_PORT = 9000;
	public static final int HDS_NOTARY_REPLICAS_FIRST_CC_PORT = 10000;
	public static final String DB_NAME_PREFIX = "hds_replica_";
	private static LogManager logManager;

	private static String port;
	private static boolean isUseCC = false;
	private static int regularReplicasNumber;
	private static int ccReplicasNumber;
	private static String driver;
	private static String databaseServerUrl;
	private static String url;
	private static String user;
	private static String password;

	public static void main(String[] args) {
		Logger logger = Logger.getAnonymousLogger();
		int serverPort = 9000;
		try {
			String port = args[0];
			int maxClientID = Integer.parseInt(args[1]);
			int maxSRegularReplicasNumber = Integer.parseInt(args[2]);
			int maxCCReplicasNumber = Integer.parseInt(args[3]);
			serverPort = Integer.parseInt(port);

			ResourceManager.setServerPort(serverPort);
			ResourceManager.setMinClientId(HDS_NOTARY_CLIENTS_FIRST_PORT);
			ResourceManager.setMaxClientId(HDS_NOTARY_REPLICAS_FIRST_CC_PORT + maxClientID - 1);

			ServerApplication.port = port;
			ServerApplication.regularReplicasNumber = maxSRegularReplicasNumber;
			ServerApplication.ccReplicasNumber = maxCCReplicasNumber;

			logManager = new LogManager(port);

			fetchProperties();
			if (serverPort >= HDS_NOTARY_REPLICAS_FIRST_CC_PORT) {
				ServerProperties.bootstrap();
				isUseCC = true;
			}

			logger.info("Started server in port " + serverPort + " and max client id " + maxClientID);
		}
		catch (Exception ex) {
			logger.warning("Exiting:\n" + ex.getMessage());
			System.exit(1);
		}

		try {
			PSQLServerSetup.initDatabased();
		}
		catch (DBInitException ex) {
			logger.warning("Could not create database. Exiting");
			logger.warning(ex.getMessage());
			System.exit(-3);
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
			databaseServerUrl = properties.getProperty("spring.datasource.url");
			url = databaseServerUrl + DB_NAME_PREFIX + getPort();
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

	public static String getDatabaseServerUrl() {
		return databaseServerUrl;
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String newUrl) { url = newUrl; }

	public static String getUser() {
		return user;
	}

	public static String getPassword() {
		return password;
	}

	public static int getRegularReplicasNumber() {
		return regularReplicasNumber;
	}

	public static int getCCReplicasNumber() {
		return ccReplicasNumber;
	}

	public static boolean isIsUseCC() {
		return isUseCC;
	}

	public static LogManager getLogManager() {
		return logManager;
	}
}
