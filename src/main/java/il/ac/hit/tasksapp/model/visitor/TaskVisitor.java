package il.ac.hit.tasksapp.model.visitor;

import il.ac.hit.tasksapp.model.record.TaskRecord;

/** Visitor interface for reports. */
public interface TaskVisitor {
    void visit(TaskRecord task);
}
