package hds.client.controllers;

import hds.client.helpers.ClientProperties;
import hds.security.SecurityManager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

@RestController
public class HelloWorld {

    @GetMapping(value = "/hello")
    public String HelloWorldHandle() {
        String resourceId = ClientProperties.getPort();
        String resourceContent = "None";
        PublicKey resourcePublicKey;

        try {
            resourcePublicKey = SecurityManager.getPublicKeyFromResource(resourceId);
            byte[] encodedPublicKey = resourcePublicKey.getEncoded();
            String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);
            return b64PublicKey;
        } catch (IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return "failed";
    }
}
