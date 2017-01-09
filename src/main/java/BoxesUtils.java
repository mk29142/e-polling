import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class BoxesUtils {
    private Connection connection;

    BoxesUtils(Connection connection) {
        this.connection = connection;
    }

    List<Box> getStatementBoxes(Integer pollId) {
        // Checking if connection exists
        try {
            // If they have answered questions already do something to notify
            // user. If not then carry on.
            PreparedStatement findStatements = connection.prepareStatement(
                    "SELECT * FROM arguments WHERE poll_id=? ORDER BY 'arg_id';");
            findStatements.setInt(1, pollId);
            ResultSet rs = connection.createStatement().executeQuery(
                    findStatements.toString().replace("'", "\""));

            List<Box> boxes = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("arg_id");
                int parentId = rs.getInt("parent_id");
                String statement = rs.getString("argument");
                String type = rs.getString("type");
                boxes.add(new Box(
                        id,
                        parentId,
                        statement,
                        type,
                        /* Dummy as this is not used yet */"Against"));
            }

            return boxes;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }
}
