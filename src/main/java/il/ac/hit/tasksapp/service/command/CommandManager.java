package il.ac.hit.tasksapp.service.command;

import il.ac.hit.tasksapp.dao.TasksDAOException;

import java.util.ArrayDeque;
import java.util.Deque;

/** Maintains undo/redo stacks and runs commands. */
public final class CommandManager {
    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();

    /** Execute and push to undo; clear redo. */
    public void doCommand(Command c) throws TasksDAOException {
        c.execute();
        undo.push(c);
        redo.clear();
    }

    /** Undo last command (if any). */
    public void undo() throws TasksDAOException {
        if (undo.isEmpty()) return;
        Command c = undo.pop();
        c.undo();
        redo.push(c);
    }

    /** Redo last undone command (if any). */
    public void redo() throws TasksDAOException {
        if (redo.isEmpty()) return;
        Command c = redo.pop();
        c.execute();
        undo.push(c);
    }
}
