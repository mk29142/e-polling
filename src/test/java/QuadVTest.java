//package src.test.java;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuadVTest {
    private Node root;
    private Node child;
    private Node secondChild;
    private Argument arg;
    private MasterTree master;

    @Before
    public void initialise() {
        root = new Node(false);
        child = new Node(false);
        secondChild = new Node(false);
        root.addChild(child);
        root.addChild(secondChild);

        arg = new Argument(true, "Should britain leave the EU?",false);
        arg.addChild(true, "EU migrants are good for the economy", false);
        arg.addChild(true, "EU migrants pay more tax than they take out", 
                false);

        master = new MasterTree(root);
        master.argumentToList(arg);
    }

    @Test
    public void canAddChildrentoPoll() {
        assertNotNull(arg.getChildren());
    }

    @Test
    public void argumentObjectToBooleanListOfVotes() {
        List<Boolean> correctList = new ArrayList<Boolean>();
        correctList.add(true);
        correctList.add(true);
        correctList.add(true);

        assertEquals(correctList,master.getVoteList());
    }

    @Test
    public void checkBaseScore() {
        master.updateScore(master.getVoteList(), root);
        assertEquals(1.0, root.getBaseScore(), 0.005);
        assertEquals(1.0, child.getBaseScore(), 0.005);
        assertEquals(1.0, secondChild.getBaseScore(), 0.005);
    }

    @Test
    public void checkUpdateScoreEmptiesArgumentVoteList() {
        master.updateScore(master.getVoteList(), root);

        assertTrue(master.getVoteList().isEmpty());
    }

    @Test
    public void canAddChildren() {
        assertEquals(2, root.getChildren().size());
    }

    @Test
    public void canGetAttackers() {
        assertEquals(2, root.getAttackers().size());
    }

    @Test
    public void updateMasterTreeScore() {
        master.updateScore(master.getVoteList(), root);

        assertTrue(master.getVoteList().isEmpty());
        assertEquals(0.0, root.getScore(), 0.005);
    }
}
