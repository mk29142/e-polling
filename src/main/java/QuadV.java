import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

import com.sun.org.apache.xpath.internal.operations.Mod;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class QuadV {
	public static void main(String[] args) {
        // Configure Spark
        port(getPort());
        try {
           getConnection();
        } catch (URISyntaxException | SQLException e) {

        }

        staticFiles.location("/front-end/public");

        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();

        Map map = new HashMap();
        map.put("name", "Sam");

        get("/", (req, res) ->
                new ModelAndView(map, "index.mustache"),
                templateEngine);

        get("/votingroom", (req, res) ->
                new ModelAndView(map, "votingroom.mustache"),
                templateEngine);

        get("/vote/:id", (req, res) ->
                new ModelAndView(map, "vote.mustache"),
                templateEngine);

        get("/create", (req, res) ->
                new ModelAndView(map, "create.mustache"),
                templateEngine);

        get("/results", (req, res) ->
                new ModelAndView(map, "results.mustache"),
                templateEngine);
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
