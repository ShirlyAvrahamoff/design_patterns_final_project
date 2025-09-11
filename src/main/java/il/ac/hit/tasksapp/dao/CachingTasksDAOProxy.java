package il.ac.hit.tasksapp.dao;

import il.ac.hit.tasksapp.model.ITask;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy over an ITasksDAO that caches reads and invalidates on writes.
 * - getTasks(): caches the array and also fills a by-id map.
 * - getTask(id): uses the by-id cache and falls back to the real DAO.
 * - add/update/delete: delegate and invalidate caches.
 */
public class CachingTasksDAOProxy implements ITasksDAO {

    private final ITasksDAO real;
    private volatile ITask[] tasksCache;              // cache for list
    private final Map<Integer, ITask> taskByIdCache = new ConcurrentHashMap<>();

    public CachingTasksDAOProxy(ITasksDAO real) {
        this.real = real;
    }

    /* -------- READS (use cache) -------- */

    @Override
    public ITask[] getTasks() throws TasksDAOException {
        ITask[] cached = tasksCache;
        if (cached != null) return Arrays.copyOf(cached, cached.length);

        ITask[] fresh = real.getTasks();
        tasksCache = Arrays.copyOf(fresh, fresh.length);

        taskByIdCache.clear();
        for (ITask t : fresh) taskByIdCache.put(t.getId(), t);

        return fresh;
    }

    @Override
    public ITask getTask(int id) throws TasksDAOException {
        ITask cached = taskByIdCache.get(id);
        if (cached != null) return cached;
        ITask t = real.getTask(id);
        if (t != null) taskByIdCache.put(id, t);
        return t;
    }

    /* -------- WRITES (invalidate) -------- */

    @Override
    public void addTask(ITask task) throws TasksDAOException {
        real.addTask(task);
        invalidate();
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        real.updateTask(task);
        invalidate();
    }

    @Override
    public void deleteTasks() throws TasksDAOException {
        real.deleteTasks();
        invalidate();
    }

    @Override
    public void deleteTask(int id) throws TasksDAOException {
        real.deleteTask(id);
        invalidate();
    }

    private void invalidate() {
        tasksCache = null;
        taskByIdCache.clear();
    }
}
