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

        String portId = args[0];
        String maxPortId = args[1];
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        app.setDefaultProperties(Collections.singletonMap("server.port", portId));
        app.run(args);
    }
 }
