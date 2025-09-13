package il.ac.hit.tasksapp.view;

import il.ac.hit.tasksapp.model.filter.TaskFilter;
import il.ac.hit.tasksapp.model.state.TaskState;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Filter dialog.
 *
 * Lets the user build a composable filter from three optional conditions:
 * 1) TITLE_CONTAINS (+ NOT)
 * 2) STATE_IS (+ NOT)
 * 3) ID_RANGE (from..to) (+ NOT)
 *
 * The active conditions are combined globally with AND / OR.
 * Result is returned as a TaskFilter (Combinator pattern).
 */
public final class AdvancedFilterDialog extends JDialog {

    /* ------- global combine ------- */
    private final JComboBox<String> combineBox =
            new JComboBox<>(new String[]{"AND", "OR"});

    /* ------- title contains ------- */
    private final JCheckBox useTitle = new JCheckBox();
    private final JTextField titleText = new JTextField(26);
    private final JCheckBox notTitle = new JCheckBox("NOT");

    /* ------- state is ------- */
    private final JCheckBox useState = new JCheckBox();
    private final JComboBox<TaskState> stateBox =
            new JComboBox<>(new TaskState[]{TaskState.TO_DO, TaskState.IN_PROGRESS, TaskState.COMPLETED});
    private final JCheckBox notState = new JCheckBox("NOT");

    /* ------- id range ------- */
    private final JCheckBox useId = new JCheckBox();
    private final JTextField idFrom = new JTextField(8);
    private final JTextField idTo   = new JTextField(8);
    private final JCheckBox notId = new JCheckBox("NOT");

    /* ------- result ------- */
    private TaskFilter result;

