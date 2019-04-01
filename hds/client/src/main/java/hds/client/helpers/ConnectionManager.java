package hds.client.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hds.security.domain.SecureResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionManager {

    public static HttpURLConnection initiateGETConnection(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    public static SecureResponse getSecureResponse(HttpURLConnection connection) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = getJSONString(connection);
        SecureResponse secureResponse = objectMapper.readValue(jsonResponse, SecureResponse.class);
        return secureResponse;
    }

    private static String getJSONString(HttpURLConnection connection) throws IOException {
        String currentLine;
        StringBuilder jsonResponse = new StringBuilder();

        InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStream);

        while ((currentLine = bufferedReader.readLine()) != null) {
            jsonResponse.append(currentLine);
        }

        bufferedReader.close();
        inputStream.close();

        return jsonResponse.toString();
    }
}
