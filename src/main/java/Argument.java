package main.java;

import java.util.List;

public class Argument {
    private List<Supporter> supporters; 
    private List<Attacker> attackers;
    private String argumentTitle;
    //TODO: Should probably change to be a number on a scale of 1 to 10, with 10 being strongly agree, and 1 being strongly disagree
    /* True for agreement/yes, false for disagreement/no. */
    private boolean vote;
    /* Use score function (sigma) from DF-QuAD algorithm to calculate strength 
     * of argument using the vote base score as the base score. */ 
    private double strength;

    public Argument(boolean vote, String argumentTitle) {
        this.argumentTitle = argumentTitle;
        this.vote = vote;
    }

    public boolean getVote() {
        return vote;
    }

    public String getArgumentTitle() {
        return argumentTitle;
    }

    public void addSupporter(boolean vote, String argumentTitle) {
        Supporter supporter = new Supporter(vote, argumentTitle);
        this.supporters.add(supporter);
    }
    
    public void addAttacker(boolean vote, String argumentTitle) {
        Attacker attacker = new Attacker(vote, argumentTitle);
        this.attackers.add(attacker);
    }

    //TODO: Implement algorithm to check that the tree is fully consistent (IS THIS WHAT isStable() was meant to be doing)
    /* Checks that this argument is consistent with its supporters and 
     * attackers.
     * If there are no supporters or attackers, it is consistent.
     * If we agree with the statement, but agree with any of its attackers and 
     * don't agree with any of its supporters, then it is inconsistent.
     * If we disagree with the statement, but agree with any of its supporters 
     * and don't agree with any of its attackers, then it is inconsistent.
     * Otherwise, it is consistent. */
    public boolean isConsistent() {
        
        if (vote) {
            if (hasAttackerVote() && !hasSupporterVote()) {
                return false;
            }
        } else {
            if (hasSupporterVote() && !hasAttackerVote()) {
                return false;
            }
        }
        
        return true;
    }
    
    /* Returns true if the subtree with this argument as the root is 
     * consistent, which is when the root and all of its children are 
     * consistent. */
    public boolean isSubTreeConsistent() {
        for (Supporter supporter : supporters) {
            if (!supporter.isConsistent()) {
                return false;
            }
        }
        
        for (Attacker attacker : attackers) {
            if (!attacker.isConsistent()) {
                return false;
            }
        }
        
        return this.isConsistent();
    }

    /* Returns true if the voter has agreed with a supporter of this 
     * argument. Returns false otherwise. */
    public boolean hasSupporterVote() {
        for (Supporter supporter : this.supporters) {
            if (supporter.getVote()) {
                return true;
            }
        }
        
        return false;
    }
    
    /* Returns true if the voter has agreed with an attacker of this
     * argument. Returns false otherwise. */
    public boolean hasAttackerVote() {
        for (Attacker attacker : this.attackers) {
            if (attacker.getVote()) {
                return true;
            }
        }
        
        return false;
    }
    
}