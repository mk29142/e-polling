import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;


public class QuadV {
    public static void main(String[] args) {
        // Configure Spark and server routes.
        Connection connection;

        try {
            connection = getConnection();
        } catch (URISyntaxException | SQLException e) {
            System.out.println(e.getMessage());
            return;
        }

        port(getPort());

        staticFiles.location("/front-end/public");

        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

        get("/", (req, res) -> new ModelAndView(null, "index.mustache"), templateEngine);

        get("/votingroom", (req, res) -> {
            //Get all of the names of polls for listing
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, poll_name FROM public.polls");

            List<Poll> polls = new ArrayList<>();

            while (rs.next()) {
                Poll p = new Poll(rs.getInt("id"), rs.getString("poll_name"));
                polls.add(p);
            }

            return new ModelAndView(polls, "votingroom.mustache");
        }, templateEngine);

        get("/vote/:id", ((request, response) -> new ModelAndView(null, "vote.mustache")));

        get("/boxes/:id", "application/json", (request, response) -> {
            // Get the questions one by one for the specific poll
            // use PreparedStatement in here to stop string injection
            PreparedStatement findPolls = connection.prepareStatement("SELECT poll.\"?\" FROM polls");
            findPolls.setString(1, request.params(":id"));
            ResultSet rs = findPolls.executeQuery();
            List<Box> boxes = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                int parentId = rs.getInt("parentId");
                String text = rs.getString("text");
                String type = rs.getString("type");
                boxes.add(new Box(id, parentId, text, type));
            }

            return boxes;
        }, new JsonTransformer());

        get("/create", (req, res) ->
                new ModelAndView(null, "create.mustache"), templateEngine);

        get("/results", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);
    }

    private static Connection getConnection()
            throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        String user = "tcwufcwhobioex";
        String pass = "AJ1O0Xgyo68mJT410unmRO-WIp";
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(dbUrl, user, pass);
    }

    private static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");
        return port != null ? Integer.parseInt(port) : 8080;
    }

    private static class Poll {
        int id;
        String name;

        Poll(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static class Box {
        int id, parentId;
        String text, type;

        Box(int id, int parentId, String text, String type) {
            this.id = id;
            this.parentId = parentId;
            this.text = text;
            this.type = type;
        }
    }
}
