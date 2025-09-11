package il.ac.hit.tasksapp.service.strategy;

import il.ac.hit.tasksapp.model.ITask;

import java.text.Collator;
import java.util.*;

/** Sort by title using the current locale. */
public final class SortByTitle implements SortStrategy {
    @Override public List<ITask> sort(List<ITask> input) {
        List<ITask> out = new ArrayList<>(input);
        Collator c = Collator.getInstance(Locale.getDefault());
        out.sort(Comparator.comparing(ITask::getTitle, Comparator.nullsFirst(c)));
        return out;
    }
}
