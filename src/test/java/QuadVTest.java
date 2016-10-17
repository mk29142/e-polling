import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuadVTest {
    
    @Test
    public void Silly() {
        assertEquals(1, 1);
    }

   /* @Test
    public void canAddChildrentoPoll() {
        Argument arg = new Argument(true, "Should britain leave the EU?", true);

        List<Argument> children = new ArrayList<Argument>();
        children.add(new Argument(true, "EU migrants are good for the economy", false));
        arg.setChildren(children);

        assertNotNull(arg.getChildren());
    }

    @Test
    public void canAccessArgName() {
        String title = "Should britain leave the EU?";

        Argument arg = new Argument(true, title, true);

        assertEquals(arg.getArgumentTitle(), title);
    }

    @Test
    public void isPollStable(){
        Argument arg = new Argument(true, "Should britain leave the EU?", true);

        List<Argument> children = new ArrayList<Argument>();
        children.add(new Argument(true, "EU migrants are good for the economy", false));
        arg.setChildren(children);

        assertFalse(arg.isStable());
    }*/

}
