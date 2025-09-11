package il.ac.hit.tasksapp.service.command;

import il.ac.hit.tasksapp.dao.ITasksDAO;
import il.ac.hit.tasksapp.dao.TasksDAOException;
import il.ac.hit.tasksapp.model.ITask;

/** Adds a new task. Undo = delete the same id. */
public final class AddTaskCommand implements Command {
    private final ITasksDAO dao;
    private final ITask task;

    public AddTaskCommand(ITasksDAO dao, ITask task) {
        this.dao = dao;
        this.task = task;
    }

    @Override public void execute() throws TasksDAOException { dao.addTask(task); }
    @Override public void undo() throws TasksDAOException { dao.deleteTask(task.getId()); }
}
