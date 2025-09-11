package il.ac.hit.tasksapp.model.filter;

import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.state.TaskState;

/**
 * Combinator filter for tasks. Each filter can describe itself as a simple DSL string.
 */
public interface TaskFilter {
    /** Predicate check. */
    boolean test(ITask t);

    /** Human readable description (used by the UI badge). */
    default String describe() { return "ANY"; }

    /* ---- factories ---- */

    static TaskFilter any() { return new Any(); }

    static TaskFilter byTitleContains(String q) { return new TitleContains(q); }

    static TaskFilter byState(TaskState st) { return new StateIs(st); }

    static TaskFilter byIdBetween(int lo, int hi) { return new IdBetween(lo, hi); }

    /* ---- combinators ---- */

    default TaskFilter and(TaskFilter other) { return new And(this, other); }

    default TaskFilter or(TaskFilter other) { return new Or(this, other); }

    default TaskFilter not() { return new Not(this); }

    /* ==== concrete filters (records) ==== */

    record Any() implements TaskFilter {
        @Override public boolean test(ITask t) { return true; }
        @Override public String describe() { return "ANY"; }
    }

    record TitleContains(String q) implements TaskFilter {
        @Override public boolean test(ITask t) {
            return t.getTitle() != null && t.getTitle().toLowerCase().contains(q.toLowerCase());
        }
        @Override public String describe() { return "TITLE CONTAINS \"" + q + "\""; }
    }

    record StateIs(TaskState st) implements TaskFilter {
        @Override public boolean test(ITask t) { return t.getState() == st; }
        @Override public String describe() { return "STATE IS " + st; }
    }

    record IdBetween(int lo, int hi) implements TaskFilter {
        @Override public boolean test(ITask t) {
            int id = t.getId();
            return id >= lo && id <= hi;
        }
        @Override public String describe() { return "ID BETWEEN " + lo + ".." + hi; }
    }

    record And(TaskFilter a, TaskFilter b) implements TaskFilter {
        @Override public boolean test(ITask t) { return a.test(t) && b.test(t); }
        @Override public String describe() { return "(" + a.describe() + " AND " + b.describe() + ")"; }
    }

    record Or(TaskFilter a, TaskFilter b) implements TaskFilter {
        @Override public boolean test(ITask t) { return a.test(t) || b.test(t); }
        @Override public String describe() { return "(" + a.describe() + " OR " + b.describe() + ")"; }
    }

    record Not(TaskFilter f) implements TaskFilter {
        @Override public boolean test(ITask t) { return !f.test(t); }
        @Override public String describe() { return "NOT (" + f.describe() + ")"; }
    }
}
