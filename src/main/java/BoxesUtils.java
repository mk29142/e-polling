import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BoxesUtils {
    private Connection connection;
    private String pollId;
    private String ip;

    public BoxesUtils(Connection connection, String pollId) {
        this.connection = connection;
        this.pollId = pollId;
    }

    public Object getStatementBoxes() {
        // Checking if connection exists
        try {
            // If they have answered questions already do something to notify
            // user. If not then carry on.
            PreparedStatement findStatements = connection.prepareStatement("SELECT * FROM ? ORDER BY 'statement_id';");
            findStatements.setString(1, pollId);
            ResultSet rs = connection.createStatement().executeQuery(findStatements.toString().replace("'", "\""));

            List<Box> boxes = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("statement_id");
                int parentId = rs.getInt("parent_id");
                String statement = rs.getString("statement");
                String type = rs.getString("type");
                boxes.add(new Box(id, parentId, statement, type));
            }

            return boxes;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "500 Error";
        }
    }
}
