import java.sql.Connection;

/*
 * Think about concurrency in this class as some of the functions update shared variables in the database
 */
public class MasterTree {
    private Argument root;

    public MasterTree(Connection connection, String pollId) {

    }

    /*
     * updateScore updates the scores of all the nodes in the MasterTree, there may be a problem caused by concatenating
     * the attackers and supporters list into children.
     */
    public void updateScores() {
        root.updateScore();

        updateDatabase();
    }

    private void updateDatabase() {

    }
}