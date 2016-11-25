import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.*;

class CreateUtils {
    private Connection connection;
    private JsonElement element;

    CreateUtils(Connection connection, JsonElement element) {
        this.connection = connection;
        this.element = element;
    }

    String createPoll() {
        PreparedStatement insertPoll;
        PreparedStatement findId;
        PreparedStatement createPoll;
        PreparedStatement createAnswers;
        JsonArray list;

        try {
            insertPoll = connection.prepareStatement("INSERT INTO polls(poll_name) VALUES (?);");

            findId = connection.prepareStatement("SELECT CURRVAL('polls_id_seq')");
            createPoll = connection.prepareStatement("CREATE TABLE ? " +
                    "(statement_id INT NOT NULL, " +
                    "parent_id INT, " +
                    "level INT NOT NULL, " +
                    "statement TEXT NOT NULL, " +
                    "score REAL," +
                    "yes_votes INT NOT NULL," +
                    "no_votes INT NOT NULL," +
                    "type statement_type);");
            createAnswers = connection.prepareStatement("CREATE TABLE ? " +
                    "(user_id TEXT, stupidity INT);");

            JsonObject obj = element.getAsJsonObject();
            list = obj.getAsJsonArray("list");
            String name = obj.get("name").getAsString();

            insertPoll.setString(1, name);
        } catch (SQLException e) {
            String errMessage = e.getMessage();
            System.out.println(errMessage);
            return errMessage;
        }

        try {
            insertPoll.executeUpdate();
            ResultSet rs = findId.executeQuery();
            rs.next();
            Integer id = rs.getInt("currval");
            createPoll.setString(1, id.toString());
            createAnswers.setString(1, id.toString() + "_answers");
            connection.createStatement().execute(createPoll.toString().replace("'", "\""));
            connection.createStatement().execute(createAnswers.toString().replace("'", "\""));

            for (JsonElement elem : list) {
                JsonObject elemObj = elem.getAsJsonObject();
                System.out.println(elemObj.toString());

                try {
                    PreparedStatement addRow = connection.prepareStatement
                            ("INSERT INTO ? VALUES(?, ?, ?, ?, ?, ?, ?, ?::statement_type);");

                    //this statement adds a column to the answers table
                    PreparedStatement addAnswerColumn = connection.prepareStatement("ALTER TABLE ? ADD COLUMN ? BOOLEAN;");

                    Integer statement_id = elemObj.get("id").getAsInt();

                    addRow.setString(1, id.toString());
                    addRow.setInt(2, statement_id);

                    addAnswerColumn.setString(1, id.toString() + "_answers");
                    addAnswerColumn.setString(2, statement_id.toString());

                    if (elemObj.get("parentId").isJsonNull()) {
                        addRow.setNull(3, Types.INTEGER);
                    } else {
                        addRow.setInt(3, elemObj.get("parentId").getAsInt());
                    }

                    addRow.setInt(4, elemObj.get("level").getAsInt());

                    addRow = connection.prepareStatement(addRow.toString().replace("'", "\""));
                    addAnswerColumn = connection.prepareStatement(addAnswerColumn.toString().replace("'", "\""));

                    addRow.setString(1, elemObj.get("value").getAsString());
                    addRow.setFloat(2, 0);
                    addRow.setInt(3, 0);
                    addRow.setInt(4, 0);
                    addRow.setString(5, elemObj.get("type").getAsString());
                    addRow.executeUpdate();
                    addAnswerColumn.executeUpdate();
                } catch (SQLException | UnsupportedOperationException e) {
                    System.out.print("FAILED: ");
                    System.out.println(e.getMessage());
                }
            }

            return id.toString();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "500 ERROR";
        }
    }
}
