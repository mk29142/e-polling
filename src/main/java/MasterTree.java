//package src.main.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rf2114 on 14/10/16.
 */

/*
Think about concurrency in this class as some of the functions update shared variables in the database
 */
public class MasterTree {

    private Node root;
    private List<Boolean> voteList;

    public MasterTree(Node root){
        this.root = root;
        this.voteList = new ArrayList<>();
    }

    public double getRootScore(){
        return root.getScore();
    }


    public List<Boolean> getVoteList(){
        return voteList;
    }
    /*
    updateScore updates the scores of all the nodes in the MasterTree, there may be a problem caused by concatenating
    the attackers and supporters list into children.
     */
    public void updateScore(List<Boolean> newArgumentList, Node node){
        List<Node> children = node.getChildren();

        for (Node child : children) {
            updateScore(newArgumentList, child);
        }

        boolean vote = newArgumentList.remove(0);
        if (vote) {
            node.incrementVotesFor();
        } else {
            node.incrementVotesAgainst();
        }

        node.combinationFunction();
    }

    /*
    Turns argument tree into list of votes, post order tree traversal is used
     */
    public void argumentToList(Argument argument){
        List<Argument> children = argument.getChildren();

        for (Argument child : children) {
            argumentToList(child);
        }

        voteList.add(argument.getVote());
    }

}

