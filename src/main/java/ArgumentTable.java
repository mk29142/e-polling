import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArgumentTable {

    Connection connection;

    public ArgumentTable(Connection connection) {
        this.connection = connection;
    }

    String addToTable(JsonArray argList, Integer pollId) {

        try {

            for (JsonElement elem : argList) {
                JsonObject elemObj = elem.getAsJsonObject();
                System.out.println(elemObj.toString());

                PreparedStatement addRow =
                        connection.prepareStatement("INSERT INTO arguments " +
                                "VALUES(?, ?, ?, ?, ?, ?, ?, ?::statement_type);");

                Integer statement_id = elemObj.get("id").getAsInt();

                addRow.setInt(1, pollId);
                addRow.setInt(2, statement_id);

                if (elemObj.get("parentId").isJsonNull()) {
                    addRow.setNull(3, Types.INTEGER);
                } else {
                    addRow.setInt(3, elemObj.get("parentId").getAsInt());
                }

                //addRow = connection.prepareStatement(addRow.toString().replace("'", "\""));

                addRow.setString(4, elemObj.get("value").getAsString());
                addRow.setFloat(5, 0);
                addRow.setInt(6, 0);
                addRow.setInt(7, 0);
                addRow.setString(8, elemObj.get("type").getAsString());
                addRow.executeUpdate();
            }

            return "SUCCESS";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "500 ERROR";
        }
    }

    List<GraphData> getGraphData(int pollId) {
        List<GraphData> graphData = new ArrayList<>();

        try {
            PreparedStatement getStatementData =
                    connection.prepareStatement(
                            "SELECT * FROM arguments WHERE poll_id=? ORDER BY arg_id;");
            getStatementData.setInt(1, pollId);

            ResultSet statementData = getStatementData.executeQuery();

            while (statementData.next()) {
                String text = statementData.getString("argument");
                int id = statementData.getInt("arg_id");
                Integer parentId = statementData.getInt("parent_id");
                int yesVotes = statementData.getInt("yes_votes");
                int noVotes = statementData.getInt("no_votes");
                float score = statementData.getFloat("score");

                graphData.add(new GraphData(
                        id, parentId, score, yesVotes, noVotes, text));
            }

            return graphData;
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in ArgumentTable.getGraphData");
            return new ArrayList<>();
        }
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
