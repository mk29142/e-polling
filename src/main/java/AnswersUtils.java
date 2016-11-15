import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sp5714 on 15/11/16.
 */
public class AnswersUtils {

    private Connection connection;
    private String pollId;
    private String ip;

    public AnswersUtils(Connection connection, String pollId, String ip) {
        this.connection = connection;
        this.pollId = pollId;
        this.ip = ip;
    }

    public void enterAnswersIntoDatabase(JsonArray answers) {
        // Go through head's adding changed vote values (for first run all answers given)
        for (int i = 0; i < answers.size(); i++) {
            JsonArray headAnswers = answers.get(i).getAsJsonArray();

            // go through a single head
            for (int j = 0; j < headAnswers.size(); j++) {
                JsonElement elem = headAnswers.get(j);
                JsonObject answer = elem.getAsJsonObject();

                boolean vote;
                try {
                    vote = answer.get("support").getAsString().equals("yes");
                } catch (Exception e) {
                    //In all other cases than the first "support" is not a field so
                    //we don't update it with anything
                    continue;
                }

                Integer id = answer.get("id").getAsInt();

                try {
                    PreparedStatement insertAnswer = connection.prepareStatement("UPDATE ? SET ?=");
                    insertAnswer.setString(1, pollId + "_answers");
                    insertAnswer.setString(2, id.toString());

                    PreparedStatement insertValues = connection.prepareStatement(insertAnswer.toString().replace("'", "\"")
                            + "? WHERE user_id=?;");

                    insertValues.setBoolean(1, vote);
                    insertValues.setString(2, ip);

                    insertValues.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public String resolveDynamicQuestions(JsonObject data) {
        //Pull from the database into argument objects
        try {
            //get row of answers for user
            PreparedStatement getUserAnswers = connection.prepareStatement("SELECT * FROM ? WHERE user_id=");
            getUserAnswers.setString(1, pollId + "_answers");
            PreparedStatement getAnswers = connection.prepareStatement(getUserAnswers.toString().replace("'", "\"") + "?;");
            getAnswers.setString(1, ip);

            // get all answers
            ResultSet rs = getAnswers.executeQuery();
            rs.next(); //rs is now the first row from the answers table with user_id (should be unique

            Integer nextLevel = data.get("nextLevel").getAsInt();

            List<List<Box>> dynamicQuestions;
            ResultSet headIds;
            do {
                dynamicQuestions = new ArrayList<>(); //1st elem of each inner list is the head

                //get all id's for a level
                PreparedStatement getHeads = connection.prepareStatement("SELECT \"statement_id\" FROM ? WHERE \"level\"=");
                getHeads.setString(1, pollId);
                PreparedStatement getHeadIds = connection.prepareStatement(getHeads.toString().replace("'","\"") + nextLevel);
                headIds = getHeadIds.executeQuery();

                //this case will occur when we go past last level of tree
                if (!headIds.isBeforeFirst()) {
                    return "STOP";
                }

                //for each head find its inconsistencies and store it in a list of boxes

                while (headIds.next()) {

                    Integer currentHead = headIds.getInt("statement_id");
                    Argument head;
                    List<Argument> inconsistencies = new ArrayList<>();

                    //get parent = currenthead and children rows in poll table where parent_id = currentHead
                    PreparedStatement getValues = connection.prepareStatement("SELECT * FROM ? WHERE parent_id=");
                    getValues.setString(1, pollId);
                    PreparedStatement getChildren = connection.prepareStatement(getValues.toString().replace("'", "\"") + "? OR" +
                            " statement_id=? ORDER BY statement_id");
                    getChildren.setInt(1, currentHead);
                    getChildren.setInt(2, currentHead);

                    ResultSet children = getChildren.executeQuery();
                    //set of all rows for relevant nodes in tree
                    if (children.isBeforeFirst()) { //only true if there are children (ignore heads without children)

                        children.next(); // head is the first here as it has the lowest index

                        head = new Argument(rs.getBoolean("0"), children.getString("statement"), children.getString("type").equals("Pro"));
                        head.setId(children.getInt("statement_id"));

                        //while there is a row for a child
                        while (children.next()) {
                            Integer argumentId = children.getInt("statement_id");
                            Integer parentId = children.getInt("parent_id");
                            Argument arg = new Argument(rs.getBoolean(argumentId.toString()), children.getString("statement"),
                                    children.getString("type").equals("Pro"));
                            arg.setId(argumentId);
                            arg.setParent(parentId);

                            head.addChild(arg);
                        }

                        inconsistencies = head.getInconsistencies();

                        //if there are inconsistencies then store them with their head node
                        if (!inconsistencies.isEmpty()) {
                            List<Box> headInconsistencies = new ArrayList<>();
                            headInconsistencies.add(0, head.toBox());

                            for (Argument a : inconsistencies) {
                                headInconsistencies.add(a.toBox());
                            }
                            dynamicQuestions.add(headInconsistencies);
                        }


                    }
                }

                nextLevel++;

            } while (dynamicQuestions.isEmpty());


            for (List<Box> lb : dynamicQuestions) {
                System.out.println("---------------");
                for (Box b : lb) {
                    System.out.println(b.text);
                }
                System.out.println("---------------");
            }

            JsonTransformer jT = new JsonTransformer();
            return jT.render(new DynamicData(dynamicQuestions, nextLevel));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "500 ERROR";
        }
    }
}
