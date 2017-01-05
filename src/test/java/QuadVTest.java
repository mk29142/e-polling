import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QuadVTest {
    private String compareString = "Kante is a great football player";

    @Test
    public void checkStringSaysTwoSimilarHaveHighSimilarity() {
        double val =
                NLPUtils.checkStrings(
                        compareString,
                        "Kante has football skills that are brilliant");
        assertTrue(val >= 0.5);
    }
}

