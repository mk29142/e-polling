import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class AnswersUtils {
    private MasterTree mt;
    private Connection connection;
    private String pollId;
    private String ip;

    AnswersUtils(Connection connection, String pollId, String ip) {
        this.connection = connection;
        this.pollId = pollId;
        this.ip = ip;
        this.mt = new MasterTree(connection);
    }

    AnswersUtils(Connection connection, String pollId) {
        this(connection, pollId, null);
    }

    void enterAnswersIntoDatabase(JsonArray answers) {
        // Go through head's adding changed vote values (for first run all answers given)
        for (int i = 0; i < answers.size(); i++) {
            JsonArray headAnswers = answers.get(i).getAsJsonArray();

            // Go through a single head
            for (int j = 0; j < headAnswers.size(); j++) {
                JsonElement elem = headAnswers.get(j);
                JsonObject answer = elem.getAsJsonObject();

                boolean vote;
                try {
                    vote = answer.get("support").getAsString().equals("yes");
                } catch (Exception e) {
                    // In all other cases than the first "support" is not a
                    // field so we don't update it with anything
                    continue;
                }

                Integer id = answer.get("id").getAsInt();

                try {
                    insertAnswer(vote, id);
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    Object resolveDynamicQuestions(JsonObject data) {
        // Pull from the database into argument objects
        try {
            /*
             this list will have the "inconsistent" node at its head with all its supporters/attackers
             in the rest of the list
            */
            List<Box> dynamicQuestion = findDynamicQ(data);

            //if there are no dynamic questions
            if(dynamicQuestion==null){

                mt.updateVotes(pollId, ip);
                mt.updateScores(pollId);
                mt.deleteFromDataBase(pollId, ip);

                return "STOP";

            }
            return dynamicQuestion;
        } catch (Exception e) {
            System.out.println(e.getMessage() + " in resolveDynamicQuestions");
            return "500 ERROR";
        }
    }

    void addUser() {
        try {
            PreparedStatement createUser = connection.prepareStatement("INSERT INTO ? (user_id)");
            createUser.setString(1, pollId + "_answers");
            PreparedStatement insertIp = connection.prepareStatement(createUser.toString().replace("'", "\"") + "  VALUES(?);");

            insertIp.setString(1, ip);
            insertIp.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    List<GraphData> getGraphData() {
        List<GraphData> graphData = new ArrayList<>();

        try {
            PreparedStatement getStatementData =
                    connection.prepareStatement(
                            "SELECT * FROM ? ORDER BY 'statement_id';");
            getStatementData.setString(1, pollId);

            getStatementData = connection.prepareStatement(
                    getStatementData.toString().replace("'", "\""));
            ResultSet statementData = getStatementData.executeQuery();

            while (statementData.next()) {
                String text = statementData.getString("statement");
                int id = statementData.getInt("statement_id");
                Integer parentId = statementData.getInt("parent_id");
                int yesVotes = statementData.getInt("yes_votes");
                int noVotes = statementData.getInt("no_votes");
                float score = statementData.getFloat("score");

                graphData.add(new GraphData(
                        id, parentId, score, yesVotes, noVotes, text));
            }

            return graphData;
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in getGraphData");
            return new ArrayList<>();
        }
    }

    private void insertAnswer(boolean vote, Integer id) throws SQLException {
        PreparedStatement insertAnswer =
                connection.prepareStatement("UPDATE ? SET ?=");
        insertAnswer.setString(1, pollId + "_answers");
        insertAnswer.setString(2, id.toString());

        PreparedStatement insertValues = connection.prepareStatement(
                insertAnswer.toString().replace("'", "\"")
                + "? WHERE user_id=?;");

        insertValues.setBoolean(1, vote);
        insertValues.setString(2, ip);

        int worked = insertValues.executeUpdate();
        if (worked != 1) throw new SQLException("No answers were inserted. "
            + id + ": " + vote + ", ip: " + ip);
    }

    private ResultSet getAnswers() throws SQLException {
        // Get row of answers for user
        PreparedStatement getUserAnswers =
                connection.prepareStatement("SELECT * FROM ? WHERE user_id=");
        getUserAnswers.setString(1, pollId + "_answers");
        PreparedStatement getAnswers =
                connection.prepareStatement(
                        getUserAnswers.toString().replace("'", "\"") + "?;");
        getAnswers.setString(1, ip);

        // Get all answers
        return getAnswers.executeQuery();
    }

    private ResultSet getChildren(Integer currentHead) throws SQLException {
        // Get parent = currentHead and children rows in poll
        // table where parent_id = currentHead
        PreparedStatement getValues =
                connection.prepareStatement(
                        "SELECT * FROM ? WHERE parent_id=");
        getValues.setString(1, pollId);
        PreparedStatement getChildren =
                connection.prepareStatement(
                        getValues.toString().replace("'", "\"") + "? OR" +
                " statement_id=? ORDER BY statement_id");
        getChildren.setInt(1, currentHead);
        getChildren.setInt(2, currentHead);

        return getChildren.executeQuery();
    }

    private ResultSet getHeadIds(Integer nextLevel) throws SQLException {
        // Get all ids for a level
        PreparedStatement getHeads = connection.prepareStatement(
                "SELECT \"statement_id\" FROM ? WHERE \"level\"=");
        getHeads.setString(1, pollId);
        PreparedStatement getHeadIds =
                connection.prepareStatement(
                        getHeads.toString().replace("'","\"") + nextLevel);
        return getHeadIds.executeQuery();
    }

    private List<Box> findDynamicQ(JsonObject data) {

        JsonArray arguments = data.getAsJsonArray("questions").get(0).getAsJsonArray();

        JsonObject jsonHead = arguments.get(0).getAsJsonObject();
        //this needs to be set
        Argument head = new Argument(jsonHead);

        List<Argument> argList = new ArrayList<Argument>();
        argList.add(head);

        //turn all json arrays into arguments
        for(int i = 1; i < arguments.size(); i++){
            JsonObject jsonArr = arguments.get(i).getAsJsonObject();
            Argument arg = new Argument(jsonArr);
            argList.add(arg);
        }

        //set the children of each argument using argList
        for(int i = 0; i < argList.size(); i++){
            Argument arg = argList.get(i);
            for(int j = 0; j < argList.size(); j++){
                if(arg.getId() == argList.get(j).getParent()){
                    arg.addChild(argList.get(j));
                }
            }
        }

        //getInconsistencies() has to be changed in Argument if we want this
        // to work for trees with more than one row
        List<Argument> inconsistencies = head.getInconsistencies();

        // If there are inconsistencies then store them with
        // their head node
        if (!inconsistencies.isEmpty()) {
            List<Box> dynamicQuestion = new ArrayList<>();
            dynamicQuestion.add(head.toBox());
            dynamicQuestion.addAll(inconsistencies
                            .stream()
                            .map(Argument::toBox)
                            .collect(Collectors.toList()));

            return dynamicQuestion;
        }

        return null;

    }

    private void addChild(ResultSet rs, ResultSet children, Argument head) throws SQLException {
        Integer argumentId = children.getInt("statement_id");
        Integer parentId = children.getInt("parent_id");
        Argument arg = new Argument(
                rs.getBoolean(argumentId.toString()),
                children.getString("statement"),
                children.getString("type").equals("Pro"));
        arg.setId(argumentId);
        arg.setParent(parentId);

        head.addChild(arg);
    }
}
