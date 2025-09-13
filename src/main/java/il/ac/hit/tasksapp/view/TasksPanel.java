package il.ac.hit.tasksapp.view;

import il.ac.hit.tasksapp.dao.TasksDAOException;
import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.Task;
import il.ac.hit.tasksapp.model.filter.TaskFilter;
import il.ac.hit.tasksapp.model.state.TaskState;
import il.ac.hit.tasksapp.service.command.AddTaskCommand;
import il.ac.hit.tasksapp.service.command.Command;
import il.ac.hit.tasksapp.service.command.CommandManager;
import il.ac.hit.tasksapp.service.command.DeleteTaskCommand;
import il.ac.hit.tasksapp.service.command.UpdateTaskCommand;
import il.ac.hit.tasksapp.service.strategy.SortById;
import il.ac.hit.tasksapp.service.strategy.SortByState;
import il.ac.hit.tasksapp.service.strategy.SortByTitle;
import il.ac.hit.tasksapp.vm.TasksViewModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Main Swing panel (View).
 * Top:    Task editor (ID/Title/Description/State + CRUD)
 * Middle: Filters (basic + Advanced...)
 * Bottom: Tools (Sort, Undo/Redo, Export, Stats) + table with a "Filter:" badge.
 */
public class TasksPanel extends JPanel {

    /* ---------------- UI palette ---------------- */
    private static final Color FG_TODO   = new Color(190, 50, 50);
    private static final Color FG_INPROG = new Color(170, 120, 0);
    private static final Color FG_DONE   = new Color(0, 130, 60);

    /* ------------ VM + Commands ------------ */
    private final TasksViewModel vm;
    private final CommandManager cmdMgr = new CommandManager();

    /* ------------ Table ------------ */
    private final TasksTableModel tableModel = new TasksTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel filterBadge = new JLabel("Filter: ANY");

    /* ------------ Row 1: task fields ------------ */
    private final JTextField idField = new JTextField(12);
    private final JTextField titleField = new JTextField(28);
    private final JTextField descriptionField = new JTextField(64);
    private final JComboBox<Object> stateBox =
            new JComboBox<>(new Object[]{"Select state…", TaskState.TO_DO, TaskState.IN_PROGRESS, TaskState.COMPLETED});

    /* ------------ Row 2: filters ------------ */
    private final JTextField findTitle = new JTextField(24);
    private final JCheckBox notTitle = new JCheckBox("NOT");
    private final JComboBox<Object> stateFilter =
            new JComboBox<>(new Object[]{"Any", TaskState.TO_DO, TaskState.IN_PROGRESS, TaskState.COMPLETED});
    private final JTextField idMinField = new JTextField(8);
    private final JTextField idMaxField = new JTextField(8);
    private final JComboBox<String> logicBox = new JComboBox<>(new String[]{"AND", "OR"});

    /* ------------ Row 3: tools ------------ */
    private final JComboBox<String> sortBox = new JComboBox<>(new String[]{"ID", "Title", "State"});

    /** Build the whole panel. */
    public TasksPanel(TasksViewModel vm) {
        this.vm = vm;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        // numeric-only
        ((AbstractDocument) idField.getDocument()).setDocumentFilter(new DigitsOnly());
        ((AbstractDocument) idMinField.getDocument()).setDocumentFilter(new DigitsOnly());
        ((AbstractDocument) idMaxField.getDocument()).setDocumentFilter(new DigitsOnly());

        // header with three stacked rows
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(buildRow1FieldsAndCrud());
        header.add(Box.createVerticalStrut(6));
        header.add(buildFiltersTight());
        header.add(Box.createVerticalStrut(6));
        header.add(buildRow3Utilities());
        add(header, BorderLayout.NORTH);

        // table area
        filterBadge.setFont(filterBadge.getFont().deriveFont(Font.ITALIC, 12f));
        JPanel badgeBar = new JPanel(new BorderLayout());
        badgeBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
        badgeBar.add(filterBadge, BorderLayout.WEST);

        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.add(badgeBar, BorderLayout.NORTH);
        tableArea.add(tableScroll, BorderLayout.CENTER);

        add(tableArea, BorderLayout.CENTER);

        // selection -> form
        table.getSelectionModel().addListSelectionListener(this::onSelect);

        // observe VM
        vm.addListener(evt -> {
            switch (evt.getPropertyName()) {
                case "tasks" -> SwingUtilities.invokeLater(() -> {
                    tableModel.setData(vm.getTasks());
                    tuneColumns();
                });
                case "filter" -> SwingUtilities.invokeLater(() ->
                        filterBadge.setText("Filter: " + vm.getFilterDescription()));
            }
        });

        // initial data
        doSafe(() -> {
            vm.refresh();
            filterBadge.setText("Filter: " + vm.getFilterDescription());
            tuneColumns();
        });
    }

