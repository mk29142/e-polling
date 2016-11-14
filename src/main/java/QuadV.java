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
                    "level INT NOT NULL, " +
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
                    System.out.println(elemObj.toString());

                    try {
                        PreparedStatement addRow = connection.prepareStatement("INSERT INTO ? VALUES(?, ?, ?, ?, ?::statement_type);");

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

                        addRow.setInt(4, elemObj.get("level").getAsInt());

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
            TODO:
            NEED TO ONLY INSERT ID IP IS NOT ALREADY IN TABLE
            DO A CHECK HERE
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
            JsonArray answers = data.get("questions").getAsJsonArray();

            System.out.println(answers);

            // Go through head's adding changed vote values (for first run all answers given)
            for (int i = 0; i < answers.size(); i++) {
                JsonArray headAnswers = answers.get(i).getAsJsonArray();

                // go through a single head
                for (int j = 0; j < headAnswers.size(); j++) {
                    JsonElement elem = headAnswers.get(j);
                    JsonObject answer = elem.getAsJsonObject();

                    boolean vote;
                    try {
                        vote = answer.get("support").getAsString().equals("yes");
                    } catch (Exception e) {
                        //In all other cases than the first "support" is not a field so
                        //we don't update it with anything
                        continue;
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
            }

            //Pull from the database into argument objects
            try {
                //get row of answers for user
                PreparedStatement getUserAnswers = connection.prepareStatement("SELECT * FROM ? WHERE user_id=");
                getUserAnswers.setString(1, pollId + "_answers");
                PreparedStatement getAnswers = connection.prepareStatement(getUserAnswers.toString().replace("'", "\"") + "?;");
                getAnswers.setString(1, ip);

                // get all answers
                ResultSet rs = getAnswers.executeQuery();
                rs.next(); //rs is now the first row from the answers table with user_id (should be unique

                Integer nextLevel = data.get("nextLevel").getAsInt();

                List<List<Box>> dynamicQuestions;
                ResultSet rs3;
                do {
                    dynamicQuestions = new ArrayList<>(); //1st elem of each inner list is the head

                    //get all id's for a level
                    PreparedStatement getHeads = connection.prepareStatement("SELECT \"statement_id\" FROM ? WHERE \"level\"=");
                    getHeads.setString(1 ,pollId);
                    PreparedStatement getHeadIds = connection.prepareStatement(getHeads.toString().replace("'","\"") + nextLevel);
                    rs3 = getHeadIds.executeQuery();

                    //this case will occur when we go past last level of tree
                    if (!rs3.isBeforeFirst()) {
                        return "STOP";
                    }

                    //for each head find its' inconsistencies and store it in a list of boxes

                    while (rs3.next()) {

                        Integer currentHead = rs3.getInt("statement_id");
                        Argument head;
                        List<Argument> inconsistencies = new ArrayList<>();

                        //get parent = currenthead and children rows in poll table where parent_id = currentHead
                        PreparedStatement getValues = connection.prepareStatement("SELECT * FROM ? WHERE parent_id=");
                        getValues.setString(1, pollId);
                        PreparedStatement getChildren = connection.prepareStatement(getValues.toString().replace("'", "\"") + "? OR" +
                                " statement_id=? ORDER BY statement_id");
                        getChildren.setInt(1, currentHead);
                        getChildren.setInt(2, currentHead);

                        ResultSet rs2 = getChildren.executeQuery(); //set of all rows for relevant nodes in tree
                        if (rs2.isBeforeFirst()) { //only true if there are children (ignore heads without children)

                            rs2.next(); // head is the first here as it has the lowest index

                            head = new Argument(rs.getBoolean("0"), rs2.getString("statement"), rs2.getString("type").equals("Pro"));
                            head.setId(rs2.getInt("statement_id"));

                            //while there is a row for a child
                            while (rs2.next()) {
                                Integer argumentId = rs2.getInt("statement_id");
                                Integer parentId = rs2.getInt("parent_id");
                                Argument arg = new Argument(rs.getBoolean(argumentId.toString()), rs2.getString("statement"),
                                        rs2.getString("type").equals("Pro"));
                                arg.setId(argumentId);
                                arg.setParent(parentId);

                                head.addChild(arg);
                            }

                            inconsistencies = head.getInconsistencies();

                            //if there are inconsistencies then store them with their head node
                            if (!inconsistencies.isEmpty()) {
                                List<Box> headInconsistencies = new ArrayList<>();
                                headInconsistencies.add(0, head.toBox());

                                for (Argument a : inconsistencies) {
                                    headInconsistencies.add(a.toBox());
                                }
                                dynamicQuestions.add(headInconsistencies);
                            }


                        }
                    }

                    nextLevel++;

                } while (dynamicQuestions.isEmpty());


                for (List<Box> lb : dynamicQuestions) {
                    System.out.println("---------------");
                    for (Box b : lb) {
                        System.out.println(b.text);
                    }
                    System.out.println("---------------");
                }

                return new DynamicData(dynamicQuestions, nextLevel);
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
