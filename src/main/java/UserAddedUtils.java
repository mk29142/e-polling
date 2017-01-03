import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class UserAddedUtils {
    private final Connection connection;
    private final String pollId;

    public UserAddedUtils(Connection connection, String pollId) {
        this.connection = connection;
        this.pollId = pollId;
    }

    public String addNewArg(JsonObject newArg) {
        String userId = newArg.get("userId").getAsString();
        int id = newArg.get("id").getAsInt();
        int parent = newArg.get("parent").getAsInt();
        String txt = newArg.get("text").getAsString();
        String type = newArg.get("type").getAsString();
        return addToTable(id, parent, txt, type, userId);
    }

    private String addToTable(
            int id,
            int parent,
            String txt,
            String type,
            String userId) {
        try {
            PreparedStatement newArg =
                    connection.prepareStatement("INSERT INTO ?"
                            + "VALUES (?, ?, ?, ?::statement_type, ?);");
            newArg.setString(1, pollId + "_user_added");
            newArg = connection.prepareStatement(
                    newArg.toString().replace("'", "\""));

            newArg.setInt(1, id);
            newArg.setInt(2, parent);
            newArg.setString(3, txt);
            newArg.setString(4, type);
            newArg.setString(5, userId);

            newArg.execute();

            return "SUCCESS";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "FAIL";
        }
    }
}
