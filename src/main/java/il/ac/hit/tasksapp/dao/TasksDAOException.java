package il.ac.hit.tasksapp.dao;

/** Project-specific checked exception for persistence failures. */
public class TasksDAOException extends Exception {
    public TasksDAOException(String msg) { super(msg); }
    public TasksDAOException(String msg, Throwable cause) { super(msg, cause); }
}
