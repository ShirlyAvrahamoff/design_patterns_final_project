package il.ac.hit.tasksapp.model;

import il.ac.hit.tasksapp.model.state.TaskState;

/** Mutable task model. Validation stays in setters; ctor delegates. */
public class Task implements ITask {
    /** unique id (>=0) */
    private int id;
    /** short title (1..255) */
    private String title;
    /** description (0..500) */
    private String description;
    /** lifecycle state */
    private TaskState state;

    /** Primary ctor. */
    public Task(int id, String title, String description, TaskState state) {
        setId(id); setTitle(title); setDescription(description); setState(state);
    }

    @Override public int getId() { return id; }
    @Override public String getTitle() { return title; }
    @Override public String getDescription() { return description; }
    @Override public TaskState getState() { return state; }

    /** Validate and set id. */
    public final void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("id must be >= 0");
        this.id = id;
    }
    /** Validate and set title. */
    public final void setTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        if (title.length() > 255) throw new IllegalArgumentException("title too long (max 255)");
        this.title = title.trim();
    }
    /** Normalize and set description. */
    public final void setDescription(String description) {
        description = (description == null ? "" : description);
        if (description.length() > 500) throw new IllegalArgumentException("description too long (max 500)");
        this.description = description;
    }
    /** Validate and set state. */
    public final void setState(TaskState state) {
        if (state == null) throw new IllegalArgumentException("state is required");
        this.state = state;
    }

    @Override public boolean equals(Object o) { return (o instanceof Task t) && t.id == id; }
    @Override public int hashCode() { return Integer.hashCode(id); }
    @Override public String toString() { return "Task{id=" + id + ", title='" + title + "', state=" + state + "}"; }
}
