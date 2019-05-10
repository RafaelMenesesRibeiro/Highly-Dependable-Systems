package hds.client.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.GoodStateResponse;
import hds.security.msgtypes.SaleCertificateResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ConnectionManager {
    public enum Expect {
        BASIC_MESSAGE,
        GOOD_STATE_RESPONSE,
        SALE_CERT_RESPONSE,
        SALE_CERT_RESPONSES
    }

    public static final int MAX_WAIT_BEFORE_TIMEOUT = 5000;

    /***********************************************************
     *
     * SEND REQUEST METHODS
     *
     ***********************************************************/

    public static HttpURLConnection initiateGETConnection(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(MAX_WAIT_BEFORE_TIMEOUT);
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
        connection.setConnectTimeout(MAX_WAIT_BEFORE_TIMEOUT);
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

    /***********************************************************
     *
     * RECEIVE RESPONSE METHODS
     *
     ***********************************************************/

    // public static BasicMessage getResponseMessage(HttpURLConnection conn, Expect type) throws IOException {
    public static Object getResponseMessage(HttpURLConnection conn, Expect type) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = getJSONStringFromHttpResponse(conn);

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            switch (type) {
                case BASIC_MESSAGE:
                    return objectMapper.readValue(jsonString, BasicMessage.class);
                case GOOD_STATE_RESPONSE:
                    return objectMapper.readValue(jsonString, GoodStateResponse.class);
                case SALE_CERT_RESPONSE:
                    return objectMapper.readValue(jsonString, SaleCertificateResponse.class);
            }
        } else if (conn.getResponseCode() == HttpURLConnection.HTTP_MULT_CHOICE) {
            try {
                return new JSONArray(jsonString);
            } catch (JSONException jsone) {
                System.out.println(jsone.getMessage());
                return null;
            }
        }
        return objectMapper.readValue(jsonString, ErrorResponse.class);
    }

    /***********************************************************
     *
     * JSON OBJECT AND JSON STRING RELATED METHODS
     *
     ***********************************************************/

    public static JSONObject newJSONObject(Object object) throws JsonProcessingException, JSONException {
        ObjectMapper objectMapper = new ObjectMapper();
        return new JSONObject(objectMapper.writeValueAsString(object));
    }

    private static boolean is400Response(HttpURLConnection connection) throws IOException {
        return (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST &&
                connection.getResponseCode() < 500);
    }

    private static InputStreamReader getBufferedReaderFromHttpURLConnection(HttpURLConnection connection, boolean isBadRequest)
            throws IOException {

        if (isBadRequest) {  return new InputStreamReader(connection.getErrorStream()); }
        return new InputStreamReader(connection.getInputStream());
    }

    private static String getJSONStringFromHttpResponse(HttpURLConnection connection) throws IOException {
        String currentLine;
        StringBuilder jsonResponse = new StringBuilder();
        InputStreamReader inputStream = getBufferedReaderFromHttpURLConnection(connection, is400Response(connection));
        BufferedReader bufferedReader = new BufferedReader(inputStream);
        while ((currentLine = bufferedReader.readLine()) != null) {
            jsonResponse.append(currentLine);
        }
        bufferedReader.close();
        inputStream.close();
        return jsonResponse.toString();
    }
}
