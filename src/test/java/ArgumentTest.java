import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;


public class ArgumentTest {
    private Argument arg;

    @Before
    public void initialise() {
        arg = new Argument(true, "Britain should leave the EU",false);
        Argument child1 = new Argument(true, "EU migrants are good for the economy", false);
        Argument child2 = new Argument(true, "EU migrants pay more tax than they take out", false);
        arg.addChild(child1);
        arg.addChild(child2);
    }

    @Test
    public void canAddChildren() {
        assertEquals(2, arg.getChildren().size());
    }

    @Test
    public void checkArgumentTreeIsInconsistent() {
        assertNotNull(arg.getInconsistencies());
    }

    @Test
    public void toBoxReturnsBoxObject(){
        assertThat(arg.toBox(), instanceOf(Box.class));
    }

    /*
    @Test
    public void updateArgumentTreeScores() {



    }
    */


}
