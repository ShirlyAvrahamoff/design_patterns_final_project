
package il.ac.hit.tasksapp.app;

import il.ac.hit.tasksapp.view.TasksPanel;
import il.ac.hit.tasksapp.vm.TasksViewModel;

import javax.swing.*;

/** App entry. Builds the ViewModel and shows the Swing view. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(i.getName())) {
                        UIManager.setLookAndFeel(i.getClassName());
                        break;
                    }
                }
                TasksViewModel vm = new TasksViewModel();
                JFrame f = new JFrame("Tasks Management App");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setContentPane(new TasksPanel(vm));
                f.setSize(1100, 680);
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
