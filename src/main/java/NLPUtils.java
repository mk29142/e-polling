import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

class NLPUtils {
    public static double checkStrings(String oldArg, String newArg) {
        double[][] stringVectors = stringsToVectors(oldArg, newArg);

        // Use cosineSimilarity to analyse these vectors
        return cosineSimilarity(stringVectors[0], stringVectors[1]);
    }

    private static String removeStopWordsAndStem(String string) {
        StringBuilder result = new StringBuilder();
        StringTokenizer st = new StringTokenizer(string);

        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (!isStopWord(next)) {
                // Here we could stem and lemmatize the words
                result.append(next);
            }
        }

        return result.toString();
    }

    private static String removeRepetition(String string) {
        return Arrays.stream(string.split(" "))
                .distinct()
                .collect(Collectors.joining(" "));
    }

    private static double[][] stringsToVectors(String string1, String string2) {
        String concatUniqueWords =
                removeStopWordsAndStem(removeRepetition(string1 + " " + string2));

        // Group semantically similar words in a phrase
        String[] strings = {string1, string2};
        StringTokenizer concatToken = new StringTokenizer(concatUniqueWords);

        double[][] result = new double[strings.length][concatToken.countTokens()];

        for (int i = 0; i < strings.length; i++) {
            double[] relatedness = new double[concatToken.countTokens()];

            for (int j = 0; concatToken.hasMoreTokens(); j++) {
                double currRelatedness = 0;
                String currToken = concatToken.nextToken();
                StringTokenizer tk = new StringTokenizer(strings[i]);

                while (tk.hasMoreTokens()) {
                    currRelatedness += wuPalmerRelatedness(tk.nextToken(), currToken);
                }

                relatedness[j] = currRelatedness;
            }

            result[i] = relatedness;
        }

        return result;
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

    private static double wuPalmerRelatedness(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        ILexicalDatabase db = new NictWordNet();
        RelatednessCalculator rc =  new WuPalmer(db);
        return rc.calcRelatednessOfWords(word1, word2);
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
