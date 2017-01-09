import com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class PollTable {
    private Connection connection;

    PollTable(Connection connection) {
        this.connection = connection;
    }

    public List<Poll> getPolls() {
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
            System.out.println(e.getMessage() + " in DatabaseUtils.getPolls()");
            return null;
        }
    }

    public Integer addToTable(JsonObject obj) {
        PreparedStatement insertPoll;
        PreparedStatement findId;

        try {
            insertPoll = connection.prepareStatement("INSERT INTO polls(poll_name, email, password) VALUES (?, ?, ?);");

            findId = connection.prepareStatement("SELECT CURRVAL('polls_id_seq')");

            insertPoll.setString(1, obj.get("name").getAsString());
            insertPoll.setString(2, obj.get("email").getAsString());
            insertPoll.setString(3, obj.get("password").getAsString());
            insertPoll.executeUpdate();

            ResultSet rs = findId.executeQuery();
            rs.next();
            return rs.getInt("currval");

        } catch (SQLException e) {
            String errMessage = e.getMessage();
            System.out.println(errMessage);
            return -1;
        }
    }

}
