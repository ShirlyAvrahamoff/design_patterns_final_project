package il.ac.hit.tasksapp.vm;

import il.ac.hit.tasksapp.dao.CachingTasksDAOProxy;
import il.ac.hit.tasksapp.dao.ITasksDAO;
import il.ac.hit.tasksapp.dao.TasksDAOException;
import il.ac.hit.tasksapp.dao.TasksDAOImpl;
import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.filter.TaskFilter;
import il.ac.hit.tasksapp.model.record.TaskRecord;
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
    private final ITasksDAO dao;

    private List<ITask> tasks = List.of();
    private SortStrategy sort = new SortById();
    private TaskFilter filter = TaskFilter.any();

    public TasksViewModel() throws TasksDAOException {
        // Wrap the singleton DAO with the Proxy so caching is active at runtime.
        this.dao = new CachingTasksDAOProxy(TasksDAOImpl.getInstance());
        refresh();
        pcs.firePropertyChange("filter", null, getFilterDescription());
    }

    public void addListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }

    public List<ITask> getTasks() { return Collections.unmodifiableList(tasks); }
    public String getFilterDescription() { return filter.describe(); }
    public SortStrategy getSortStrategy() { return sort; }
    public ITasksDAO getDaoForCommands() { return dao; }

    public void refresh() throws TasksDAOException {
        var raw = Arrays.asList(dao.getTasks());
        var filtered = raw.stream().filter(filter::test).toList();
        this.tasks = sort.sort(filtered);
        pcs.firePropertyChange("tasks", null, this.tasks);
    }

    public void setSortStrategy(SortStrategy s) throws TasksDAOException {
        this.sort = (s == null ? new SortById() : s);
        refresh();
    }

    public void setFilter(TaskFilter f) throws TasksDAOException {
        this.filter = (f == null ? TaskFilter.any() : f);
        pcs.firePropertyChange("filter", null, getFilterDescription());
        refresh();
    }

    public void add(ITask t) throws TasksDAOException { dao.addTask(t); refresh(); }
    public void update(ITask t) throws TasksDAOException { dao.updateTask(t); refresh(); }
    public void delete(int id) throws TasksDAOException { dao.deleteTask(id); refresh(); }

    /** Reports via Visitor (records + pattern matching in visitors). */
    public String buildCsvReport() {
        var v = new CsvVisitor();
        for (ITask t : tasks) v.visit(TaskRecord.from(t));
        return v.result();
    }

    public String buildJsonReport() {
        var v = new JsonVisitor();
        for (ITask t : tasks) v.visit(TaskRecord.from(t));
        return v.result();
    }

    public String buildStateStats() {
        var v = new StatsVisitor();
        for (ITask t : tasks) v.visit(TaskRecord.from(t));
        return v.result();
    }
}
