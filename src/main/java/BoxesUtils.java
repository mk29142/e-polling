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

    public BoxesUtils(Connection connection, String pollId, String ip) {
        this.connection = connection;
        this.pollId = pollId;
        this.ip = ip;
    }

    public Object getStatementBoxes() {
        // Checking if connection exists
        try {
            PreparedStatement ipCheck = connection.prepareStatement("SELECT " +
                    "EXISTS(SELECT * FROM ? ");
            ipCheck.setString(1, pollId + "_answers");

            PreparedStatement insertIp = connection.prepareStatement(ipCheck
                    .toString().replace("'", "\"") + "WHERE user_id=?);");
            insertIp.setString(1, ip);

            ResultSet rs = insertIp.executeQuery();
            rs.next();

            // If they have answered questions already do something to notify
            // user. If not then carry on.
            PreparedStatement findStatements = connection.prepareStatement("SELECT * FROM ?;");
            findStatements.setString(1, pollId);
            rs = connection.createStatement().executeQuery(findStatements.toString().replace("'", "\""));

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
