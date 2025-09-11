package il.ac.hit.tasksapp.dao;

import il.ac.hit.tasksapp.model.ITask;

/** DAO contract for tasks persistence. */
public interface ITasksDAO {
    ITask[] getTasks() throws TasksDAOException;
    ITask getTask(int id) throws TasksDAOException;
    void addTask(ITask task) throws TasksDAOException;
    void updateTask(ITask task) throws TasksDAOException;
    void deleteTasks() throws TasksDAOException;
    void deleteTask(int id) throws TasksDAOException;
}
