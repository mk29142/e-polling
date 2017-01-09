import java.net.InterfaceAddress;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;

import com.google.gson.*;

import static spark.Spark.*;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class QuadV {
    public static void main(String[] args) {
        // Configure Spark and server routes.
        Connection connection;
        DatabaseUtils dbUtils;
        PollTable pTable;
        AnswersTable ansTable;
        ArgumentTable argTable;
        UserAddedTable uaTable;
        JsonTransformer jt = new JsonTransformer();

        try {
            connection = getConnection();
            dbUtils = new DatabaseUtils(connection);
            dbUtils.initializeDatabase();
            pTable = new PollTable(connection);
            argTable = new ArgumentTable(connection);
            ansTable = new AnswersTable(connection);
            uaTable = new UserAddedTable(connection);
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


            List<Poll> polls = pTable.getPolls();
            map.put("polls", polls);

            return new ModelAndView(map, "votingroom.mustache");
        }, templateEngine);

        get("/vote/:id", ((req, res) -> new ModelAndView(null, "vote.mustache")), templateEngine);

        // Get the questions one by one for the specific poll
        // Use PreparedStatement in here to stop string injection
        get("/boxes/:id", "application/json", (req, res) -> {
            Integer pollId = Integer.parseInt(req.params(":id"));
            return argTable.getStatementBoxes(pollId);
        }, jt);

        get("/create", (req, res) ->
                new ModelAndView(null, "create.mustache"), templateEngine);

        post("/create", (req, res) -> {
                JsonObject obj = new JsonParser().parse(req.body()).getAsJsonObject();
                Integer pollId = pTable.addToTable(obj);
                argTable.addToTable(obj.getAsJsonArray("list"), pollId);
                return pollId.toString();
        }, jt);

        post("/user/:id", (req, res) -> {
            Integer pollId = Integer.parseInt(req.params(":id"));
            String userId = req.session().id();
            ansTable.addUser(pollId, userId);
            return userId;
        });

        post("/answers/:id", "application/json", (req, res) -> {
            Integer pollId = Integer.parseInt(req.params(":id"));
            JsonObject data = new JsonParser()
                    .parse(req.body())
                    .getAsJsonObject();

            String userId = data.get("userId").getAsString();

            JsonArray answers = data.get("questions").getAsJsonArray();

            ansTable.enterAnswersIntoDatabase(answers, pollId, userId);
            DynamicData dynamicQ = ansTable.resolveDynamicQuestions(data, pollId, userId);

            if (dynamicQ.isEnd()) {
                return "STOP";
            } else {
                return dynamicQ;
            }
        }, jt);

        post("/useradded/:id", "application/json", (req, res) -> {
            Integer pollId = Integer.parseInt(req.params(":id"));
            JsonObject newArg = new JsonParser()
                    .parse(req.body())
                    .getAsJsonObject();

            return uaTable.addNewArg(newArg, pollId);
        }, jt);

        get("/results", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);

        // Where we will show all of the graphs and stats of the poll
        get("/results/:id", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);

        get("/graph/:id", "application/json", (req, res) -> {
            Integer pollId = Integer.parseInt(req.params(":id"));
            return argTable.getGraphData(pollId);
        }, new JsonTransformer());

        get("/nodeGraph/:id", "application/json", (req, res) -> {
                Integer pollId = Integer.parseInt(req.params(":id"));
                List<GraphData> gData = argTable.getGraphData(pollId);
                NodeGraphBuilder ngb = new NodeGraphBuilder();
                return ngb.createResultGraph(gData);
        }, jt);
    }

    private static Connection getConnection()
            throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        System.out.println("Initializing database connection to " + dbUrl);
        return DriverManager.getConnection(dbUrl);
    }

    private static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");
        return port != null ? Integer.parseInt(port) : 8080;
    }
}
