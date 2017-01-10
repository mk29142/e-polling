import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QuadVTest {
    private String compareStr = "Kante is a great football player";

    @Test
    public void checkStringSaysTwoSimilarHaveHighSimilarity() {
        String otherCompareStr = "Kante is a very good player of football";
        double val =
                NLPUtils.checkStrings(compareStr, otherCompareStr);
        System.out.println(val);
        assertTrue(val >= 0.5);
    }

    @Test
    public void checkStringSaysNotSimilarLowSimilarity() {
        String notSimilarStr = "Every day of my life I eat a lot of lemons and drink water with beans and bear snacks";
        double val = NLPUtils.checkStrings(notSimilarStr, compareStr);
        System.out.println(val);
        assertTrue(val <= 0.6);
    }

    @Test
    public void checkSimilarSentence() {
        String comparator = "Kante is great at sports and has nice technique";
        double val = NLPUtils.checkStrings(compareStr, comparator);
        System.out.println(val);
    }

    @Test
    public void checkSentencesAreTheSame() {
        String comparator = "Kante is a great football player";
        double val = NLPUtils.checkStrings(compareStr, comparator);
        System.out.println(val);
    }

    @Test
    public void relatedGivesValuesBetweenZeroAndOneWithSameWord() {
        double sameWord = NLPUtils.relatedness("Word", "worD");
        System.out.println(sameWord);
        assertTrue(sameWord <= 1.0);
        assertTrue(sameWord >= 0.0);
    }

    @Test
    public void relatedGivesValuesBetweenZeroAndOneForDiffWords() {
        double veryDiffWords = NLPUtils.relatedness("HEllo", "Giraffe");
        System.out.println(veryDiffWords);
        assertTrue(veryDiffWords <= 1);
        assertTrue(veryDiffWords >= 0);
    }

    @Test
    public void checkDropFallRelatedness() {
        double r = NLPUtils.relatedness("drop", "fall");
        System.out.println(r);
        assertTrue(r > 0);
    }
}

