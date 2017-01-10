import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class UserAddedUtils {
    private final Connection connection;
    private final String pollId;

    UserAddedUtils(Connection connection, String pollId) {
        this.connection = connection;
        this.pollId = pollId;
    }

    String addNewArg(JsonObject newArg) {
        int parent = newArg.get("parent").getAsInt();
        String txt = newArg.get("text").getAsString();
        String type = newArg.get("type").getAsString();

        List<Argument> similar = getSimilarArgs(txt, type, parent);
        final int MAX_SIMILAR = 1;

        if (similar.size() >= MAX_SIMILAR) {
            addToPoll(parent, txt, type);
            removeFromUserAdded(similar);
            return "SUCCESS";
        }

        return addToUserAdded(parent, txt, type);
    }

    private void removeFromUserAdded(List<Argument> argsToDelete) {
        try {
            PreparedStatement delete =
                    connection.prepareStatement("DELETE FROM ? WHERE statement_id=?");
            delete.setString(1, pollId + "_user_added");
            delete = connection.prepareStatement(delete.toString().replace("'", "\""));

            for (Argument arg : argsToDelete) {
                delete.setInt(1, arg.getId());
                delete.execute();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addToPoll(int parent, String txt, String type) {
        try {
            PreparedStatement getCurrId =
                    connection.prepareStatement("SELECT count(*) FROM ?;");
            getCurrId.setString(1, pollId);
            getCurrId =
                    connection.prepareStatement(getCurrId.toString().replace("'", "\""));
            ResultSet idrs = getCurrId.executeQuery();
            idrs.next();

            Integer nextId = idrs.getInt("count");

            PreparedStatement add =
                    connection.prepareStatement("INSERT INTO ? " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?::statement_type);");
            add.setString(1, pollId);
            add = connection.prepareStatement(add.toString().replace("'", "\""));
            add.setInt(1, nextId);
            add.setInt(2, parent);
            add.setString(3, txt);
            add.setFloat(4, 0);
            add.setInt(5, 0);
            add.setInt(6, 0);
            add.setString(7, type);

            PreparedStatement addAnswerColumn =
                    connection.prepareStatement("ALTER TABLE ? ADD COLUMN ? BOOLEAN;");
            addAnswerColumn.setString(1, pollId + "_answers");
            addAnswerColumn.setString(2, nextId.toString());
            addAnswerColumn =
                    connection.prepareStatement(addAnswerColumn.toString().replace("'", "\""));

            addAnswerColumn.executeUpdate();
            add.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private List<Argument> getSimilarArgs(String txt, String type, int parent) {
        List<Argument> comparable = grabComparableStatements(type, parent);
        final double threshold = 0.75;
        return comparable.stream()
                .filter(arg -> NLPUtils.checkStrings(arg.getText(), txt) > threshold)
                .collect(Collectors.toList());
    }

    private String addToUserAdded(
            int parent,
            String txt,
            String type) {
        try {
            PreparedStatement newArg =
                    connection.prepareStatement("INSERT INTO ? (parent_id, statement, type)"
                            + "VALUES (?, ?, ?::statement_type);");
            newArg.setString(1, pollId + "_user_added");
            newArg = connection.prepareStatement(
                    newArg.toString().replace("'", "\""));

            newArg.setInt(1, parent);
            newArg.setString(2, txt);
            newArg.setString(3, type);

            newArg.execute();

            return "SUCCESS";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "FAIL";
        }
    }

    // Comparable means those with the same type and parent
    private List<Argument> grabComparableStatements(String type, int parentId) {
        List<Argument> args = new ArrayList<>();

        try {
            PreparedStatement ps =
                    connection.prepareStatement("SELECT * FROM ?" +
                            " WHERE type=?::statement_type AND parent_id=?;");
            ps.setString(1, pollId + "_user_added");
            ps = connection.prepareStatement(ps.toString().replace("'", "\""));
            ps.setString(1, type);
            ps.setInt(2, parentId);

            ResultSet results = ps.executeQuery();
            while (results.next()) {
                Argument arg = new Argument(
                        true, // FAKE
                        results.getString("statement"),
                        type.equals("Pro"));
                arg.setId(results.getInt("statement_id"));
                args.add(arg);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return args;
    }
}
