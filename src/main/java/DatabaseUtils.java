import java.sql.*;

class DatabaseUtils {
    private Connection connection;

    DatabaseUtils(Connection connection) {
        this.connection = connection;
    }

    public void initializeDatabase() {
        try {
            PreparedStatement createArgTable;
            PreparedStatement createAnswersTable;
            PreparedStatement createUserAddedTable;

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS polls" +
                    "(id SERIAL UNIQUE PRIMARY KEY, poll_name TEXT, email TEXT, password TEXT);");
            //connection.createStatement().execute("CREATE TYPE statement_type " +
            //        "AS ENUM ('Issue', 'Pro', 'Con', 'Answer')");
            createArgTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS arguments " +
                    "(poll_id INT NOT NULL REFERENCES polls(id) ON DELETE CASCADE, " +
                    "arg_id INT NOT NULL, " +
                    "parent_id INT, " +
                    "argument TEXT NOT NULL, " +
                    "score REAL," +
                    "yes_votes INT NOT NULL," +
                    "no_votes INT NOT NULL," +
                    "type statement_type," +
                    "PRIMARY KEY(poll_id, arg_id))");
            createAnswersTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS answers " +
                    "(poll_id INT NOT NULL REFERENCES polls(id) ON DELETE CASCADE," +
                    "user_id TEXT NOT NULL," +
                    "answersArray BOOLEAN ARRAY DEFAULT '{}');");
            createUserAddedTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS user_added " +
                    "(poll_id INT NOT NULL REFERENCES polls(id) ON DELETE CASCADE, " +
                    "arg_id SERIAL UNIQUE, " +
                    "parent_id INT NOT NULL, " +
                    "argument TEXT NOT NULL, " +
                    "type statement_type," +
                    "PRIMARY KEY (poll_id, arg_id));");

            createAnswersTable.execute();
            createArgTable.execute();
            createUserAddedTable.execute();

            System.out.println("Database Configured");
        } catch (SQLException e) {
            System.out.println(e.getMessage() + " in DatabaseUtils.initializeDatabase()");
        }
    }
}
