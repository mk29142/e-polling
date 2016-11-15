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

        get("/", (req, res) -> new ModelAndView(null, "index.mustache"), templateEngine);

        get("/votingroom", (req, res) -> {
            // Get all of the names of polls for listing
            Map<String, List<Poll>> map = new HashMap<>();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, poll_name FROM polls");

            List<Poll> polls = new ArrayList<>();

            while (rs.next()) {
                Poll p = new Poll(rs.getInt("id"), rs.getString("poll_name"));
                polls.add(p);
            }

            map.put("polls", polls);

            return new ModelAndView(map, "votingroom.mustache");
        }, templateEngine);

        get("/vote/:id", ((req, res) -> new ModelAndView(null, "vote.mustache")), templateEngine);

        get("/boxes/:id", "application/json", (request, response) -> {
            // Get the questions one by one for the specific poll
            // use PreparedStatement in here to stop string injection
            String ip = request.ip();
            ResultSet rs = null;

            //checking if connection exists
            PreparedStatement ipCheck = connection.prepareStatement("SELECT EXISTS(SELECT * FROM ? ");
            ipCheck.setString(1,request.params(":id")+"_answers");

            PreparedStatement insertIp = connection.prepareStatement(ipCheck.toString().replace("'","\"") +"WHERE user_id=?);");
            insertIp.setString(1, ip);

            rs = insertIp.executeQuery();
            rs.next();

            //if they have answered questions already do something to notify user
            //if not then carry on

            try {
                PreparedStatement findStatements = connection.prepareStatement("SELECT * FROM ?;");
                findStatements.setString(1, request.params(":id"));
                rs = connection.createStatement().executeQuery(findStatements.toString().replace("'", "\""));
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }
            List<Box> boxes = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("statement_id");
                int parentId = rs.getInt("parent_id");
                String statement = rs.getString("statement");
                String type = rs.getString("type");
                boxes.add(new Box(id, parentId, statement, type));
            }

            return boxes;
        }, new JsonTransformer());

        get("/create", (req, res) -> new ModelAndView(null, "create.mustache"), templateEngine);

        post("/create", (req, res) -> {
            String body = req.body();
            JsonElement element = new JsonParser().parse(body);

            return new CreateUtils(connection, element).createPoll();
        });

        post("/user/:id", (request, response) -> {
            // TODO: NEED TO ONLY INSERT IF IP IS NOT ALREADY IN TABLE DO A CHECK HERE
            String ip = request.ip();

            // Printing out the ip so you can check
            System.out.println(ip);

            new AnswersUtils(connection, request.params(":id"), ip).addUser();

            return "200 OK";
        });

        post("/answers/:id", "application/json", (req, res) -> {
            JsonObject data = new JsonParser()
                    .parse(req.body())
                    .getAsJsonObject();
            String pollId = req.params(":id");
            String ip = req.ip();
            JsonArray answers = data.get("questions").getAsJsonArray();
            AnswersUtils ans = new AnswersUtils(connection, pollId, ip);

            ans.enterAnswersIntoDatabase(answers);

            return ans.resolveDynamicQuestions(data);
        });

        get("/results", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);

        get("/results/:id", (req, res) -> {
                //where we will show all of the graphs and stats of the poll
                return new ModelAndView(null, "results.mustache");
        }, templateEngine);
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

    private static class Poll {
        int id;
        String name;

        Poll(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Poll = " + name + " with id " + id;
        }
    }

}