    /* ---------------- Row builders ---------------- */

    /** Row 1: Task fields + CRUD buttons (Add/Update/Delete/Delete All/Clear). */
    private JComponent buildRow1FieldsAndCrud() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Field sizes
        idField.setColumns(6);
        Dimension idDim = new Dimension(90, idField.getPreferredSize().height);
        idField.setMinimumSize(idDim);
        idField.setPreferredSize(idDim);

        titleField.setColumns(40);
        descriptionField.setColumns(64);
        stateBox.setPreferredSize(new Dimension(160, 26));

        int r = 0, c = 0;
        g.gridy = r;

        // ID
        g.gridx = c++; g.weightx = 0; content.add(new JLabel("ID:"), g);
        g.gridx = c++; g.weightx = 0; content.add(idField, g);

        // Title (takes extra width)
        g.gridx = c++; g.weightx = 0; content.add(new JLabel("Title:"), g);
        g.gridx = c++; g.weightx = 1; content.add(titleField, g);

        // State
        g.gridx = c++; g.weightx = 0; content.add(new JLabel("State:"), g);
        g.gridx = c++; g.weightx = 0; content.add(stateBox, g);

        // CRUD buttons
        JButton addBtn    = new JButton("Add");
        JButton updBtn    = new JButton("Update");
        JButton delBtn    = new JButton("Delete");
        JButton delAllBtn = new JButton("Delete All");
        JButton clearBtn  = new JButton("Clear");

        JPanel crud = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        crud.setOpaque(false);
        crud.add(addBtn);
        crud.add(updBtn);
        crud.add(delBtn);
        crud.add(delAllBtn);
        crud.add(clearBtn);

        g.gridx = c++; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        content.add(crud, g);

        // Second line – description
        r++; c = 0; g.gridy = r;
        g.gridwidth = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = c++; g.weightx = 0; content.add(new JLabel("Description:"), g);
        g.gridx = c;   g.weightx = 1; g.gridwidth = 7;
        content.add(descriptionField, g);
        g.gridwidth = 1;

        // Actions
        addBtn.addActionListener(e -> doSafe(() -> {
            ITask t = readTaskFromForm();
            cmdMgr.doCommand(new AddTaskCommand(vm.getDaoForCommands(), t));
            vm.refresh();
            clearForm();
        }));

        updBtn.addActionListener(e -> doSafe(() -> {
            ITask t = readTaskFromForm();
            cmdMgr.doCommand(new UpdateTaskCommand(vm.getDaoForCommands(), t));
            vm.refresh();
        }));

        delBtn.addActionListener(e -> doSafe(() -> {
            int rowIdx = table.getSelectedRow();
            if (rowIdx < 0) throw new IllegalArgumentException("Select a row to delete.");
            int id = (Integer) tableModel.getValueAt(rowIdx, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete task ID " + id + "?",
                    "Confirm", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                cmdMgr.doCommand(new DeleteTaskCommand(vm.getDaoForCommands(), id));
                vm.refresh();
                clearForm();
            }
        }));

