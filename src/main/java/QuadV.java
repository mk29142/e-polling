package main.java;

import static spark.Spark.*;

public class QuadV {
	public static void main(String[] args) {
        // Configure Spark
        port(4567);
        staticFiles.location("/front-end/build");

        get("/index", (request, response) -> "Hello Friend!");
	}
}
