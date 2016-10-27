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

            MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

            get("/", (req, res) -> new ModelAndView(null, "index.mustache"), templateEngine);

            get("/votingroom", (req, res) -> {
                //Get all of the names of polls for listing
                Statement stmt = connection.createStatement();
                Map<String, ArrayList> map = new HashMap<>();
                ResultSet rs = stmt.executeQuery("SELECT poll_name FROM public.polls");

                ArrayList<String> output = new ArrayList<String>();
                while (rs.next()) {
                    output.add(rs.getString("poll_name"));
                }
                map.put("polls", output);
                return new ModelAndView(map, "votingroom.mustache");
            }, templateEngine);

            get("/vote/:id", (request, response) -> {
                Map<String, ArrayList> map = new HashMap<>();
                //Get the questions one by one for the specific poll
                //use PreparedStatement in here to stop string injection
                PreparedStatement findPolls = connection.prepareStatement("SELECT poll.\"?\" FROM polls");
                findPolls.setString(1, request.params(":id"));

                ResultSet rs = findPolls.executeQuery();


                return new ModelAndView(map, "vote.mustache");
            }, templateEngine);

            get("/create", (req, res) ->
                            new ModelAndView(null, "create.mustache"),
                    templateEngine);

            get("/results", (req, res) ->
                            new ModelAndView(null, "results.mustache"),
                    templateEngine);
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
