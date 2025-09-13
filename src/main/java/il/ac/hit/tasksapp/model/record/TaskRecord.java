package il.ac.hit.tasksapp.model.record;

import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.state.TaskState;

/**
 * Immutable data carrier used by Visitors.
 */
public record TaskRecord(int id, String title, String description, TaskState state) {

    /**
     * Build a TaskRecord snapshot from an ITask.
     */
    public static TaskRecord from(ITask task) {
        return new TaskRecord(task.getId(), task.getTitle(), task.getDescription(), task.getState());
    }
}
