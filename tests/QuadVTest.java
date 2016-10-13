import org.junit.Test;
import junit.framework.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuadVTest {
    
    @Test
    public void Silly() {
            assertEquals(1,1);
    }

    @Test
    public void canCreateArgumentObject(){
        assertFalse(new Argument(true, "should britain leave the eu", true)==null);
    }

    @Test
    public void canAddChildrentoPoll(){
        Argument arg = new Argument(true, "should britain leave the eu", true);
        List<Argument> children = new ArrayList<Argument>();
        children.add(new Argument(true, "eu migrants are good for the economy", false));
        arg.setChildren(children);
        assertFalse(arg.children==null);
    }

    @Test
    public void isPollStable(){
        Argument arg = new Argument(true, "should britain leave the eu", true);
        List<Argument> children = new ArrayList<Argument>();
        children.add(new Argument(true, "eu migrants are good for the economy", false));
        arg.setChildren(children);
        assertFalse(arg.isStable());
    }

}
