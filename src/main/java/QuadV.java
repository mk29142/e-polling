import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.util.JSONPObject;
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
            //  connection.createStatement().execute("CREATE TYPE IF NOT EXISTS statement_type " +
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

        get("/vote/:id", ((request, response) -> new ModelAndView(null, "vote.mustache")), templateEngine);

        get("/boxes/:id", "application/json", (request, response) -> {
            // Get the questions one by one for the specific poll
            // use PreparedStatement in here to stop string injection
            ResultSet rs = null;
            try {
                PreparedStatement findPoll = connection.prepareStatement("SELECT poll_name FROM polls WHERE id = ?;");
                findPoll.setInt(1, Integer.parseInt(request.params(":id")));
                rs = findPoll.executeQuery();

                rs.next();
                PreparedStatement findStatements = connection.prepareStatement("SELECT * FROM ?;");
                findStatements.setString(1, rs.getString("poll_name"));
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
            JsonParser jsonParser = new JsonParser();
            JsonElement element = jsonParser.parse(body);

            PreparedStatement insertPoll = connection.prepareStatement("INSERT INTO polls VALUES (?);");
            PreparedStatement findId = connection.prepareStatement("SELECT CURRVAL('polls_id_seq')");
            PreparedStatement createPoll = connection.prepareStatement("CREATE TABLE ? " +
                    "(statement_id INT NOT NULL, " +
                    "parent_id INT, " +
                    "statement TEXT NOT NULL, " +
                    "type statement_type);");

            JsonObject obj = element.getAsJsonObject();
            JsonArray list = obj.getAsJsonArray("list");
            String name = obj.get("name").getAsString();

            createPoll.setString(1, name);
            insertPoll.setString(1, name);

            try {
                insertPoll.executeUpdate();
                ResultSet rs = findId.executeQuery();
                connection.createStatement().execute(createPoll.toString().replace("'", "\""));

                for (JsonElement elem : list) {
                    JsonObject elemObj = elem.getAsJsonObject();

                    try {
                        PreparedStatement addRow = connection.prepareStatement("INSERT INTO ? VALUES(?, ?, ?, ?::statement_type);");
                        addRow.setString(1, name);
                        addRow.setInt(2, elemObj.get("id").getAsInt());

                        if (elemObj.get("parentId").isJsonNull()) {
                            addRow.setNull(3, Types.INTEGER);
                        } else {
                            addRow.setInt(3, elemObj.get("parentId").getAsInt());
                        }

                        addRow = connection.prepareStatement(addRow.toString().replace("'", "\""));

                        addRow.setString(1, elemObj.get("value").getAsString());
                        addRow.setString(2, elemObj.get("type").getAsString());
                        addRow.executeUpdate();
                    } catch (SQLException | UnsupportedOperationException e) {
                        System.out.print("FAILED: ");
                        System.out.println(e.getMessage());
                    }
                }

                rs.next();

                return rs.getInt("currval");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return "500 ERROR";
            }
        });

        get("/results", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);

        get("/results/:id", (req, res) ->
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

    private static class Box {
        int id, parent;
        String text, type;

        Box(int id, int parentId, String text, String type) {
            this.id = id;
            this.parent = parentId;
            this.text = text;
            this.type = type;
        }
    }
}
