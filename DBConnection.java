import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");  // Load JDBC driver
            return DriverManager.getConnection("jdbc:sqlite:quiz.db"); // Connect to quiz.db
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
