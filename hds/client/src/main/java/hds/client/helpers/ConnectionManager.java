package hds.client.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import hds.client.domain.SecureResponse;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.spec.InvalidKeySpecException;

import static hds.security.SecurityManager.isAuthenticResponse;

public class ConnectionManager {
    private static final int MAX_WAIT = 8000;

    public static HttpURLConnection initiateGETConnection(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(MAX_WAIT);
        return connection;
    }

    public static HttpURLConnection initiatePOSTConnection(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(MAX_WAIT);
        return connection;
    }

    public static void sendPostRequest(HttpURLConnection connection, JSONObject requestObj) throws IOException {
        OutputStream outputStream = connection.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write(requestObj.toString());
        outputStreamWriter.flush();
        outputStreamWriter.close();
        outputStream.close();
    }

    public static hds.client.domain.SecureResponse getSecureResponse(HttpURLConnection connection) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = getJSONStringFromHttpResponse(connection);
        try {
            return objectMapper.readValue(jsonResponse, hds.client.domain.SecureGoodStateResponse.class);
        } catch (UnrecognizedPropertyException upe) {
            return objectMapper.readValue(jsonResponse, hds.client.domain.SecureErrorResponse.class);
        }
    }

    private static String getJSONStringFromHttpResponse(HttpURLConnection connection) throws IOException {
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

    public static void processResponse(HttpURLConnection connection, String nodeId)
            throws InvalidKeySpecException, IOException {

        hds.client.domain.SecureResponse domainSecureResponse = getSecureResponse(connection);
        hds.security.msgtypes.SecureResponse secureResponse = domainSecureResponse.translateSecureResponse();

        if (isAuthenticResponse(secureResponse, nodeId)) {
            switch (connection.getResponseCode()) {
                case(HttpURLConnection.HTTP_OK):
                    System.out.println("[o] " + secureResponse.toString());
                    break;
                case(HttpURLConnection.HTTP_NOT_FOUND):
                    System.out.println("    [x] One of the specified parameters not exist in the notary system;");
                    break;
                default:
                    System.out.println("    [x] Notary suffered from an internal error, please try again later;");
                    break;
            }
        } else {
            System.out.println(String.format("    [x] Could not validate nodeId: %s, signature...", nodeId));
        }
    }
}
