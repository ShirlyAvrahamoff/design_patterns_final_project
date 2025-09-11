package il.ac.hit.tasksapp.service.command;

import il.ac.hit.tasksapp.dao.TasksDAOException;

/** A reversible change to the model. */
public interface Command {
    void execute() throws TasksDAOException;
    void undo() throws TasksDAOException;
}
