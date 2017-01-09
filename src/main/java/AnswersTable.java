import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class AnswersTable {
    private MasterTree mt;
    private Connection connection;

    AnswersTable(Connection connection) {
        this.connection = connection;
        this.mt = new MasterTree(connection);
    }

    void enterAnswersIntoDatabase(JsonArray answers, Integer pollId, String userId) {
        // Go through head's adding changed vote values
        // (for first run all answers given)
        // This statement adds a column to the answers table

        for (int i = 0; i < answers.size(); i++) {
            JsonObject answer = answers.get(i).getAsJsonObject();

            boolean vote = answer.get("support").getAsString().equals("yes");
            int answerId = answer.get("id").getAsInt();

            // So that we can safely ignore the dummy statement
            if (answerId < 10000) insertAnswer(vote, answerId, pollId, userId);
        }
    }

    DynamicData resolveDynamicQuestions(JsonObject data, Integer pollId, String userId) {
        // This list will have the "inconsistent" node at its head with all its
        // supporters/attackers in the rest of the list
        DynamicData dynamicData = new DynamicData(findDynamicQ(data));

        // If there are no dynamic questions we update the graph table
        if (dynamicData.isEnd()) {
            mt.updateVotes(pollId, userId);
            mt.updateScores(pollId);
            mt.deleteFromDataBase(pollId, userId);
        }

        return dynamicData;
    }

    void addUser(Integer pollId, String userId) {
        try {
            PreparedStatement createUser = connection.prepareStatement(
                    "INSERT INTO answers(poll_id, user_id) VALUES(?, ?)");
            createUser.setInt(1, pollId);
            createUser.setString(2, userId);

            createUser.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in AnswersTable.addUser");
        }
    }

    private void insertAnswer(boolean vote, Integer id, Integer pollId, String userId) {
        try {
            PreparedStatement insertAnswer =
                connection.prepareStatement("UPDATE answers SET answersArray[?] = ");
            insertAnswer.setInt(1, id);

            PreparedStatement insertValues = connection.prepareStatement(
                    insertAnswer.toString().replace("'", "\"")
                    + "? WHERE user_id=? AND poll_id=?");

            insertValues.setBoolean(1, vote);
            insertValues.setString(2, userId);
            insertValues.setInt(3, pollId);

            insertValues.executeUpdate();
        } catch (SQLException e1) {
            // This could be because we need to add the new column
            // so we should add that here.
            System.out.println(e1.getMessage() + " e1 in AnswerTable.insertAnswer");

            try {
                PreparedStatement insertAnswer =
                        connection.prepareStatement("UPDATE answers SET answersArray = answersArray || ?");
                insertAnswer.setBoolean(1, vote);
            } catch (SQLException e2) {
                System.out.println(e2.getMessage() + " e2 in AnswerTable.insertAnswer");
            }

        }
    }

    // Turn all json arrays into arguments
    private List<Argument> convertToArgumentList(JsonArray arguments) {
        List<Argument> argList = new ArrayList<>();

        for (int i = 1; i < arguments.size(); i++) {
            JsonObject jsonArr = arguments.get(i).getAsJsonObject();
            Argument arg = new Argument(jsonArr);
            argList.add(arg);
        }

        return argList;
    }

    // Set the children of each argument using argList
    private void setChildrenArguments(List<Argument> argList) {
        for (int i = 0; i < argList.size(); i++) {
            Argument arg = argList.get(i);
            int argId = arg.getId();

            for (Argument currArg : argList) {
                if (argId == currArg.getParent()) {
                    arg.addChild(currArg);
                }
            }
        }
    }

    private List<Box> findDynamicQ(JsonObject data) {
        JsonArray arguments = data.get("questions").getAsJsonArray();
        JsonObject jsonHead = arguments.get(0).getAsJsonObject();

        // This needs to be set
        List<Argument> argList = convertToArgumentList(arguments);
        Argument head = new Argument(jsonHead);
        argList.add(head);

        setChildrenArguments(argList);

        List<Argument> inconsistencies = head.getInconsistencies();

        // If there are inconsistencies then store them with
        // their head node
        return inconsistencies
                .stream()
                .map(Argument::toBox)
                .collect(Collectors.toList());
    }
}
