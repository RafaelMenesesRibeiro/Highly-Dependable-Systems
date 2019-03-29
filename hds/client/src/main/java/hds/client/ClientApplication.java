package hds.client;

import hds.client.helpers.ClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.Properties;

@SpringBootApplication
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);

        String port = args[0];
        ClientProperties.setPort(port);

        app.setDefaultProperties(Collections.singletonMap("server.port", port));
        app.run(args);
    }
 }
