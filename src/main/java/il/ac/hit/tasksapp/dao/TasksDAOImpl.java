package il.ac.hit.tasksapp.dao;

import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.Task;
import il.ac.hit.tasksapp.model.state.TaskState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Derby embedded DAO (Singleton).
 * NOTE: No in-memory caching here; caching is delegated to CachingTasksDAOProxy.
 */
public final class TasksDAOImpl implements ITasksDAO {

    /** Singleton holder (eager). */
    private static final TasksDAOImpl INSTANCE = new TasksDAOImpl();

    /** Derby connection (embedded). */
    private final Connection conn;

    /** Private ctor (Singleton). */
    private TasksDAOImpl() {
        try {
            try { Class.forName("org.apache.derby.jdbc.EmbeddedDriver"); } catch (ClassNotFoundException ignore) { }
            this.conn = DriverManager.getConnection("jdbc:derby:tasksdb;create=true");
            createTableIfMissing();
        } catch (SQLException e) {
            throw new RuntimeException("Derby init failed", e);
        }
    }

    /** Singleton accessor. */
    public static TasksDAOImpl getInstance() { return INSTANCE; }

    /** Create table if it does not exist. */
    private void createTableIfMissing() throws SQLException {
        final String ddl = """
            create table tasks (
              id          int primary key,
              title       varchar(255) not null,
              description varchar(500),
              state       varchar(20)  not null
            )
            """;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            // X0Y32 = Table/View already exists
            if (!"X0Y32".equals(e.getSQLState())) throw e;
        }
    }

    /* -------------------- DAO API -------------------- */

    @Override
    public synchronized ITask[] getTasks() throws TasksDAOException {
        final String sql = "select id, title, description, state from tasks order by id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ITask> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list.toArray(new ITask[0]);
        } catch (SQLException e) {
            throw new TasksDAOException("getTasks failed", e);
        }
    }

    @Override
    public synchronized ITask getTask(int id) throws TasksDAOException {
        final String sql = "select id, title, description, state from tasks where id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        } catch (SQLException e) {
            throw new TasksDAOException("getTask failed for id=" + id, e);
        }
    }

    @Override
    public synchronized void addTask(ITask task) throws TasksDAOException {
        final String sql = "insert into tasks(id, title, description, state) values(?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, task.getId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getState().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("addTask failed for id=" + task.getId(), e);
        }
    }

    @Override
    public synchronized void updateTask(ITask task) throws TasksDAOException {
        final String sql = "update tasks set title=?, description=?, state=? where id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getState().name());
            ps.setInt(4, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("updateTask failed for id=" + task.getId(), e);
        }
    }

    @Override
    public synchronized void deleteTasks() throws TasksDAOException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("delete from tasks");
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTasks failed", e);
        }
    }

    @Override
    public synchronized void deleteTask(int id) throws TasksDAOException {
        final String sql = "delete from tasks where id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("deleteTask failed for id=" + id, e);
        }
    }

    /* -------------------- helpers -------------------- */

    private static ITask map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        TaskState state = TaskState.valueOf(rs.getString("state"));
        return new Task(id, title, description, state);
    }
}
