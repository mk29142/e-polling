import java.net.URISyntaxException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

import com.sun.org.apache.xpath.internal.operations.Mod;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;


public class QuadV {
    public static void main(String[] args) {
        // Configure Spark and server routes.
        port(getPort());
        Connection connection;
        staticFiles.location("/front-end/public");

        try {
            connection = getConnection();


            MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

            Map map = new HashMap();
            map.put("name", "Sam");

            get("/", (req, res) -> {
                        //Get all of the names of polls for listing
                        Statement stmt = connection.createStatement();

                        ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='poll';");
                       return new ModelAndView(map, "index.mustache");
                    }, templateEngine);

            get("/votingroom", (req, res) ->
                            new ModelAndView(map, "votingroom.mustache"),
                    templateEngine);

            get("/vote/:id", (request, response) -> {
                        //Get the questions one by one for the specific poll
                        //use PreparedStatement in here to stop string injection
                        String pollname = request.params(":id");
                        PreparedStatement findPolls = connection.prepareStatement("SELECT  FROM polls");
                        return new ModelAndView(map, "vote.mustache");
                    }, templateEngine);

            get("/create", (req, res) ->
                            new ModelAndView(map, "create.mustache"),
                    templateEngine);

            get("/results", (req, res) ->
                            new ModelAndView(map, "results.mustache"),
                    templateEngine);
        } catch (Exception e) {

        }

    }

    private static Connection getConnection()
            throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    private static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");
        return port != null ? Integer.parseInt(port) : 4567;
    }
}
