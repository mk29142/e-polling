import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

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
        return string.split(" ");
    }

    private static class Pair<T> {
        private T one;
        private T two;

        Pair(T one, T two) {
            this.one = one;
            this.two = two;
        }

        public T getOne() { return this.one; }
        public T getTwo() { return this.two; }
    }

    static Pair<double[]> stringsToVectors(String s1, String s2) {
        String[] arr1 = removeRepetition(removeStopWordsAndStem(s1));
        String[] arr2 = removeRepetition(removeStopWordsAndStem(s2));

        double[][] relatedMatrix = matrixRelatedness(arr1, arr2);

        double[] word1 = new double[relatedMatrix.length];
        for (int i = 0; i < relatedMatrix.length; i++) {
            double sum = 0;
            for (int j = 0; j < relatedMatrix[0].length; j++) {
                sum += relatedMatrix[i][j];
            }

            word1[i] = sum / relatedMatrix[0].length;
        }

        double[] word2 = new double[relatedMatrix[0].length];
        for (int i = 0; i < relatedMatrix[0].length; i++) {
            double sum = 0;
            for (int j = 0; j < relatedMatrix.length; j++) {
                sum += relatedMatrix[j][i];
            }

            word2[i] = sum / relatedMatrix.length;
        }
        return new Pair<>(word1, word2);
    }

    private static double[][] matrixRelatedness(String[] s1, String[] s2) {
        double[][] result = new double[s1.length][s2.length];
        RelatednessCalculator[] rcs = { new WuPalmer(db), new Path(db), new Lin(db) };

        for (RelatednessCalculator rc : rcs) {
            double[][] value = rc.getNormalizedSimilarityMatrix(s1, s2);

            result = addMatrix(result, value);
        }

        return divide(result, rcs.length);
    }

    private static double[][] divide(double[][] result, int den) {
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                result[i][j] /= den;
            }
        }

        return result;
    }

    private static double[][] addMatrix(double[][] result, double[][] value) {
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                result[i][j] += value[i][j];
            }
        }

        return result;
    }

    private static void printDoubleArray(double[][] arr) {
        for (double[] inner : arr) {
            for (int j = 0; j < inner.length - 1; j++) {
                System.out.print(String.format("%1$,.2f", inner[j]) + ", ");
            }

            System.out.println(String.format("%1$,.2f", inner[inner.length - 1]));
        }
    }

    private static double cosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = dotProduct(vector1, vector2);
        double euclideanDist =
                euclideanDistance(vector1) * euclideanDistance(vector2);
        return dotProduct / euclideanDist;
    }

    private static double euclideanDistance(double[] vector){
        double result = 0.0;

        for (double aVector : vector) {
            result += aVector * aVector;
        }

        return result;
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
        double sum = 0;

        for (RelatednessCalculator rc : rcs) {
            double value = rc.calcRelatednessOfWords(word1, word2);
            if (value >= 0 && value <= 1) {
                sum += value;
            }
        }

        return sum / rcs.length;
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
