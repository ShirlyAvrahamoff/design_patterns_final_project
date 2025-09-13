package il.ac.hit.tasksapp.vm;

import il.ac.hit.tasksapp.dao.ITasksDAO;
import il.ac.hit.tasksapp.dao.TasksDAOException;
import il.ac.hit.tasksapp.dao.TasksDAOImpl;
import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.filter.TaskFilter;
import il.ac.hit.tasksapp.model.visitor.CsvVisitor;
import il.ac.hit.tasksapp.model.visitor.JsonVisitor;
import il.ac.hit.tasksapp.model.visitor.StatsVisitor;
import il.ac.hit.tasksapp.service.strategy.SortById;
import il.ac.hit.tasksapp.service.strategy.SortStrategy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel: holds the visible tasks list, current sort strategy, and current filter.
 * Fires "tasks" whenever the visible list changes, and "filter" when the filter text changes.
 */
public class TasksViewModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** DAO is a Singleton (see TasksDAOImpl.getInstance()). */
    private final ITasksDAO dao;

    /** Current visible list after filter + sort. */
    private List<ITask> tasks = List.of();

    /** Strategy pattern for sorting. */
    private SortStrategy sort = new SortById();

    /** Combinator filter used to test tasks. */
    private TaskFilter filter = TaskFilter.any();

    public TasksViewModel() throws TasksDAOException {
        this.dao = TasksDAOImpl.getInstance();   // Singleton accessor
        refresh();
        pcs.firePropertyChange("filter", null, getFilterDescription());
    }

    /* ------------ Observer wiring ------------ */

    public void addListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }

    public void removeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }

    /* ------------ Read model for the View ------------ */

    public List<ITask> getTasks() { return Collections.unmodifiableList(tasks); }

    public String getFilterDescription() { return filter.describe(); }

    public SortStrategy getSortStrategy() { return sort; }

    /** Used by Command objects (Add/Update/Delete). */
    public ITasksDAO getDaoForCommands() { return dao; }

    /* ------------ Mutations / reload ------------ */

    /** Reload from DAO, apply filter + sort, fire "tasks". */
    public void refresh() throws TasksDAOException {
        var raw = Arrays.asList(dao.getTasks());
        var filtered = raw.stream().filter(filter::test).toList();
        this.tasks = sort.sort(filtered);
        pcs.firePropertyChange("tasks", null, this.tasks);
    }

    /** Set a new sort strategy and refresh. */
    public void setSortStrategy(SortStrategy s) throws TasksDAOException {
        this.sort = (s == null ? new SortById() : s);
        refresh();
    }

    /** Set a new filter, fire "filter" text, then refresh. */
    public void setFilter(TaskFilter f) throws TasksDAOException {
        this.filter = (f == null ? TaskFilter.any() : f);
        pcs.firePropertyChange("filter", null, getFilterDescription());
        refresh();
    }

    /* ------------ Simple CRUD wrappers (optional if you use Commands directly) ------------ */

    public void add(ITask t) throws TasksDAOException { dao.addTask(t); refresh(); }

    public void update(ITask t) throws TasksDAOException { dao.updateTask(t); refresh(); }

    public void delete(int id) throws TasksDAOException { dao.deleteTask(id); refresh(); }

    /* ------------ Reports via Visitor (records + pattern matching in StatsVisitor) ------------ */

    /** Build CSV from current visible list. */
    public String buildCsvReport() {
        var v = new CsvVisitor();
        for (ITask t : tasks) t.accept(v);
        return v.result();
    }

    /** Build JSON from current visible list. */
    public String buildJsonReport() {
        var v = new JsonVisitor();
        for (ITask t : tasks) t.accept(v);
        return v.result();
    }

    /** Build simple state statistics string (uses record pattern matching). */
    public String buildStateStats() {
        var v = new StatsVisitor();
        for (ITask t : tasks) t.accept(v);
        return v.result();
    }
}
