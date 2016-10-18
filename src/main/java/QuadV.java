import static spark.Spark.*;

public class QuadV {
	public static void main(String[] args) {
        // Configure Spark
        port(getHerokuAssignedPort());
        staticFiles.location("/front-end/build");

        get("/hello", (request, response) -> "Hello Friend!");
	}

    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String port = processBuilder.environment().get("PORT");

        if (port != null) {
            return Integer.parseInt(port);
        }

        return 4567; // Return default port if heroku-port isn't set (i.e. on localhost)
    }
}
