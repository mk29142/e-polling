import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/*
 * Think about concurrency in this class as some of the functions update shared variables in the database
 */
class MasterTree {
    private Connection connection;

    MasterTree(Connection connection) {
        this.connection = connection;
    }

    /*
     * updateScore updates the scores of all the nodes in the MasterTree, there may be a problem caused by concatenating
     * the attackers and supporters list into children.
     */
    synchronized void updateVotes(String pollId, String userId) {
        try {
            PreparedStatement getAnswerRow =
                    connection.prepareStatement(
                            "SELECT * FROM ? WHERE 'user_id'=");
            getAnswerRow.setString(1, pollId + "_answers");
            PreparedStatement answerRow =
                    connection.prepareStatement(
                            getAnswerRow.toString().replace("'","\"") + "?");
            answerRow.setString(1, userId);

            ResultSet userAnswers = answerRow.executeQuery();
            userAnswers.next();

            PreparedStatement getQuestions =
                    connection.prepareStatement("SELECT * FROM ?");
            getQuestions.setString(1, pollId);

            ResultSet questions =
                    connection.createStatement().executeQuery(
                            getQuestions.toString().replace("'", "\""));

            while (questions.next()) {
                Integer statementId = questions.getInt("statement_id");
                boolean vote = userAnswers.getBoolean(statementId.toString());

                String yesOrNo = vote? "yes_votes" : "no_votes";

                PreparedStatement updateStatement =
                        connection.prepareStatement(
                                "UPDATE ? SET ? = ? + 1 WHERE 'statement_id'=");
                updateStatement.setString(1, pollId);
                updateStatement.setString(2, yesOrNo);
                updateStatement.setString(3, yesOrNo);

                updateStatement =
                        connection.prepareStatement(
                                updateStatement.toString().replace("'","\"") +"?");
                updateStatement.setInt(1, statementId);
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in updateVotes");
        }
    }

    synchronized void updateScores(String pollId) {
        try {
            PreparedStatement getRoot =
                    connection.prepareStatement(
                            "SELECT * FROM ? WHERE 'statement_id' = 0;");
            getRoot.setString(1, pollId);
            getRoot =
                    connection.prepareStatement(
                            getRoot.toString().replace("'", "\""));
            ResultSet rootResult = getRoot.executeQuery();
            rootResult.next(); // So that we get the first returned row

            Argument root = new Argument(
                    true /* Fake */,
                    rootResult.getString("statement"),
                    true /* Fake */);

            root.setVotesAgainst(rootResult.getInt("no_votes"));
            root.setVotesFor(rootResult.getInt("yes_votes"));
            root.setId(0);

            setArgumentChildren(root, pollId); // Builds argument tree

            root.updateScore(connection, pollId); // Recursively tell to update
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in updateScores");
        }
    }

    private void setArgumentChildren(Argument parent, String pollId) {
        try {
            PreparedStatement findChildren =
                    connection.prepareStatement(
                            "SELECT * FROM ? WHERE 'parent_id'=");
            findChildren.setString(1, pollId);
            findChildren =
                    connection.prepareStatement(
                            findChildren.toString().replace("'", "\"") + "?");
            findChildren.setInt(1, parent.getId());
            ResultSet children = findChildren.executeQuery();

            while (children.next()) {
                String text = children.getString("statement");
                boolean isSupporter = children.getString("type").equals("Pro");
                Argument child = new Argument(true /* Fake */, text, isSupporter);
                child.setId(children.getInt("statement_id"));
                child.setVotesFor(children.getInt("yes_votes"));
                child.setVotesAgainst(children.getInt("no_votes"));
                setArgumentChildren(child, pollId);
                parent.addChild(child);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in setArgumentChildren");
        }
    }

    void deleteFromDataBase(String pollId, String userId) {
        try {
            PreparedStatement deleteUser =
                    connection.prepareStatement(
                            "DELETE FROM ? WHERE 'user_id'=");
            deleteUser.setString(1, pollId + "_answers");

            deleteUser =
                    connection.prepareStatement(
                            deleteUser.toString().replace("'","\"") + "?");
            deleteUser.setString(1, userId);
            deleteUser.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage() + "in deleteFromDataBase");
        }
    }
}
