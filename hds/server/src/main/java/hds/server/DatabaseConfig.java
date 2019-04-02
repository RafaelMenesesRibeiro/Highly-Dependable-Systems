package hds.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Profile("web")
@Configuration
@Component
public class DatabaseConfig {
	@Value("${spring.datasource.driverClassName}")
	public String driver;
	@Value("${spring.datasource.platform}")
	public String platform;
	@Value("${spring.datasource.url}")
	public String url;
	@Value("${spring.datasource.username}")
	public String user;
	@Value("${spring.datasource.password}")
	public String password;

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		return dataSource;
	}

	public Connection getConnection() throws Exception {
		return dataSource().getConnection();
	}
}