        delAllBtn.addActionListener(e -> doSafe(() -> {
            if (JOptionPane.showConfirmDialog(this, "Delete ALL tasks?",
                    "Confirm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

            cmdMgr.doCommand(new Command() {
                private java.util.List<ITask> snapshot;

                @Override public void execute() {
                    try {
                        snapshot = java.util.Arrays.asList(vm.getDaoForCommands().getTasks());
                        vm.getDaoForCommands().deleteTasks();
                    } catch (TasksDAOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                @Override public void undo() {
                    if (snapshot == null) return;
                    try {
                        for (ITask t : snapshot) vm.getDaoForCommands().addTask(t);
                    } catch (TasksDAOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            vm.refresh();
            clearForm();
        }));

        clearBtn.addActionListener(e -> clearForm());

        return wrapTitledSection("Task", content);
    }

    /** Row 2: Filters (compact, clean, no colored background). */
    private JComponent buildFiltersTight() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        // Sizes
        findTitle.setColumns(24);
        stateFilter.setPreferredSize(new Dimension(160, 26));
        idMinField.setColumns(6);
        idMaxField.setColumns(6);
        logicBox.setPreferredSize(new Dimension(90, 26));

        JButton apply = new JButton("Apply");
        JButton clear = new JButton("Clear");
        JButton adv   = new JButton("Advanced…");

        apply.addActionListener(e -> doSafe(() -> vm.setFilter(buildFilterFromUI())));
        clear.addActionListener(e -> doSafe(() -> {
            findTitle.setText(""); notTitle.setSelected(false);
            stateFilter.setSelectedIndex(0);
            idMinField.setText(""); idMaxField.setText("");
            logicBox.setSelectedIndex(0);
            vm.setFilter(TaskFilter.any());
        }));
        adv.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(TasksPanel.this);
            TaskFilter f = AdvancedFilterDialog.showDialog(owner);
            if (f != null) doSafe(() -> vm.setFilter(f));
        });

        java.util.function.BiFunction<String, JComponent, JPanel> pair = (txt, comp) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            p.setOpaque(false);
            JLabel l = new JLabel(txt);
            l.setLabelFor(comp);
            p.add(l);
            p.add(comp);
            return p;
        };

        // ID range
        JPanel idRange = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        idRange.setOpaque(false);
        idRange.add(new JLabel("ID range:"));
        idRange.add(idMinField);
        idRange.add(new JLabel("to"));
        idRange.add(idMaxField);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.anchor = GridBagConstraints.BASELINE_LEADING;

        int col = 0;

        // Row 1
        g.gridy = 0;
        g.gridx = col++; content.add(pair.apply("Title contains:", findTitle), g);
        g.gridx = col++; content.add(notTitle, g);
        g.gridx = col++; content.add(pair.apply("State:", stateFilter), g);
        g.gridx = col++; g.weightx = 1; content.add(Box.createHorizontalGlue(), g);
        g.weightx = 0;

        // Row 2
        g.gridy = 1; col = 0;
        g.gridx = col++; content.add(idRange, g);
        g.gridx = col++; content.add(pair.apply("Combine:", logicBox), g);

        g.gridx = col++; g.weightx = 1; content.add(Box.createHorizontalGlue(), g);
        g.weightx = 0;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        btns.add(apply); btns.add(clear); btns.add(adv);
        g.gridx = col; content.add(btns, g);

        // Enter = Apply
        SwingUtilities.invokeLater(() -> {
            JRootPane rp = SwingUtilities.getRootPane(content);
            if (rp != null) rp.setDefaultButton(apply);
        });

        return wrapTitledSection("Filter", content);
    }

    /** Row 3: sort/undo/redo/export/stats. */
    private JComponent buildRow3Utilities() {
        JPanel row = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 8, 0, 8);
        g.anchor = GridBagConstraints.WEST;

        int c = 0;
        g.gridx = c++; g.gridy = 0; row.add(new JLabel("Sort:"), g);
        sortBox.setPreferredSize(new Dimension(140, 26));
        g.gridx = c++; row.add(sortBox, g);

        g.gridx = c++; g.weightx = 1; row.add(Box.createHorizontalGlue(), g);
        g.weightx = 0;

        JButton undo = new JButton("Undo");
        JButton redo = new JButton("Redo");
        JButton export = new JButton("Export");
        JButton stats  = new JButton("Stats");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.add(undo); right.add(redo); right.add(export); right.add(stats);
        g.gridx = c; row.add(right, g);

        sortBox.addActionListener(e -> doSafe(() -> vm.setSortStrategy(
                switch ((String) sortBox.getSelectedItem()) {
                    case "Title" -> new SortByTitle();
                    case "State" -> new SortByState();
                    default      -> new SortById();
                })));

        undo.addActionListener(e -> doSafe(() -> { cmdMgr.undo(); vm.refresh(); }));
        redo.addActionListener(e -> doSafe(() -> { cmdMgr.redo(); vm.refresh(); }));

        // Export menu (CSV / JSON)
        JPopupMenu menu = new JPopupMenu();
        JMenuItem csv = new JMenuItem("CSV");
        JMenuItem json = new JMenuItem("JSON");
        menu.add(csv); menu.add(json);
        export.addActionListener(e -> menu.show(export, 0, export.getHeight()));
        csv.addActionListener(e -> doSafe(() -> save(vm.buildCsvReport(), "tasks.csv")));
        json.addActionListener(e -> doSafe(() -> save(vm.buildJsonReport(), "tasks.json")));

        // stats popup
        stats.addActionListener(e -> doSafe(() ->
                JOptionPane.showMessageDialog(this, vm.buildStateStats(),
                        "State Stats", JOptionPane.INFORMATION_MESSAGE)));

        return row;
    }

    /* ---------------- Helpers ---------------- */

    /** Wraps inner content with a titled section and subtle inner padding (no background color). */
    private JComponent wrapTitledSection(String title, JComponent inner) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createTitledBorder(title));

        JPanel pad = new JPanel(new BorderLayout());
        pad.setOpaque(false);
        pad.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        inner.setOpaque(false);
        pad.add(inner, BorderLayout.CENTER);
        outer.add(pad, BorderLayout.CENTER);
        return outer;
    }

