import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Argument {
    private double score;

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

    /*
     * Use score function (sigma) from DF-QuAD algorithm to calculate strength
     * of argument using the vote base score as the base score.
     */
    private double strength;

    Argument(boolean vote, String argumentTitle, boolean isSupporter) {
        this.text = argumentTitle;
        this.vote = vote;
        this.isSupporter = isSupporter;
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

    public String getText() {
        return text;
    }

    // Returns box for sending back Argument in JSON
    public Box toBox() {

        String type = isSupporter ? "Pro" : "Con";
        return new Box(id, parent, text, type);
    }

    public void addChild(boolean vote, String argumentTitle,
            boolean isSupporter) {
        Argument child = new Argument(vote, argumentTitle, isSupporter);
        this.children.add(child);
    }

    public void addChild(Argument child) {
        this.children.add(child);
    }

    public void addChildren(List<Argument> children) {
        this.children.addAll(children);
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

    /*
     * Concatenates Attacker and Supporters and returns list of Arguments,
     * this is used in class MasterTree for the argumentToList() function
     */
    List<Argument> getChildren(){
        return children;
    }

    /*
     * Returns true if the subtree with this argument as the root is
     * consistent, which is when the root and all of its children are
     * consistent.
     */
    public boolean isSubTreeConsistent() {
        for (Argument child : children) {
            if (!child.isSubTreeConsistent()) {
                return false;
            }
        }

        return this.getInconsistencies().isEmpty();
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
    public double strengthAggregationFunction(List<Double> S) {
        switch(S.size()) {
            case 0: return 0;
            case 1: return S.get(0);
            case 2: return baseFunction(S.get(0), S.get(1));
            default:
                double argScore = S.remove(S.size());
                return baseFunction(strengthAggregationFunction(S), argScore);
        }
    }

    /*
     * This function "base function" is defined in the notes and is exclusively
     * used (so far) as
     * part of the aggregation function
     */
    public double baseFunction(double score1, double score2) {
        return score1 + score2 - (score1 * score2);
    }

    public List<Argument> getAllAttackers() {
        return children
                .stream()
                .filter(u -> !u.isSupporter())
                .collect(Collectors.toList());
    }

    public List<Argument> getAllSupporters() {
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
    public void combinationFunction() {
        List<Double> attackerScoreList = scoreList(getAllAttackers());
        double attackerScore =
                strengthAggregationFunction(attackerScoreList);
        List<Double> supporterScoreList = scoreList(getAllSupporters());
        double supporterScore =
                strengthAggregationFunction(supporterScoreList);

        double baseScore = getBaseScore();

        if (attackerScore >= supporterScore) {
            score = baseScore -
                    baseScore * Math.abs(supporterScore - attackerScore);
        } else {
            score = baseScore +
                    (1 - baseScore) * Math.abs(supporterScore - attackerScore);
        }
    }

    /*
     * Simple getter that is used in scoreList (below)
     */
    public double getScore() {
        return score;
    }

    /*
     * scoreList turns a list of nodes into a list of scores and is needed in the
     * combination function
     */
    public List<Double> scoreList(List<Argument> argList) {
        List<Double> scoreList = new ArrayList<>();

        for (Argument arg : argList) {
            scoreList.add(arg.getScore());
        }

        return scoreList;
    }

    /*
     * getBaseScore is a simple function that is defined in the notes and is
     * calculated based on the user votes for and against an argument
     */
    public double getBaseScore() {
        int totalVotes = votesFor + votesAgainst;
        if (totalVotes == 0) {
            return 0.5;
        } else {
            return 0.5 + (0.5 * (votesFor - votesAgainst) / totalVotes);
        }
    }

    public void updateScore() {
        this.children.forEach(Argument::updateScore);

        System.out.println("Hello");
    }
}
