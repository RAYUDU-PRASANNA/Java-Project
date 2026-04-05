package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection — always returns a FRESH connection.
 * Using a shared singleton connection caused "statement closed" errors
 * when multiple statements were used in the same transaction.
 * Each DAO method gets its own connection and closes it when done.
 */
public class DBConnection {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/busgo"
                                        + "?useSSL=false"
                                        + "&allowPublicKeyRetrieval=true"
                                        + "&serverTimezone=Asia/Kolkata"
                                        + "&useUnicode=true"
                                        + "&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "kiran@123"; // ← change to your MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] Driver not found — add mysql-connector-j jar to build path.");
        }
    }

    /**
     * Returns a brand-new connection every time.
     * Caller is responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        System.out.println("[DBConnection] Connected to MySQL ✓");
        return con;
    }

    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Test FAILED: " + e.getMessage());
            return false;
        }
    }
}