    /** Build a TaskFilter from the basic controls. */
    private TaskFilter buildFilterFromUI() {
        boolean useAnd = "AND".equals(logicBox.getSelectedItem());

        java.util.List<TaskFilter> parts = new java.util.ArrayList<>();

        String q = findTitle.getText().trim();
        if (!q.isEmpty()) {
            TaskFilter t = TaskFilter.byTitleContains(q);
            if (notTitle.isSelected()) t = t.not();
            parts.add(t);
        }

        Object sf = stateFilter.getSelectedItem();
        if (sf instanceof TaskState st) parts.add(TaskFilter.byState(st));

        Integer min = idMinField.getText().isBlank() ? null : Integer.parseInt(idMinField.getText());
        Integer max = idMaxField.getText().isBlank() ? null : Integer.parseInt(idMaxField.getText());
        if (min != null || max != null) {
            int lo = (min == null ? Integer.MIN_VALUE : min);
            int hi = (max == null ? Integer.MAX_VALUE : max);
            if (lo > hi) throw new IllegalArgumentException("ID range is invalid (from > to).");
            parts.add(TaskFilter.byIdBetween(lo, hi));
        }

        if (parts.isEmpty()) return TaskFilter.any();

        TaskFilter out = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            out = useAnd ? out.and(parts.get(i)) : out.or(parts.get(i));
        }
        return out;
    }

    /** Selection in table -> fill the form. */
    private void onSelect(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int r = table.getSelectedRow();
        if (r < 0) return;
        ITask t = tableModel.getTaskAt(r);
        idField.setText(String.valueOf(t.getId()));
        titleField.setText(t.getTitle());
        descriptionField.setText(t.getDescription());
        stateBox.setSelectedItem(t.getState());
    }

    /** Read & validate form -> Task. */
    private ITask readTaskFromForm() {
        String idText = idField.getText().trim();
        String title  = titleField.getText().trim();
        String desc   = descriptionField.getText().trim();
        Object sel    = stateBox.getSelectedItem();

        if (idText.isEmpty()) { idField.requestFocus(); throw new IllegalArgumentException("ID is required (e.g., 1)."); }
        int id;
        try { id = Integer.parseInt(idText); }
        catch (NumberFormatException nfe) { idField.requestFocus(); throw new IllegalArgumentException("ID must be a whole number (e.g., 1)."); }
        if (id < 0) { idField.requestFocus(); throw new IllegalArgumentException("ID must be >= 0."); }

        if (title.isEmpty()) { titleField.requestFocus(); throw new IllegalArgumentException("Title is required (max 255)."); }
        if (title.length() > 255) { titleField.requestFocus(); throw new IllegalArgumentException("Title is too long (max 255)."); }
        if (desc.length() > 500) { descriptionField.requestFocus(); throw new IllegalArgumentException("Description is too long (max 500)."); }

        if (!(sel instanceof TaskState st)) { stateBox.requestFocus(); throw new IllegalArgumentException("Please choose a state."); }

        return new Task(id, title, desc, st);
    }

    /** Show error dialog on failure. */
    private void doSafe(Action a) {
        try { a.run(); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }

    /** Clear form to defaults. */
    private void clearForm() {
        idField.setText("");
        titleField.setText("");
        descriptionField.setText("");
        stateBox.setSelectedIndex(0);
        table.clearSelection();
        idField.requestFocus();
    }

    /** Column widths, alignment, and elegant state coloring (foreground only). */
    private void tuneColumns() {
        var cm = table.getColumnModel();
        if (cm.getColumnCount() < 4) return;

        int w;
        Component p = table.getParent();
        if (p instanceof JViewport vp) w = vp.getExtentSize().width;
        else w = table.getWidth();
        if (w <= 0) w = 1050;

        int idW    = Math.max(70,  (int)(w * 0.08));
        int titleW = Math.max(220, (int)(w * 0.28));
        int descW  = Math.max(400, (int)(w * 0.50));
        int stateW = Math.max(110, (int)(w * 0.14));

        cm.getColumn(0).setPreferredWidth(idW);
        cm.getColumn(1).setPreferredWidth(titleW);
        cm.getColumn(2).setPreferredWidth(descW);
        cm.getColumn(3).setPreferredWidth(stateW);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftWithTooltip = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setToolTipText(value == null ? null : value.toString());
                return c;
            }
        };

        // Foreground-only color for state; keep background default and preserve selection.
        DefaultTableCellRenderer stateRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSel, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                // Default colors first
                if (isSel) {
                    c.setForeground(tbl.getSelectionForeground());
                    c.setBackground(tbl.getSelectionBackground());
                } else {
                    c.setBackground(tbl.getBackground());
                    c.setForeground(tbl.getForeground());
                    if (value != null) {
                        switch (value.toString()) {
                            case "TO_DO"       -> c.setForeground(FG_TODO);
                            case "IN_PROGRESS" -> c.setForeground(FG_INPROG);
                            case "COMPLETED"   -> c.setForeground(FG_DONE);
                            default            -> c.setForeground(tbl.getForeground());
                        }
                    }
                }
                return c;
            }
        };

        cm.getColumn(0).setCellRenderer(center);          // ID centered
        cm.getColumn(1).setCellRenderer(leftWithTooltip); // Title left
        cm.getColumn(2).setCellRenderer(leftWithTooltip); // Description left
        cm.getColumn(3).setCellRenderer(stateRenderer);   // State colored text + centered
    }

    /** Save text to a file via chooser. */
    private void save(String text, String defaultName) throws Exception {
        var chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Files.writeString(chooser.getSelectedFile().toPath(), text, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Saved: " + chooser.getSelectedFile().getAbsolutePath(),
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** DocumentFilter: digits only. */
    private static class DigitsOnly extends DocumentFilter {
        @Override public void insertString(FilterBypass fb, int o, String s, AttributeSet a) throws BadLocationException {
            if (s != null && s.chars().allMatch(Character::isDigit)) super.insertString(fb, o, s, a);
        }
        @Override public void replace(FilterBypass fb, int o, int l, String s, AttributeSet a) throws BadLocationException {
            if (s != null && s.chars().allMatch(Character::isDigit)) super.replace(fb, o, l, s, a);
        }
    }
}
