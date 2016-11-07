import java.util.ArrayList;
import java.util.List;

class Argument {
    private boolean isSupporter;
    private List<Argument> children;
    private String argumentTitle;
    private int parent;
    private int id;
    //TODO: Should probably change to be a number on a scale of 1 to 10, with

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

    // 10 being strongly agree, and 1 being strongly disagree
    /* True for agreement/yes, false for disagreement/no. */
    private boolean vote;

    /* Use score function (sigma) from DF-QuAD algorithm to calculate strength
     * of argument using the vote base score as the base score. */
    private double strength;

    Argument(boolean vote, String argumentTitle, boolean isSupporter) {
        this.argumentTitle = argumentTitle;
        this.vote = vote;
        this.isSupporter = isSupporter;
        this.children = new ArrayList<>();
    }

    boolean getVote() {
        return vote;
    }
    boolean isSupporter() { return isSupporter;}

    public String getArgumentTitle() {
        return argumentTitle;
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

    //TODO: Implement algorithm to check that the tree is fully consistent
    //      (IS THIS WHAT isStable() was meant to be doing)
    /* Checks that this argument is consistent with its supporters and
     * attackers.
     * If there are no supporters or attackers, it is consistent.
     * If we agree with the statement, but agree with any of its attackers and
     * don't agree with any of its supporters, then it is inconsistent.
     * If we disagree with the statement, but agree with any of its supporters
     * and don't agree with any of its attackers, then it is inconsistent.
     * Otherwise, it is consistent. */
    public List<Argument> getInconsistencies() {
        List<Argument> attackers = getAttackers();
        List<Argument> supporters = getSupporters();

        if (this.vote) {
            if (inconsistentLists(attackers, supporters)) {
                return attackers;
            }
        } else {
            if (inconsistentLists(supporters, attackers)) {
                return supporters;
            }
        }

        return new ArrayList<>();
    }

    // Depends on this.vote
    private boolean inconsistentLists(List<Argument> l1, List<Argument> l2) {
        return !l1.isEmpty() && l2.isEmpty();
    }

    /*
    Concatenates Attacker and Supporters and returns list of Arguments,
    this is used in class MasterTree for the argumentToList() function
    */
    List<Argument> getChildren(){
        return children;
    }

    /* Returns true if the subtree with this argument as the root is
     * consistent, which is when the root and all of its children are
     * consistent. */
    public boolean isSubTreeConsistent() {
        for (Argument child : children) {
            if (!child.isSubTreeConsistent()) {
                return false;
            }
        }

        return !this.getInconsistencies().isEmpty();
    }

    /* Returns true if the voter has agreed with a supporter of this
     * argument. Returns false otherwise. */
    private List<Argument> getSupporters() {
        List<Argument> supporters = new ArrayList<>();
        for (Argument child : this.children) {
            if (child.getVote() && child.isSupporter()) {
                supporters.add(child);
            }
        }

        return supporters;
    }

    /* Returns true if the voter has agreed with an attacker of this
     * argument. Returns false otherwise. */
    private List<Argument> getAttackers() {
        List<Argument> attackers = new ArrayList<>();
        for (Argument child : this.children) {
            if (child.getVote() && !child.isSupporter()) {
                attackers.add(child);
            }
        }

        return attackers;
    }

}