    private AdvancedFilterDialog(Window owner) {
        super(owner, "Advanced Filter", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        setResizable(false);

        // Only digits in ID fields
        DocumentFilter digits = new DigitsOnly();
        ((AbstractDocument) idFrom.getDocument()).setDocumentFilter(digits);
        ((AbstractDocument) idTo.getDocument()).setDocumentFilter(digits);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;

        /* --- top: combine --- */
        add(new JLabel("Combine:"), g);
        g.gridx = 1; combineBox.setPreferredSize(new Dimension(90, 26)); add(combineBox, g);
        g.gridx = 2; g.weightx = 1; add(new JLabel("(Select conditions below)"), g);
        g.gridy++; g.gridx = 0; g.weightx = 0;

        /* --- row: TITLE_CONTAINS (+ NOT) --- */
        add(new JLabel("When:"), g);
        g.gridx = 1; add(useTitle, g);
        g.gridx = 2; add(new JLabel("TITLE_CONTAINS"), g);
        g.gridx = 3; g.weightx = 1; titleText.setPreferredSize(new Dimension(320, 26)); add(titleText, g);
        g.gridx = 4; g.weightx = 0; add(notTitle, g);
        nextRow(g);

        /* --- row: STATE_IS (+ NOT) --- */
        add(new JLabel("When:"), g);
        g.gridx = 1; add(useState, g);
        g.gridx = 2; add(new JLabel("STATE_IS"), g);
        g.gridx = 3; stateBox.setPreferredSize(new Dimension(180, 26)); add(stateBox, g);
        g.gridx = 4; add(notState, g);
        nextRow(g);

        /* --- row: ID_RANGE (+ NOT) --- */
        add(new JLabel("When:"), g);
        g.gridx = 1; add(useId, g);
        g.gridx = 2; add(new JLabel("ID_RANGE"), g);

        JPanel idRange = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        idFrom.setPreferredSize(new Dimension(80, 26));
        idTo.setPreferredSize(new Dimension(80, 26));
        idRange.add(new JLabel("from:"));
        idRange.add(idFrom);
        idRange.add(new JLabel("to:"));
        idRange.add(idTo);

        g.gridx = 3; g.weightx = 1; add(idRange, g);
        g.gridx = 4; g.weightx = 0; add(notId, g);
        nextRow(g);

        /* --- buttons --- */
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton clear = new JButton("Clear all");
        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("Apply");
        btns.add(clear); btns.add(cancel); btns.add(ok);

        g.gridx = 0; g.gridwidth = 5; add(btns, g);

        clear.addActionListener(e -> clearAll());
        cancel.addActionListener(e -> { result = null; dispose(); });
        ok.addActionListener(e -> {
            try {
                result = buildFilter();
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Invalid filter", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    /** Utility to jump to next row in GridBag. */
    private static void nextRow(GridBagConstraints g) {
        g.gridy++; g.gridx = 0; g.gridwidth = 1; g.weightx = 0;
    }

    /**
     * Creates a TaskFilter from the user's selections.
     * Each enabled condition becomes one TaskFilter (possibly negated via .not()).
     * All enabled filters are then combined using AND/OR chosen at the top.
     */
    private TaskFilter buildFilter() {
        List<TaskFilter> parts = new ArrayList<>();

        if (useTitle.isSelected()) {
            String q = titleText.getText().trim();
            if (q.isEmpty()) throw new IllegalArgumentException("Title text is empty.");
            TaskFilter t = TaskFilter.byTitleContains(q);
            if (notTitle.isSelected()) t = t.not();
            parts.add(t);
        }

        if (useState.isSelected()) {
            TaskState st = (TaskState) stateBox.getSelectedItem();
            TaskFilter t = TaskFilter.byState(st);
            if (notState.isSelected()) t = t.not();
            parts.add(t);
        }

        if (useId.isSelected()) {
            Integer lo = parseInt(idFrom.getText());
            Integer hi = parseInt(idTo.getText());
            if (lo == null && hi == null) {
                throw new IllegalArgumentException("Enter at least one bound for ID range.");
            }
            int from = (lo == null ? Integer.MIN_VALUE : lo);
            int to   = (hi == null ? Integer.MAX_VALUE : hi);
            if (from > to) throw new IllegalArgumentException("ID range is invalid (from > to).");
            TaskFilter t = TaskFilter.byIdBetween(from, to);
            if (notId.isSelected()) t = t.not();
            parts.add(t);
        }

        if (parts.isEmpty()) return TaskFilter.any();

        boolean and = "AND".equals(combineBox.getSelectedItem());
        TaskFilter out = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            out = and ? out.and(parts.get(i)) : out.or(parts.get(i));
        }
        return out;
    }

    /** Clear all inputs. */
    private void clearAll() {
        combineBox.setSelectedIndex(0);
        useTitle.setSelected(false); titleText.setText(""); notTitle.setSelected(false);
        useState.setSelected(false); stateBox.setSelectedIndex(0); notState.setSelected(false);
        useId.setSelected(false); idFrom.setText(""); idTo.setText(""); notId.setSelected(false);
    }

    /** Parse integer or null if blank. */
    private static Integer parseInt(String s) {
        s = s == null ? "" : s.trim();
        if (s.isEmpty()) return null;
        return Integer.valueOf(s);
    }

    /** Text-field filter that allows only digits. */
    private static final class DigitsOnly extends DocumentFilter {
        @Override public void insertString(FilterBypass fb, int o, String s, AttributeSet a) throws BadLocationException {
            if (s != null && s.chars().allMatch(Character::isDigit)) super.insertString(fb, o, s, a);
        }
        @Override public void replace(FilterBypass fb, int o, int l, String s, AttributeSet a) throws BadLocationException {
            if (s != null && s.chars().allMatch(Character::isDigit)) super.replace(fb, o, l, s, a);
        }
    }

    /**
     * Shows the dialog and returns the composed TaskFilter, or null if cancelled.
     */
    public static TaskFilter showDialog(Window owner) {
        AdvancedFilterDialog d = new AdvancedFilterDialog(owner);
        d.setVisible(true);
        return d.result;
        // result is set by the OK/Cancel handlers
    }
}
