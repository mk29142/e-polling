import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.util.Arrays;
import java.util.StringTokenizer;

class NLPUtils {
    private static ILexicalDatabase db = new NictWordNet();

    static double checkStrings(String oldArg, String newArg) {
        Pair<double[]> stringVectors = stringsToVectors(oldArg, newArg);

        // Use cosineSimilarity to analyse these vectors
        return cosineSimilarity(stringVectors.getOne(), stringVectors.getTwo());
    }

    private static String removeStopWordsAndStem(String string) {
        StringBuilder result = new StringBuilder();
        StringTokenizer st = new StringTokenizer(string);

        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (!isStopWord(next)) {
                // Here we could stem and lemmatize the words
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
        String[] concatString = removeRepAndStopWords(s1 + ' ' + s2);
        String[] word1 = removeRepAndStopWords(s1);
        String[] word2 = removeRepAndStopWords(s2);

        return new Pair<>(
            makeVector(concatString, word1),
            makeVector(concatString, word2)
        );
    }

    private static double[] makeVector(String[] concat, String[] word) {
        double[] ret = new double[concat.length];

        for (int i = 0; i < concat.length; i++) {
            double sum = 0;

            for (String w : word) {
                sum += relatedness(concat[i], w);
            }

            ret[i] = sum / word.length;
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
        double[] weights = { 0.6, 0.1, 0.2 };
        double sum = 0;

        RelatednessCalculator hso = new HirstStOnge(db);
        double hsoWeight = 0.0625 * 0.1;
        double hsoVal = hso.calcRelatednessOfWords(word1, word2);

        if (hsoVal >= 0 && hsoVal <= 1) {
            sum += hsoVal * hsoWeight;
        } else {
            sum += 0.1;
        }

        for (int i = 0; i < rcs.length; i++) {
            double value = rcs[i].calcRelatednessOfWords(word1, word2);
            if (value >= 0 && value <= 1) {
                sum += value * weights[i];
            } else if (value > 1) {
                sum += weights[i];
            }
        }

        return sum;
    }

    private static boolean isStopWord(String string) {
        String[] stopArray = new String[]{"a", "an", "and", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with"};

        return Arrays.asList(stopArray).contains(string);
    }
}
