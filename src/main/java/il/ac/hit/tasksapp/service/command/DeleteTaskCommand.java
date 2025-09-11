package il.ac.hit.tasksapp.service.command;

import il.ac.hit.tasksapp.dao.ITasksDAO;
import il.ac.hit.tasksapp.dao.TasksDAOException;
import il.ac.hit.tasksapp.model.ITask;

/** Deletes a task. Undo = re-add deleted snapshot. */
public final class DeleteTaskCommand implements Command {
    private final ITasksDAO dao;
    private final int id;
    private ITask deleted;

    public DeleteTaskCommand(ITasksDAO dao, int id) {
        this.dao = dao; this.id = id;
    }

    @Override public void execute() throws TasksDAOException {
        deleted = dao.getTask(id);
        dao.deleteTask(id);
    }

    @Override public void undo() throws TasksDAOException {
        if (deleted != null) dao.addTask(deleted);
    }
}
