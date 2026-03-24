package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class ProgressDialog extends JDialog {

    private final JProgressBar progressBar = new JProgressBar();

    private ProgressDialog(Window parent, String text) {
        super(parent);
        setResizable(false);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(16, 32, 32, 16));
        getContentPane().add(panel, BorderLayout.CENTER);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] { 0, 0 };
        gbl_panel.rowHeights = new int[] { 0, 0, 0 };
        gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
        panel.setLayout(gbl_panel);

        JLabel lblProgress = new JLabel("Progress...");
        GridBagConstraints gbc_lblProgress = new GridBagConstraints();
        gbc_lblProgress.insets = new Insets(0, 0, 5, 0);
        gbc_lblProgress.gridx = 0;
        gbc_lblProgress.gridy = 0;
        panel.add(lblProgress, gbc_lblProgress);

        progressBar.setIndeterminate(true);
        GridBagConstraints gbc_progressBar = new GridBagConstraints();
        gbc_progressBar.gridx = 0;
        gbc_progressBar.gridy = 1;
        panel.add(progressBar, gbc_progressBar);

        pack();
        setLocationRelativeTo(parent);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public static <R> void show(Window parent, String text, Function<ProgressDialog, R> worker, Consumer<R> handler) {
        ProgressDialog dialog = new ProgressDialog(parent, text);
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
        new Thread(() -> {
            R result = worker.apply(dialog);
            SwingUtilities.invokeLater(() -> dialog.dispose());
            handler.accept(result);
        }).start();
    }
}
