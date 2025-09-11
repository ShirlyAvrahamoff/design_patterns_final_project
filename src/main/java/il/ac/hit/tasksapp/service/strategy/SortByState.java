package il.ac.hit.tasksapp.service.strategy;

import il.ac.hit.tasksapp.model.ITask;

import java.util.*;

/** Sort by state (enum natural order). */
public final class SortByState implements SortStrategy {
    @Override public List<ITask> sort(List<ITask> input) {
        List<ITask> out = new ArrayList<>(input);
        out.sort(Comparator.comparing(ITask::getState, Comparator.nullsFirst(Comparator.naturalOrder())));
        return out;
    }
}
