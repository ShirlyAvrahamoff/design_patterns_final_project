package il.ac.hit.tasksapp;

import il.ac.hit.tasksapp.model.Task;
import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.state.TaskState;

/** Test helpers for quickly creating tasks. */
public final class TestData {
    private TestData() {}

    public static ITask t(int id, String title, String desc, TaskState s) {
        return new Task(id, title, desc, s);
    }
}
