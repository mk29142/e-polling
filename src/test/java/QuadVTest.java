import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuadVTest {
    private String compareStr = "Kante is a great football player";
    private String otherCompareStr = "Kante has football skills that are brilliant";
    private String notSimilarStr = "Where is the nearest post office, Madam?";

    @Test
    public void checkStringSaysTwoSimilarHaveHighSimilarity() {
        double val =
                NLPUtils.checkStrings(compareStr, notSimilarStr);
        System.out.println(val);
        assertTrue(val >= 0.5);
    }

    @Test
    public void relatedGivesValuesBetweenZeroAndOneWithSameWord() {
        double sameWord = NLPUtils.relatedness("Word", "worD");
        assertTrue(sameWord <= 1.0);
        assertTrue(sameWord >= 0.0);
    }

    @Test
    public void relatedGivesValuesBetweenZeroAndOneForDiffWords() {
        double veryDiffWords = NLPUtils.relatedness("HEllo", "Giraffe");
        assertTrue(veryDiffWords <= 1);
        assertTrue(veryDiffWords >= 0);
    }
}

