import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Argument {
    /*
     * Use score function (sigma) from DF-QuAD algorithm to calculate strength
     * of argument using the vote base score as the base score.
     */
    private float score;

    private int votesFor;
    private int votesAgainst;

    private boolean isSupporter;
    private List<Argument> children;
    private String text;
    private int parent;
    private int id;

    // 10 being strongly agree, and 1 being strongly disagree
    // True for agreement/yes, false for disagreement/no.
    private boolean vote;

    public void setParent(int parent){
        this.parent = parent;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    Argument(boolean vote, String argumentTitle, boolean isSupporter) {
        this.text = argumentTitle;
        this.vote = vote;
        this.isSupporter = isSupporter;
        this.children = new ArrayList<>();
    }

    Argument(JsonObject arg){
        this.vote = arg.get("support").getAsString().equals("yes");
        this.text = arg.get("text").getAsString();
        this.isSupporter = arg.get("type").getAsString().equals("Pro");
        this.parent = arg.get("parent").getAsInt();
        this.id = arg.get("id").getAsInt();
        this.children = new ArrayList<>();
    }

    boolean getVote() {
        return vote;
    }
    private boolean isSupporter() { return isSupporter;}

    void setVotesFor(int votesFor) {
        this.votesFor = votesFor;
    }

    void setVotesAgainst(int votesAgainst) {
        this.votesAgainst = votesAgainst;
    }

    public String getText() {
        return text;
    }

    // Returns box for sending back Argument in JSON
    Box toBox() {
        return new Box(
                id,
                parent,
                text,
                isSupporter ? "Pro" : "Con",
                vote ? "For" : "Against");
    }

    void addChild(Argument child) {
        this.children.add(child);
    }

    /*
     * Checks that this argument is consistent with its supporters and
     * attackers.
     * If there are no supporters or attackers, it is consistent.
     * If we agree with the statement, but agree with any of its attackers and
     * don't agree with any of its supporters, then it is inconsistent.
     * If we disagree with the statement, but agree with any of its supporters
     * and don't agree with any of its attackers, then it is inconsistent.
     * Otherwise, it is consistent.
     */
    List<Argument> getInconsistencies() {
        List<Argument> inconsistencies = new ArrayList<>();

        List<Argument> attackers = attackersAgreedWith();
        List<Argument> supporters = supportersAgreedWith();

        if (this.vote) {
            if (supporters.isEmpty()) {
                inconsistencies.add(this);
                inconsistencies.addAll(getAllSupporters());
                return inconsistencies;
            }
        } else {
            if (attackers.isEmpty()) {
                inconsistencies.add(this);
                inconsistencies.addAll(getAllAttackers());
                return inconsistencies;
            }
        }

        for (Argument child : children){
            List<Argument> currInconsistencies = child.getInconsistencies();
            if(!currInconsistencies.isEmpty()){
                inconsistencies = currInconsistencies;
                break;
            }
        }

        return inconsistencies;
    }

    /*
     * Concatenates Attacker and Supporters and returns list of Arguments,
     * this is used in class MasterTree for the argumentToList() function
     */
    List<Argument> getChildren(){
        return children;
    }

    private List<Argument> supportersAgreedWith() {
        return this.children.stream()
                .filter(child -> child.getVote() && child.isSupporter())
                .collect(Collectors.toList());
    }

    private List<Argument> attackersAgreedWith() {
        return this.children.stream()
                .filter(child -> child.getVote() && !child.isSupporter())
                .collect(Collectors.toList());
    }

    /*
     * This recursive aggregation function is defined in the notes,
     * it returns the aggregate of a List of scores for the supporters/attackers
     * of an argument.
     * (The higher the scores in the list, the higher the aggregate, as one would expect)
     */
    private float strengthAggregationFunction(List<Float> s) {
        switch (s.size()) {
            case 0: return 0;
            case 1: return s.get(0);
            case 2: return this.baseFunction(s.get(0), s.get(1));
            default:
                float argScore = s.remove(s.size() - 1);
                return this.baseFunction(this.strengthAggregationFunction(s), argScore);
        }
    }

    /*
     * This function "base function" is defined in the notes and is exclusively
     * used (so far) as
     * part of the aggregation function
     */
    private float baseFunction(float score1, float score2) {
        return score1 + score2 - (score1 * score2);
    }

    private List<Argument> getAllAttackers() {
        return children
                .stream()
                .filter(u -> !u.isSupporter())
                .collect(Collectors.toList());
    }

    private List<Argument> getAllSupporters() {
        return children
                .stream()
                .filter(Argument::isSupporter)
                .collect(Collectors.toList());
    }

    /*
     * The combination function gives the score of an argument and is based on
     * the scores of its attackers/supporters
     * and the base score of the argument. (It is also defined in the notes)
     */
    private void combinationFunction() {
        List<Float> attackerScoreList = scoreList(getAllAttackers());
        float attackerScore =
                strengthAggregationFunction(attackerScoreList);
        List<Float> supporterScoreList = scoreList(getAllSupporters());
        float supporterScore =
                strengthAggregationFunction(supporterScoreList);

        float baseScore = getBaseScore();

        if (attackerScore >= supporterScore) {
            this.score = baseScore -
                    baseScore * Math.abs(supporterScore - attackerScore);
        } else {
            this.score = baseScore +
                    (1 - baseScore) * Math.abs(supporterScore - attackerScore);
        }
    }

    /*
     * scoreList turns a list of nodes into a list of scores and is needed in the
     * combination function
     */
    private List<Float> scoreList(List<Argument> argList) {
        return argList.stream()
                .map(arg -> arg.score)
                .collect(Collectors.toList());
    }

    /*
     * getBaseScore is a simple function that is defined in the notes and is
     * calculated based on the user votes for and against an argument
     */
    private float getBaseScore() {
        int totalVotes = this.votesFor + this.votesAgainst;
        if (totalVotes == 0) {
            return 0.5f;
        } else {
            return 0.5f + (0.5f * (this.votesFor - this.votesAgainst) / totalVotes);
        }
    }

    void updateScore(Connection connection, String pollId) {
        for (Argument child : this.children) {
            child.updateScore(connection, pollId);
        }

        this.combinationFunction(); // To set our score

        this.putScoreInDb(connection, pollId);
    }

    private void putScoreInDb(Connection connection, String pollId) {
        try {
            PreparedStatement updateScore =
                    connection.prepareStatement(
                            "UPDATE ? SET score = ? WHERE 'statement_id'=?");
            updateScore.setString(1, pollId);
            updateScore.setFloat(2, this.score);
            updateScore.setInt(3, this.id);
            updateScore =
                    connection.prepareStatement(
                            updateScore.toString().replace("'", "\""));
            updateScore.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in putScoreInDb: " + this.id);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(this.id);
        sb.append(" score: ").append(this.score);

        return sb.toString();
    }
}
