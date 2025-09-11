package il.ac.hit.tasksapp.service.command;

import il.ac.hit.tasksapp.dao.InMemoryTasksDAO;
import il.ac.hit.tasksapp.dao.ITasksDAO;
import il.ac.hit.tasksapp.model.state.TaskState;
import org.junit.jupiter.api.Test;

import static il.ac.hit.tasksapp.TestData.t;
import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    @Test
    void add_update_delete_withUndoRedo() throws Exception {
        ITasksDAO dao = new InMemoryTasksDAO();
        CommandManager mgr = new CommandManager();

        // Add
        mgr.doCommand(new AddTaskCommand(dao, t(1, "A", "", TaskState.TO_DO)));
        assertNotNull(dao.getTask(1));

        // Update
        mgr.doCommand(new UpdateTaskCommand(dao, t(1, "A1", "desc", TaskState.IN_PROGRESS)));
        assertEquals("A1", dao.getTask(1).getTitle());

        // Delete
        mgr.doCommand(new DeleteTaskCommand(dao, 1));
        assertNull(dao.getTask(1));

        // Undo delete -> task returns
        mgr.undo();
        assertNotNull(dao.getTask(1));
        assertEquals("A1", dao.getTask(1).getTitle());

        // Undo update -> back to original "A" / TO_DO
        mgr.undo();
        assertEquals("A", dao.getTask(1).getTitle());
        assertEquals(TaskState.TO_DO, dao.getTask(1).getState());

        // Undo add -> task gone
        mgr.undo();
        assertNull(dao.getTask(1));

        // Redo add
        mgr.redo();
        assertNotNull(dao.getTask(1));

        // Redo update
        mgr.redo();
        assertEquals("A1", dao.getTask(1).getTitle());

        // Redo delete
        mgr.redo();
        assertNull(dao.getTask(1));
    }
}
