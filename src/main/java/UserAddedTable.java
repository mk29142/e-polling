import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class UserAddedTable {
    private final Connection connection;

    UserAddedTable(Connection connection) {
        this.connection = connection;
    }

    String addNewArg(JsonObject newArg, Integer pollId) {
        int parent = newArg.get("parent").getAsInt();
        String txt = newArg.get("text").getAsString();
        String type = newArg.get("type").getAsString();

        List<Argument> similar = getSimilarArgs(txt, type, parent, pollId);
        final int MAX_SIMILAR = 1;

        if (similar.size() >= MAX_SIMILAR) {
            removeFromUserAdded(similar, pollId);
            return addToPoll(parent, txt, type, pollId);
        }

        return addToUserAdded(parent, txt, type, pollId);
    }

    private void removeFromUserAdded(List<Argument> argsToDelete, Integer pollId) {
        try {
            PreparedStatement delete =
                    connection.prepareStatement("DELETE FROM user_added WHERE arg_id=? AND poll_id=?");
            delete.setInt(2, pollId);

            for (Argument arg : argsToDelete) {
                delete.setInt(1, arg.getId());
                delete.execute();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " in UserAddedTable.removeFromUserAdded()");
        }
    }

    private String addToPoll(int parent, String txt, String type, Integer pollId) {
        try {
            PreparedStatement getCurrId =
                    connection.prepareStatement("SELECT count(*) FROM arguments WHERE poll_id=?;");
            getCurrId.setInt(1, pollId);
            ResultSet idrs = getCurrId.executeQuery();
            idrs.next();

            Integer nextId = idrs.getInt("count");

            PreparedStatement add =
                    connection.prepareStatement("INSERT INTO arguments " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?::statement_type);");
            add.setInt(1, pollId);
            add.setInt(2, nextId);
            add.setInt(3, parent);
            add.setString(4, txt);
            add.setFloat(5, 0);
            add.setInt(6, 0);
            add.setInt(7, 0);
            add.setString(8, type);

            add.execute();

            return "SUCCESS";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "FAIL";
        }
    }

    private List<Argument> getSimilarArgs(String txt, String type, int parent, Integer pollId) {
        List<Argument> comparable = grabComparableStatements(type, parent, pollId);
        final double threshold = 0.75;
        return comparable.stream()
                .filter(arg -> {
                    double sim = NLPUtils.checkStrings(arg.getText(), txt);
                    System.out.println(arg.getText() + " vs " + txt + " = " + sim);
                    return sim  > threshold;
                })
                .collect(Collectors.toList());
    }

    private String addToUserAdded(
            int parent,
            String txt,
            String type,
            Integer pollId) {
        try {
            PreparedStatement newArg =
                    connection.prepareStatement("INSERT INTO user_added (poll_id, parent_id, argument, type)"
                            + "VALUES (?, ?, ?, ?::statement_type);");
            newArg.setInt(1, pollId);
            newArg.setInt(2, parent);
            newArg.setString(3, txt);
            newArg.setString(4, type);

            newArg.execute();

            return "SUCCESS";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "FAIL";
        }
    }

    // Comparable means those with the same type and parent
    private List<Argument> grabComparableStatements(String type, int parentId, Integer pollId) {
        List<Argument> args = new ArrayList<>();

        try {
            PreparedStatement ps =
                    connection.prepareStatement("SELECT * FROM user_added" +
                            " WHERE poll_id=? AND type=?::statement_type AND parent_id=?;");
            ps.setInt(1, pollId);
            ps.setString(2, type);
            ps.setInt(3, parentId);

            ResultSet results = ps.executeQuery();
            while (results.next()) {
                Argument arg = new Argument(
                        true, // FAKE
                        results.getString("argument"),
                        type.equals("Pro"));
                arg.setId(results.getInt("arg_id"));
                args.add(arg);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return args;
    }
}
