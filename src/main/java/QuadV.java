import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class QuadV {
    public static void main(String[] args) {
        // Configure Spark and server routes.
        port(getPort());
        Connection connection;
        staticFiles.location("/front-end/public");

        try {
            connection = getConnection();
        } catch (URISyntaxException | SQLException e) {
            return;
        }

        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

        get("/", (req, res) -> new ModelAndView(null, "index.mustache"), templateEngine);

        get("/votingroom", (req, res) -> {
            //Get all of the names of polls for listing
            try {
                Map<String, String> map = new HashMap<>();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM public.polls");
                StringBuilder sb = new StringBuilder();
                sb.append('[');

                while (rs.next()) {
                    sb.append('{');
                    sb.append(String.format("\"id\": %d,", rs.getInt("id")));
                    sb.append(String.format("\"name\": %s", rs.getString("poll_name")));
                    sb.append("},");
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append(']');
                map.put("polls", sb.toString());
                return new ModelAndView(map, "votingroom.mustache");
            } catch (SQLException e) {
                return null;
            }
        }, templateEngine);

        get("/vote/:id", (request, response) -> {
            Map<String, ArrayList> map = new HashMap<>();
            // Get the questions one by one for the specific poll
            // use PreparedStatement in here to stop string injection

            PreparedStatement findPolls = connection.prepareStatement("SELECT * FROM poll.\"?\"");
            findPolls.setString(1, request.params(":id"));

            ResultSet rs = findPolls.executeQuery();

            return new ModelAndView(map, "vote.mustache");
        }, templateEngine);

        post("/create", (req, res) -> {
            String statements = req.body();
            PreparedStatement addStatement = connection.prepareStatement("ALTER TABLE ");
            return new ModelAndView(null, "create.mustache");
        }, templateEngine);

        get("/results", (req, res) ->
                new ModelAndView(null, "results.mustache"),
                templateEngine);
    }

    private static Connection getConnection()
            throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    private static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");
        return port != null ? Integer.parseInt(port) : 8080;
    }
}
