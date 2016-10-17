package main.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rf2114 on 14/10/16.
 */
public class MasterTree {

    private Node root;
    List<Boolean> voteList;

    public MasterTree(Node root){
        this.root = root;
    }

    /*
    updateScore updates the scores of all the nodes in the MasterTree, there may be a problem caused by concatenating
    the attackers and supporters list into children.
     */
    public void updateScore(List<Boolean> newArgumentList, Node node){
        List<Node> children = node.getChildren();
        for(Node child : children){
            updateScore(newArgumentList,child);
        }
        boolean vote = newArgumentList.get(0);
        if(vote){
            node.incrementVotesFor();
        } else {
            node.incrementVotesAgainst();
        }
        node.combinationFunction();
        newArgumentList.remove(0);
    }

    /*
    Turns argument tree into list of votes, post order tree traversal is used
     */
    public void argumentToList(Argument argument){

        List<Argument> children = argument.getChildren();
        for(Argument child : children){
            argumentToList(child);
        }
        voteList.add(argument.getVote());
    }

}


class Node {

    private double score;
    private int votesFor;
    private int votesAgainst;
    private List<Node> attackers;
    private List<Node> supporters;

    public Node(){

    }

    public void incrementVotesFor(){
        votesFor++;
    }

    public void incrementVotesAgainst(){
        votesAgainst++;
    }



    /*
   This function is used by updateScore() in the MasterTree class and is necessary for post-order tree traversal
    */
    public List<Node> getChildren(){
        List<Node> result = new ArrayList<Node>(attackers);
        result.add((Node)supporters);
        return result;

    }

    /*
    This recursive aggregation function is defined in the notes,
    it returns the aggregate of a List of scores for the supporters/attackers of an argument.
    (The higher the scores in the list, the higher the aggregate, as one would expect)
     */
    public double strengthAggregationFunction(List<Double> S){

        switch(S.size()){
            case 0: return 0;
            case 1: return S.get(0);
            case 2: return baseFunction(S.get(0), S.get(1));
            default:
                double argScore = S.get(S.size());
                S.remove(S.size());
                return baseFunction(strengthAggregationFunction(S), argScore);
        }

    }
    /*
    This function "base function" is defined in the notes and is exclusively used (so far) as
    part of the aggregation function
     */
    public double baseFunction(double score1, double score2) {

        return score1 + score2 - (score1 * score2);

    }

    /*
    The combination function gives the score of an argument and is based on the scores of its attackers/supporters
    and the base score of the argument. (It is also defined in the notes)
     */
    public double combinationFunction() {
        List<Double> attackerScoreList = scoreList(attackers);
        double attackerScore = strengthAggregationFunction(attackerScoreList);
        List<Double> supporterScoreList = scoreList(supporters);
        double supporterScore = strengthAggregationFunction(attackerScoreList);

        double baseScore = getBaseScore();

        if(attackerScore >= supporterScore){

            score = baseScore - baseScore * Math.abs(supporterScore - attackerScore);

        } else {
            score = baseScore + (1 - baseScore) * Math.abs(supporterScore - attackerScore);
        }


    }

    /*
    Simple getter that is used in scoreList (below)
     */
    public double getScore(){
        return score;
    }
    /*
    scoreList turns a list of nodes into a list of scores and is needed in the combination function
     */
    public List<Double> scoreList(List<Node> nodeList){
        List<Double> scoreList = new ArrayList<Double>();
        for(Node node : nodeList){
            scoreList.add(node.getScore());
        }
        return scoreList;
    }

    /*
    getBaseScore is a simple function that is defined in the notes and is calculated based on the user votes for and
    against an argument
     */
    public double getBaseScore() {
        int totalVotes = votesFor + votesAgainst;
        if (totalVotes == 0) {
            return 0.5;
        } else {
            return 0.5 + (0.5 * (votesFor - votesAgainst) / totalVotes);
        }

    }


}