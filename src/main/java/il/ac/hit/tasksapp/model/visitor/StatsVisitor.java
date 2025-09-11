package il.ac.hit.tasksapp.model.visitor;

import il.ac.hit.tasksapp.model.record.TaskRecord;
import il.ac.hit.tasksapp.model.state.TaskState;

import java.util.EnumMap;
import java.util.Map;

/**
 * Collects state counts using record pattern matching (switch over record).
 */
public class StatsVisitor implements TaskVisitor {
    private final Map<TaskState, Integer> counts = new EnumMap<>(TaskState.class);

    @Override public void visit(TaskRecord t) {
        // Pattern matching on record components
        switch (t) {
            case TaskRecord(int id, String title, String desc, TaskState st) -> {
                counts.merge(st, 1, Integer::sum);
            }
        }
    }

    public String result() {
        int todo = counts.getOrDefault(TaskState.TO_DO, 0);
        int prog = counts.getOrDefault(TaskState.IN_PROGRESS, 0);
        int done = counts.getOrDefault(TaskState.COMPLETED, 0);
        int all  = todo + prog + done;
        return "Total: " + all +
                "\nTO_DO: " + todo +
                "\nIN_PROGRESS: " + prog +
                "\nCOMPLETED: " + done;
    }
}
