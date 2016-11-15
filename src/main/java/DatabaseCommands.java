import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCommands {

    private Connection connection;

    public DatabaseCommands(Connection connection) {
        this.connection = connection;
    }

    public List<Poll> getAllPolls() {
      // Get all of the names of polls for listing
      List<Poll> polls = new ArrayList<>();
      try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, poll_name FROM polls");

            while (rs.next()) {
               Poll p = new Poll(rs.getInt("id"), rs.getString("poll_name"));
               polls.add(p);
            }
        } catch (SQLException e) {
          System.out.println(e.getMessage());
        }

        return polls;
    }


}
