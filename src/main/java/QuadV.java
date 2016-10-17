package main.java;

import static spark.Spark.*;
import java.sql.*;
import java.math.*;
import java.net.*;

public class QuadV {
	public static void main(String[] args) {
        // Configure Spark
        port(getPort());
        try {
           getConnection();
        } catch (Exception e) {
           
        }
        staticFiles.location("/front-end/build");
        

        
	}

    private static Connection getConnection() throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    private static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");
        return port != null ? Integer.parseInt(port) : 4567;
    }

}
