import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class DatabaseUtils {
    private Connection connection;

    DatabaseUtils(Connection connection) {
        this.connection = connection;
    }

    List<Poll> getPolls() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, poll_name FROM polls");

            List<Poll> polls = new ArrayList<>();

            while (rs.next()) {
                Poll p = new Poll(rs.getInt("id"), rs.getString("poll_name"));
                polls.add(p);
            }

            return polls;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
