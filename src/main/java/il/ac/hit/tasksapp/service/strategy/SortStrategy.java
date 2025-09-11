package il.ac.hit.tasksapp.service.strategy;

import il.ac.hit.tasksapp.model.ITask;

import java.util.List;

/** Strategy for sorting tasks. Returns a new list. */
public interface SortStrategy {
    List<ITask> sort(List<ITask> input);
}
