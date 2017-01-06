import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.PorterStemmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collector;

class NLPUtils {
    private static ILexicalDatabase db = new NictWordNet();

    static double checkStrings(String oldArg, String newArg) {
        Pair<double[]> stringVectors = stringsToVectors(oldArg, newArg);

        // Use cosineSimilarity to analyse these vectors
        double[] one = stringVectors.getOne();
        double[] two = stringVectors.getTwo();

        if (allZero(one) || allZero(two)) return 0;

        return cosineSimilarity(one, two);
    }

    private static boolean allZero(double[] arr) {
        return Arrays.stream(arr).allMatch(value -> value == 0);
    }

    private static String removeStopWordsAndStem(String string) {
        StringBuilder result = new StringBuilder();
        StringTokenizer st = new StringTokenizer(string);

        PorterStemmer ps = new PorterStemmer();

        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (!isStopWord(next)) {

                result.append(next);
                result.append(' ');
            }
        }

        return result.toString();
    }

    private static String[] removeRepetition(String string) {
        return Arrays.stream(string.split(" "))
                .distinct()
                .toArray(String[]::new);
    }

    private static class Pair<T> {
        private T one;
        private T two;

        Pair(T one, T two) {
            this.one = one;
            this.two = two;
        }

        T getOne() { return this.one; }
        T getTwo() { return this.two; }
    }

    private static Pair<double[]> stringsToVectors(String s1, String s2) {
        String[] word1 = removeRepAndStopWords(s1);
        String[] word2 = removeRepAndStopWords(s2);

        if (word1.length > word2.length) {
            word1 = simplifyLargeSentence(word1, word2);
        } else {
            word2 = simplifyLargeSentence(word2, word1);
        }

        return new Pair<>(makeVector(word2, word1), makeVector(word1, word2));
    }

    private static String[] simplifyLargeSentence(String[] larger, String[] smaller) {
        return Arrays.stream(larger)
            .sorted((a, b) -> a.length() > b.length() ? 1 : -1)
            .limit(smaller.length).toArray(String[]::new);
    }

    private static double[] makeVector(String[] sentence1, String[] sentence2) {
        double[] ret = new double[sentence1.length];

        for (int i = 0; i < sentence1.length; i++) {
            double sum = 0;
            int nonZeros = 0;

            for (String w : sentence2) {
                double r = relatedness(sentence1[i], w);
                sum += r;

                if (r > 0) {
                    nonZeros++;
                }
            }

            ret[i] = nonZeros > 0 ? sum / nonZeros : 0;
        }

        return ret;
    }

    private static String[] removeRepAndStopWords(String s) {
        return removeRepetition(removeStopWordsAndStem(s));
    }

    private static double cosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = dotProduct(vector1, vector2);
        double euclideanDist =
                euclideanDistance(vector1) * euclideanDistance(vector2);
        return dotProduct / euclideanDist;
    }

    private static double euclideanDistance(double[] vector) {
        double result = 0.0;

        for (double aVector : vector) {
            result += aVector * aVector;
        }

        return Math.sqrt(result);
    }

    private static double dotProduct(double[] vector1, double[] vector2) {
        double result = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            result += vector1[i] * vector2[i];
        }

        return result;
    }

    static double relatedness(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        RelatednessCalculator[] rcs = { new WuPalmer(db), new Path(db), new Lin(db) };
        double[] weights = { 0.4, 0.4, 0.2 };
        double[] scores = { 0, 0, 0 };

        for (int i = 0; i < rcs.length; i++) {
            List<POS[]> posPairs = rcs[i].getPOSPairs();

            for (POS[] posPair : posPairs) {

                List<Concept> synset1 =
                    (List<Concept>) db.getAllConcepts(word1, posPair[0].toString());
                List<Concept> synset2 =
                    (List<Concept>) db.getAllConcepts(word2, posPair[1].toString());

                for (Concept c1 : synset1) {
                    for (Concept c2 : synset2) {
                        double value = rcs[i].calcRelatednessOfSynset(c1, c2).getScore();

                        if (value > scores[i] && value > 0) {
                            scores[i] = value;
                        }
                    }
                }
            }
        }

        scores = Arrays.stream(scores)
            .map(value -> value > 1 ? 1 : value)
            .toArray();
        return dotProduct(scores, weights);
    }

    private static boolean isStopWord(String string) {
        String[] stopArray = {
            "a", "all", "am", "any", "aren't", "an", "about",
                "above", "after", "again", "against",
                "and", "are", "as", "at", "be", "because", "been", "before",
                "below", "between", "both", "but", "by", "can't", "cannot",
                "could", "couldn't", "did", "didn't", "do", "does", "do",
                "doesn't", "doing", "don't", "down", "during", "each", "few",
                "from", "further", "for", "had", "hadn't", "has", "hasn't", "have",
                "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
                "here's", "hers", "herself", "him", "himself", "his", "how",
                "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't",
                "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with"
        };

        return Arrays.asList(stopArray).contains(string);
    }
}
