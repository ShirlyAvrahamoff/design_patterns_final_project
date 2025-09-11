package il.ac.hit.tasksapp.service.command;

import il.ac.hit.tasksapp.dao.ITasksDAO;
import il.ac.hit.tasksapp.dao.TasksDAOException;
import il.ac.hit.tasksapp.model.ITask;

/** Updates an existing task. Undo = restore previous snapshot. */
public final class UpdateTaskCommand implements Command {
    private final ITasksDAO dao;
    private final ITask newTask;
    private ITask before;

    public UpdateTaskCommand(ITasksDAO dao, ITask newTask) {
        this.dao = dao; this.newTask = newTask;
    }

    @Override public void execute() throws TasksDAOException {
        before = dao.getTask(newTask.getId());
        dao.updateTask(newTask);
    }

    @Override public void undo() throws TasksDAOException {
        if (before != null) dao.updateTask(before);
    }
}
