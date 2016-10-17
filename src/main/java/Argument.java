package main.java;

import java.util.List;

public class Argument {
    private List<Argument> children;
    private String argumentTitle;
    private boolean vote;
    private boolean isSupporter;

    public Argument(boolean vote, String argumentTitle, boolean isSupporter) {
        this.argumentTitle = argumentTitle;
        this.vote = vote;
        this.isSupporter = isSupporter;
    }

    public boolean getVote() {
        return vote;
    }

    public boolean getIsSupporter() {
        return isSupporter;
    }

    public String getArgumentTitle() {
        return argumentTitle;
    }

    public List<Argument> getChildren() {
        return children;
    }

    public void setChildren(List<Argument> children) {
        this.children = children;
    }

    public boolean isStable() {
        return false;
    }

    public boolean hasVotedForSupporter() {
        for (Argument child : children) {
            if (child.getIsSupporter() && child.getVote()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasVotedForAttacker() {
        for (Argument child : children) {
            if (!child.getIsSupporter() && child.getVote()) {
                return true;
            }
        }

        return false;
    }
}