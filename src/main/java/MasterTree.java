import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    synchronized void updateVotes(Integer pollId, String userId) {
        try {
            PreparedStatement getAnswerRow =
                    connection.prepareStatement(
                            "SELECT * FROM answers WHERE poll_id=? AND user_id=?");
            getAnswerRow.setInt(1, pollId);
            getAnswerRow.setString(2, userId);

            System.out.println(getAnswerRow.toString());
            ResultSet userAnswers = connection.createStatement().executeQuery(
                    getAnswerRow.toString().replace("'", "'"));
            userAnswers.next();
            boolean[] answers = (boolean[])userAnswers.getArray("answersArray").getArray();
            System.out.println("hi");


            PreparedStatement getQuestions =
                    connection.prepareStatement("SELECT * FROM arguments WHERE poll_id=?");
            getQuestions.setInt(1, pollId);
            ResultSet questions = getQuestions.executeQuery();

            while (questions.next()) {
                Integer argumentId = questions.getInt("arg_id");

                boolean vote = answers[argumentId];

                String yesOrNo = vote? "yes_votes" : "no_votes";

                PreparedStatement updateStatement =
                        connection.prepareStatement(
                                "UPDATE arguments SET ? = ? + 1 WHERE poll_id=? AND arg_id=?");
                updateStatement.setString(1, yesOrNo);
                updateStatement.setString(2, yesOrNo);
                updateStatement.setInt(3, pollId);
                updateStatement.setInt(4, argumentId);
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in updateVotes");
        }
    }

    synchronized void updateScores(Integer pollId) {
        try {
            PreparedStatement getRoot =
                    connection.prepareStatement(
                            "SELECT * FROM arguments WHERE poll_id=? AND arg_id='0';");
            getRoot.setInt(1, pollId);

            ResultSet rootResult = getRoot.executeQuery();
            rootResult.next(); // So that we get the first returned row

            Argument root = new Argument(
                    true /* Fake */,
                    rootResult.getString("argument"),
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

    private void setArgumentChildren(Argument parent, Integer pollId) {
        try {
            PreparedStatement findChildren =
                    connection.prepareStatement(
                            "SELECT * FROM arguments WHERE poll_id=? AND parent_id=?");
            findChildren.setInt(1, pollId);
            findChildren.setInt(2, parent.getId());
            ResultSet children = findChildren.executeQuery();

            while (children.next()) {
                String text = children.getString("argument");
                boolean isSupporter = children.getString("type").equals("Pro");
                Argument child = new Argument(true /* Fake */, text, isSupporter);
                child.setId(children.getInt("arg_id"));
                child.setVotesFor(children.getInt("yes_votes"));
                child.setVotesAgainst(children.getInt("no_votes"));
                setArgumentChildren(child, pollId);
                parent.addChild(child);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in setArgumentChildren");
        }
    }

    void deleteFromDataBase(Integer pollId, String userId) {
        try {
            PreparedStatement deleteUser =
                    connection.prepareStatement(
                            "DELETE FROM answers WHERE poll_id=? AND 'user_id'=?;");
            deleteUser.setInt(1, pollId);
            deleteUser.setString(2, userId);

            deleteUser.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage() + "in deleteFromDataBase");
        }
    }
}
