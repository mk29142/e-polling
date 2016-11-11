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
            JsonParser jsonParser = new JsonParser();
            JsonElement element = jsonParser.parse(body);

            PreparedStatement insertPoll = connection.prepareStatement("INSERT INTO polls(poll_name) VALUES (?);");
            PreparedStatement findId = connection.prepareStatement("SELECT CURRVAL('polls_id_seq')");
            PreparedStatement createPoll = connection.prepareStatement("CREATE TABLE ? " +
                    "(statement_id INT NOT NULL, " +
                    "parent_id INT, " +
                    "statement TEXT NOT NULL, " +
                    "type statement_type);");
            PreparedStatement createAnswers = connection.prepareStatement("CREATE TABLE ? " +
                    "(user_id TEXT, stupidity INT);");

            JsonObject obj = element.getAsJsonObject();
            JsonArray list = obj.getAsJsonArray("list");
            String name = obj.get("name").getAsString();

            insertPoll.setString(1, name);

            try {
                insertPoll.executeUpdate();
                ResultSet rs = findId.executeQuery();
                rs.next();
                Integer id = rs.getInt("currval");
                createPoll.setString(1, id.toString());
                createAnswers.setString(1, id.toString() + "_answers");
                connection.createStatement().execute(createPoll.toString().replace("'", "\""));
                connection.createStatement().execute(createAnswers.toString().replace("'", "\""));

                for (JsonElement elem : list) {
                    JsonObject elemObj = elem.getAsJsonObject();

                    try {
                        PreparedStatement addRow = connection.prepareStatement("INSERT INTO ? VALUES(?, ?, ?, ?::statement_type);");

                        //this statement adds a column to the answers table
                        PreparedStatement addAnswerColumn = connection.prepareStatement("ALTER TABLE ? ADD COLUMN ? BOOLEAN;");

                        Integer statement_id = elemObj.get("id").getAsInt();

                        addRow.setString(1, id.toString());
                        addRow.setInt(2, statement_id);

                        addAnswerColumn.setString(1, id.toString() + "_answers");
                        addAnswerColumn.setString(2, statement_id.toString());

                        if (elemObj.get("parentId").isJsonNull()) {
                            addRow.setNull(3, Types.INTEGER);
                        } else {
                            addRow.setInt(3, elemObj.get("parentId").getAsInt());
                        }

                        addRow = connection.prepareStatement(addRow.toString().replace("'", "\""));
                        addAnswerColumn = connection.prepareStatement(addAnswerColumn.toString().replace("'", "\""));

                        addRow.setString(1, elemObj.get("value").getAsString());
                        addRow.setString(2, elemObj.get("type").getAsString());
                        addRow.executeUpdate();
                        addAnswerColumn.executeUpdate();
                    } catch (SQLException | UnsupportedOperationException e) {
                        System.out.print("FAILED: ");
                        System.out.println(e.getMessage());
                    }
                }

                return id;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return "500 ERROR";
            }
        });

        post("/user/:id", (request, response) -> {
            /*
            NEED TO ONLY INSERT ID IP IS NOT ALREADY IN TABLE
            */

            String pollId = request.params(":id");
            String ip = request.ip();

            //printing out the ip so you can check
            System.out.println(ip);

            try {
                PreparedStatement createUser = connection.prepareStatement("INSERT INTO ? (user_id)");
                createUser.setString(1, pollId+"_answers");
                PreparedStatement insertIp = connection.prepareStatement(createUser.toString().replace("'", "\"") + "  VALUES(?);");

                insertIp.setString(1, ip);
                insertIp.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return "200 OK";
        });

        post("/answers/:id", "application/json", (req, res) -> {

            JsonParser jsonParser = new JsonParser();

            JsonObject data = jsonParser.parse(req.body()).getAsJsonObject();
            String pollId = req.params(":id");
            String ip = req.ip();
            Integer currentHead = data.get("currentHead").getAsInt();
            JsonArray answers = data.get("questions").getAsJsonArray();

            // Go through array to make the arguments
            for (int i = 1; i < answers.size(); i++) {
                JsonElement elem = answers.get(i);
                JsonObject answer = elem.getAsJsonObject();

                boolean vote;
                try {
                    vote = answer.get("support").getAsString().equals("yes");
                } catch (Exception e) {
                    vote = false;
                }

                Integer id = answer.get("id").getAsInt();

                try {
                    PreparedStatement insertAnswer = connection.prepareStatement("UPDATE ? SET ?=");
                    insertAnswer.setString(1, pollId + "_answers");
                    insertAnswer.setString(2, id.toString());

                    PreparedStatement insertValues = connection.prepareStatement(insertAnswer.toString().replace("'", "\"")
                            + "? WHERE user_id=?;");

                    insertValues.setBoolean(1, vote);
                    insertValues.setString(2, ip);

                    insertValues.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            try {
                PreparedStatement getUserAnswers = connection.prepareStatement("SELECT * FROM ? WHERE user_id=");
                PreparedStatement getValues = connection.prepareStatement("SELECT * FROM ? WHERE parent_id=");

                getUserAnswers.setString(1, pollId + "_answers");
                getValues.setString(1, pollId);

                PreparedStatement getAnswers = connection.prepareStatement(getUserAnswers.toString().replace("'", "\"") + "?;");
                PreparedStatement getChildren = connection.prepareStatement(getValues.toString().replace("'", "\"") + "? OR" +
                        " statement_id=? ORDER BY statement_id");
                getAnswers.setString(1, ip);
                getChildren.setInt(1, currentHead);
                getChildren.setInt(2, currentHead);

                // get all answers
                ResultSet rs = getAnswers.executeQuery();
                rs.next(); //rs is now the row from the answers table with user_id

                ResultSet rs2 = getChildren.executeQuery(); //set of all rows for relevant nodes in tree
                rs2.next();

                Argument head = new Argument(rs.getBoolean("0"), rs2.getString("statement"), rs2.getString("type").equals("Pro"));
                head.setId(rs2.getInt("statement_id"));

                //while there is a row for a child
                while (rs2.next()) {
                    Integer argumentId = rs2.getInt("statement_id");
                    Integer parentId = rs2.getInt("parent_id");
//                    System.out.println(rs2.getString("statement"));
                    Argument arg = new Argument(rs.getBoolean(argumentId.toString()), rs2.getString("statement"),
                            rs2.getString("type").equals("Pro"));
                    arg.setId(argumentId);
                    arg.setParent(parentId);

                    head.addChild(arg);
                }

                List<Argument> inconsistencies = head.getInconsistencies();
                List<Box> dynamicQuestions = new ArrayList<>();
                dynamicQuestions.add(0, head.toBox());

                for (Argument a : inconsistencies) {
                    dynamicQuestions.add(a.toBox());
                    System.out.println(a.getText());
                }
                System.out.println("POSTING DYNAMIC QUESTIONS");
                return dynamicQuestions;
            } catch (Exception e) {
                System.out.println(e.getMessage());

                return "500 ERROR";
            }
        }, new JsonTransformer());

        get("/results", (req, res) ->
                new ModelAndView(null, "results.mustache"), templateEngine);

        get("/results/:id", (reqty, res) -> {
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
