package il.ac.hit.tasksapp.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Singleton that owns the embedded Derby connection and schema. */
public final class DerbyConnectionManager {
    private static final DerbyConnectionManager INSTANCE = new DerbyConnectionManager();

    /*
     * DB location:
     * - Prefer VM option: -Dtasksapp.db.dir=C:/path/to/tasksdb
     * - Otherwise use:    <user.home>/.tasksapp_db
     */
    private static final String DB_DIR =
            System.getProperty("tasksapp.db.dir",
                    System.getProperty("user.home") + File.separator + ".tasksapp_db");

    private static final String URL = "jdbc:derby:" + DB_DIR + ";create=true";

    private Connection conn;

    private DerbyConnectionManager() {}

    /** Global instance. */
    public static DerbyConnectionManager getInstance() { return INSTANCE; }

    /** Open one shared connection and ensure schema exists. */
    public synchronized Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            // Make sure directory exists so Derby can create files.
            new File(DB_DIR).mkdirs();
            conn = DriverManager.getConnection(URL);
            initSchema(conn);
        }
        return conn;
    }

    // Create tables on first run. Ignore "already exists".
    private void initSchema(Connection c) {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE tasks(
                  id INT PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  description VARCHAR(500),
                  state VARCHAR(32) NOT NULL
                )
                """);
        } catch (SQLException ignore) { /* table exists */ }
    }
}
