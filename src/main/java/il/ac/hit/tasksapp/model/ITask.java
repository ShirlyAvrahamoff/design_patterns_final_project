package il.ac.hit.tasksapp.model;

import il.ac.hit.tasksapp.model.state.TaskState;

/** Public view of a task entity, used across layers. */
public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    TaskState getState();
}
