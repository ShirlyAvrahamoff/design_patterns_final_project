package il.ac.hit.tasksapp.view;

import il.ac.hit.tasksapp.model.ITask;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Table model for tasks (ID, Title, Description, State). */
public class TasksTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Title", "Description", "State"};
    private final List<ITask> data = new ArrayList<>();

    public void setData(java.util.List<ITask> tasks) {
        data.clear();
        data.addAll(tasks);
        fireTableDataChanged();
    }

    public ITask getTaskAt(int row) { return data.get(row); }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ITask t = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> t.getId();
            case 1 -> t.getTitle();
            case 2 -> t.getDescription();
            case 3 -> t.getState();
            default -> null;
        };
    }

    @Override public Class<?> getColumnClass(int c) {
        return switch (c) {
            case 0 -> Integer.class;
            case 3 -> Enum.class;
            default -> String.class;
        };
    }
}
