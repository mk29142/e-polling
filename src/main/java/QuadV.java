//package main.java;

import static spark.Spark.*;

import java.sql.*;
import java.math.*;
import java.net.*;

public class QuadV {
    public static void main(String[] args) {
        // Configure Spark and server routes.
        port(getPort());
        Connection connection;

        try {
            c = getConnection();

            staticFiles.location("/front-end/public");

            post("/create", (req, res) -> {
                //Put Questions in the database tree structures for the first time
                req.


            });

            get("/answer", (req, res) -> {
                //Get all of the names of polls for listing
                Statement stmt = connection.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='poll';");


            });

            get("/answer/:pollname", (req, res) -> {
                //Get the questions one by one for the secific poll
                //use PreparedStatement in here to stop string injection
                String pollname = req.params(":pollname");
		PreparedStatement findPolls = c.prepareStatement("SELECT  FROM polls"); 


            });


        } catch (Exception e) {

        }
    }

    private static Connection getConnection()
            throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    private static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");
        return port != null ? Integer.parseInt(port) : 4567;
    }
}
