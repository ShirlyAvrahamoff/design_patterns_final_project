package il.ac.hit.tasksapp.service.strategy;

import il.ac.hit.tasksapp.model.ITask;

import java.util.*;

/** Sort by numeric id, ascending. */
public final class SortById implements SortStrategy {
    @Override public List<ITask> sort(List<ITask> input) {
        List<ITask> out = new ArrayList<>(input);
        out.sort(Comparator.comparingInt(ITask::getId));
        return out;
    }
}
