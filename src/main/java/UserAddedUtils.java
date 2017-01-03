import com.google.gson.JsonObject;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

class UserAddedUtils {
    private final Connection connection;
    private final String pollId;

    public UserAddedUtils(Connection connection, String pollId) {
        this.connection = connection;
        this.pollId = pollId;
    }

    public String addNewArg(JsonObject newArg) {
        String userId = newArg.get("userId").getAsString();
        int id = newArg.get("id").getAsInt();
        int parent = newArg.get("parent").getAsInt();
        String txt = newArg.get("text").getAsString();
        String type = newArg.get("type").getAsString();
        return addToTable(id, parent, txt, type, userId);
    }

    private String addToTable(
            int id,
            int parent,
            String txt,
            String type,
            String userId) {
        try {
            PreparedStatement newArg =
                    connection.prepareStatement("INSERT INTO ?"
                            + "VALUES (?, ?, ?, ?::statement_type, ?);");
            newArg.setString(1, pollId + "_user_added");
            newArg = connection.prepareStatement(
                    newArg.toString().replace("'", "\""));

            newArg.setInt(1, id);
            newArg.setInt(2, parent);
            newArg.setString(3, txt);
            newArg.setString(4, type);
            newArg.setString(5, userId);

            newArg.execute();

            return "SUCCESS";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "FAIL";
        }
    }

    private String removeStopWordsAndStem(String string) {
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

    private String removeRepetition(String string) {
        return Arrays.stream(string.split(" "))
                .distinct()
                .collect(Collectors.joining(" "));
    }

    private List<double[]> stringsToVectors(String string1, String string2) {
        List<double[]> result = new ArrayList<>();
        String concatUniqueWords = removeRepetition(string1 + " " + string2);

        // Group semantically similar words in a phrase
        String[] strings = {string1, string2};
        StringTokenizer concatToken = new StringTokenizer(concatUniqueWords);

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

            result.set(i, relatedness);
        }

        return result;
    }

    private double cosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = dotProduct(vector1, vector2);
        double euclideanDist =
                euclideanDistance(vector1) * euclideanDistance(vector2);
        return dotProduct / euclideanDist;
    }

    private double euclideanDistance(double[] vector){
        double result = 0.0;

        for (double aVector : vector) {
            result += aVector * aVector;
        }

        return result;
    }

    private double dotProduct(double[] vector1, double[] vector2) {
        double result = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            result += vector1[i] * vector2[i];
        }
        return result;
    }

    private double wuPalmerRelatedness(String word1, String word2 ) {
        WS4JConfiguration.getInstance().setMFS(true);
        ILexicalDatabase db = new NictWordNet();
        RelatednessCalculator rc =  new WuPalmer(db);
        return rc.calcRelatednessOfWords(word1, word2);
    }

    private boolean isStopWord(String string) {
        String[] stopArray = new String[]{"a", "an", "and", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with"};

        return Arrays.asList(stopArray).contains(string);
    }
}
