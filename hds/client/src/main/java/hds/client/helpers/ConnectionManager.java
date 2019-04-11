package hds.client.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hds.security.msgtypes.BasicMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.spec.InvalidKeySpecException;

public class ConnectionManager {
    private static final int MAX_WAIT = 8000;

    public static JSONObject newJSONObject(Object object) throws JsonProcessingException, JSONException {
        ObjectMapper objectMapper = new ObjectMapper();
        return new JSONObject(objectMapper.writeValueAsString(object));
    }

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

    public static BasicMessage getSecureResponse(HttpURLConnection connection) throws IOException {
        String jsonResponse = getJSONStringFromHttpResponse(connection);
        return tryNewBasicResponse(1, jsonResponse);
    }

    private static SecureResponse tryNewBasicResponse(int attempt, String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            switch (attempt) {
                case 1:
                    return objectMapper.readValue(json, hds.client.domain.SecureBasicResponse.class);
                case 2:
                    return objectMapper.readValue(json, hds.client.domain.SecureGoodStateResponse.class);
                case 3:
                    return objectMapper.readValue(json, hds.client.domain.SecureErrorResponse.class);
                default:
                    return null;
            }
        } catch (JsonMappingException jme) {
            return tryNewBasicResponse(++attempt, json);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
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

    public static void processResponse(HttpURLConnection connection, String nodeId) throws IOException {

        BasicMessage responseMessage = getSecureResponse(connection);
        hds.security.msgtypes.responses.SecureResponse secureResponse = domainSecureResponse.translateSecureResponse();

        if (isAuthenticResponse(secureResponse, nodeId)) {
            switch (connection.getResponseCode()) {
                case(HttpURLConnection.HTTP_OK):
                    System.out.println("[o] " + secureResponse.toString());
                    break;
                case(HttpURLConnection.HTTP_BAD_REQUEST):
                    System.out.println("    [x] Users and goods need to be alphanumeric characters without spaces;");
                    break;
                case(HttpURLConnection.HTTP_NOT_FOUND):
                    System.out.println("    [x] One of the specified parameters not exist in the notary system;");
                    break;
                case(HttpURLConnection.HTTP_INTERNAL_ERROR):
                    System.out.println("    [x] Ups... something went wrong on the server;");
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
