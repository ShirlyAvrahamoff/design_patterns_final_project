package il.ac.hit.tasksapp.model.record;

import il.ac.hit.tasksapp.model.state.TaskState;

/** Immutable data carrier used by Visitors (Java record). */
public record TaskRecord(int id, String title, String description, TaskState state) {}
