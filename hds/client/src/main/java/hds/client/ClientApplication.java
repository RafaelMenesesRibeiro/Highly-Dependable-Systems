package hds.client;

import hds.client.helpers.ClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.Properties;

@SpringBootApplication
public class ClientApplication {
    public static void main(String[] args) {
        String portId = args[0];
        String maxPortId = args[1];
        System.out.println(portId);
        System.out.println(maxPortId);
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", portId));
        app.run(args);
    }
 }
