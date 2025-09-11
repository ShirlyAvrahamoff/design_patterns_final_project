package il.ac.hit.tasksapp.dao;

import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.Task;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Simple in-memory DAO used only for tests (kept under src/test to avoid leaking into production JAR). */
public class InMemoryTasksDAO implements ITasksDAO {
    private final Map<Integer, ITask> data = new ConcurrentHashMap<>();

    @Override
    public synchronized ITask[] getTasks() throws TasksDAOException {
        return data.values().stream()
                .sorted(Comparator.comparingInt(ITask::getId))
                .toArray(ITask[]::new);
    }

    @Override
    public synchronized ITask getTask(int id) throws TasksDAOException {
        return data.get(id);
    }

    @Override
    public synchronized void addTask(ITask task) throws TasksDAOException {
        if (data.containsKey(task.getId())) throw new TasksDAOException("Duplicate id: " + task.getId());
        data.put(task.getId(), copy(task));
    }

    @Override
    public synchronized void updateTask(ITask task) throws TasksDAOException {
        if (!data.containsKey(task.getId())) throw new TasksDAOException("No such id: " + task.getId());
        data.put(task.getId(), copy(task));
    }

    @Override
    public synchronized void deleteTasks() throws TasksDAOException {
        data.clear();
    }

    @Override
    public synchronized void deleteTask(int id) throws TasksDAOException {
        data.remove(id);
    }

    private static ITask copy(ITask t) {
        return new Task(t.getId(), t.getTitle(), t.getDescription(), t.getState());
    }
}
