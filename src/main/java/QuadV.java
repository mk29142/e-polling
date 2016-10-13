import static spark.Spark.*;

public class QuadV {
	public static void main(String[] args) {
        get("/hello", (request, response) -> "Hello Friend!");
	}
}
