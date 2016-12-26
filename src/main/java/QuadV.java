import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.*;

import static spark.Spark.*;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class QuadV {
    public static void main(String[] args) {
        // Configure Spark and server routes.
        Connection connection;

        try {
            connection = getConnection();
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS polls" +
                    "(id SERIAL UNIQUE, poll_name TEXT);");
            //connection.createStatement().execute("CREATE TYPE statement_type " +
            //        "AS ENUM ('Issue', 'Pro', 'Con', 'Answer')");
        } catch (URISyntaxException | SQLException e) {
            System.out.println(e.getMessage());
            System.exit(e.hashCode());
            return;
        }

        staticFiles.location("/front-end/public");

        port(getPort());

        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

        get("/", (req, res) ->
                new ModelAndView(null, "index.mustache"), templateEngine);

        get("/votingroom", (req, res) -> {
            // Get all of the names of polls for listing
            Map<String, List<Poll>> map = new HashMap<>();

            List<Poll> polls = new DatabaseUtils(connection).getPolls();
            map.put("polls", polls);

            return new ModelAndView(map, "votingroom.mustache");
        }, templateEngine);

        get("/vote/:id", ((req, res) -> new ModelAndView(null, "vote.mustache")), templateEngine);

        // Get the questions one by one for the specific poll
        // Use PreparedStatement in here to stop string injection
        get("/boxes/:id", "application/json", (req, res) ->
                new BoxesUtils(connection, req.params(":id"), req.ip()).getStatementBoxes(),
                new JsonTransformer());

        get("/create", (req, res) ->
                new ModelAndView(null, "create.mustache"), templateEngine);

        post("/create", (req, res) ->
                new CreateUtils(connection, new JsonParser().parse(req.body())).createPoll());

        post("/user/:id", (req, res) -> {
            // TODO: NEED TO ONLY INSERT IF IP IS NOT ALREADY IN TABLE DO A CHECK HERE
            new AnswersUtils(connection, req.params(":id"), req.ip()).addUser();

            return "200 OK";
        });

        post("/answers/:id", "application/json", (req, res) -> {
            JsonObject data = new JsonParser()
                    .parse(req.body())
                    .getAsJsonObject();

            AnswersUtils ans = new AnswersUtils(connection, req.params(":id"), req.ip());

            JsonArray answers = data.get("questions").getAsJsonArray();

            ans.enterAnswersIntoDatabase(answers);
            return ans.resolveDynamicQuestions(data);
        }, new JsonTransformer());

        get("/results", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);

        // Where we will show all of the graphs and stats of the poll
        get("/results/:pollid", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);
    }

    private static Connection getConnection()
            throws URISyntaxException, SQLException {
        System.out.println("Initializing database connection...");
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    private static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");
        return port != null ? Integer.parseInt(port) : 8080;
    }
}
