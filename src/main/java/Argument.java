import com.google.gson.JsonArray;
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
    private float baseScore;

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

    public int getId() {
        return id;
    }

    public int getParent(){ return parent;}

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
    boolean isSupporter() { return isSupporter;}

    public void setVotesFor(int votesFor) {
        this.votesFor = votesFor;
    }

    public void setVotesAgainst(int votesAgainst) {
        this.votesAgainst = votesAgainst;
    }

    // Returns box for sending back Argument in JSON
    public Box toBox() {
        return new Box(id, parent, text, isSupporter ? "Pro" : "Con", vote ? "For" : "Against");
    }

    public void addChild(Argument child) {
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
    public List<Argument> getInconsistencies() {
        List<Argument> attackers = attackersAgreedWith();
        List<Argument> supporters = supportersAgreedWith();

        if (this.vote) {
            if (supporters.isEmpty()) {
                return getAllSupporters();
            }
        } else {
            if (attackers.isEmpty()) {
                return getAllAttackers();
            }
        }

        return new ArrayList<>();
    }

    private List<Argument> supportersAgreedWith() {
        List<Argument> supporters = new ArrayList<>();
        for (Argument child : this.children) {
            if (child.getVote() && child.isSupporter()) {
                supporters.add(child);
            }
        }

        return supporters;
    }

    private List<Argument> attackersAgreedWith() {
        List<Argument> attackers = new ArrayList<>();
        for (Argument child : this.children) {
            if (child.getVote() && !child.isSupporter()) {
                attackers.add(child);
            }
        }

        return attackers;
    }

    /*
     * This recursive aggregation function is defined in the notes,
     * it returns the aggregate of a List of scores for the supporters/attackers
     * of an argument.
     * (The higher the scores in the list, the higher the aggregate, as one would expect)
     */
    private float strengthAggregationFunction(List<Float> S) {
        switch(S.size()) {
            case 0: return 0;
            case 1: return S.get(0);
            case 2: return this.baseFunction(S.get(0), S.get(1));
            default:
                float argScore = S.remove(S.size());
                return this.baseFunction(this.strengthAggregationFunction(S), argScore);
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
                .filter(u -> u.isSupporter())
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

        this.baseScore = getBaseScore();

        if (attackerScore >= supporterScore) {
            this.score = baseScore -
                    baseScore * Math.abs(supporterScore - attackerScore);
        } else {
            this.score = baseScore +
                    (1 - baseScore) * Math.abs(supporterScore - attackerScore);
        }
    }

    /*
     * Simple getter that is used in scoreList (below)
     */
    private float getScore() {
        return this.score;
    }

    /*
     * scoreList turns a list of nodes into a list of scores and is needed in the
     * combination function
     */
    private List<Float> scoreList(List<Argument> argList) {
        List<Float> scoreList = new ArrayList<>();

        for (Argument arg : argList) {
            scoreList.add(arg.getScore());
        }

        return scoreList;
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

    public void updateScore(Connection connection, String pollId) {
        for (Argument child : this.children) {
            child.updateScore(connection, pollId);
        }

        this.combinationFunction(); // To set our score

        this.putScoreInDb(connection, pollId);
    }

    private void putScoreInDb(Connection connection, String pollId) {
        try {
            PreparedStatement updateScore = connection.prepareStatement("UPDATE ? SET ? = ? WHERE 'statement_id'=?");
            updateScore.setString(1, pollId);
            updateScore.setString(2, "score");
            updateScore.setFloat(3, this.score);
            updateScore.setInt(4, this.id);
            updateScore = connection.prepareStatement(updateScore.toString().replace("'", "\""));
            updateScore.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in putScoreInDb: " + this.id);
        }
    }
}
