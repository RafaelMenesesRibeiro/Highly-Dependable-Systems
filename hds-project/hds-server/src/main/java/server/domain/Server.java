package server.domain;

import java.sql.*;

public class Server {
    private String ip;
    private String port;

    public Server(String ip, String port) {
        this.ip = ip;
        this.port = port;
        System.out.println(String.format("Created server with ip=%s and port=%s", ip, port));
    }

    public Connection connecToDB() {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        // TODO - Moved user and password to .properties when using AWS DB.
        String user = "postgres";
        String password = "macaco90";

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void QueryDB(Connection conn, String query) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                System.out.println(String.format("RESULT: %s", rs.getString("userid")));
            }
            conn.close();
            stmt.close();
            rs.close();
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}