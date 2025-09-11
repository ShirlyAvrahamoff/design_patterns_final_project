package il.ac.hit.tasksapp.model;

import il.ac.hit.tasksapp.model.state.TaskState;
import il.ac.hit.tasksapp.model.visitor.TaskVisitor;

/** Public view of a task entity, used across layers. */
public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    TaskState getState();

    /** Visitor dispatch. Implementations pass a record snapshot. */
    void accept(TaskVisitor visitor);
}
