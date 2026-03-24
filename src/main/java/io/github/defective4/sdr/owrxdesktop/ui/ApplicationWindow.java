package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import io.github.defective4.sdr.owrxdesktop.application.ReceiverEntry;
import io.github.defective4.sdr.owrxdesktop.application.UserStorage;
import io.github.defective4.sdr.owrxdesktop.ui.component.ReceiverEntryComponent;
import io.github.defective4.sdr.owrxdesktop.ui.component.ReceiverEntryContainer;

public class ApplicationWindow extends JFrame {

    private final ReceiverEntryContainer rxContainer = new ReceiverEntryContainer();

    private final ExecutorService updateExecutor = Executors.newFixedThreadPool(1);

    private final UserStorage userStorage = new UserStorage();

    public ApplicationWindow() {
        setBounds(100, 100, 768, 512);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        {
            JMenuBar menuBar = new JMenuBar();
            setJMenuBar(menuBar);

            JMenu mnApplication = new JMenu("Application");
            menuBar.add(mnApplication);

            JMenuItem mntmQuit = new JMenuItem("Quit");
            mntmQuit.addActionListener(e -> dispose());
            mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
            mnApplication.add(mntmQuit);

            JMenu mnEdit = new JMenu("Edit");
            menuBar.add(mnEdit);

            JMenuItem mntmAddReceiver = new JMenuItem("Add receiver...");
            mntmAddReceiver.addActionListener(e -> {
                String url = JOptionPane.showInputDialog(this, "Enter the receiver's url:", "Adding a new receiver",
                        JOptionPane.INFORMATION_MESSAGE);
                if (url != null) {
                    try {
                        URI.create(url).toURL();
                        ReceiverEntry entry = new ReceiverEntry(url, userStorage.getDefaultSettings());
                        userStorage.addEntry(entry);
                        entry.setQuerying();
                        ReceiverEntryComponent cpt = rxContainer.addEntry(entry);
                        updateEntryAsync(cpt);
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(this, "The URL address you entered is invalid", "Invalid URL",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            mntmAddReceiver.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
            mnEdit.add(mntmAddReceiver);

            mnEdit.add(new JSeparator());

            JMenuItem mntmDefaultReceiverSettings = new JMenuItem("Default receiver settings...");
            mntmDefaultReceiverSettings.addActionListener(e -> {
                if (SettingsDialog.show(this, userStorage.getDefaultSettings())) {
                    JOptionPane.showMessageDialog(this,
                            "These settings will only apply to new receivers.\n"
                                    + "For existing receivers, you need to adjust the settings per receiver.",
                            "New settings", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            mntmDefaultReceiverSettings
                    .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
            mnEdit.add(mntmDefaultReceiverSettings);

            JSeparator separator = new JSeparator();
            mnEdit.add(separator);

            JMenuItem mntmRefreshAll = new JMenuItem("Refresh all");
            mntmRefreshAll.addActionListener(e -> refreshPersonalReceivers());
            mnEdit.add(mntmRefreshAll);
        }

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        {
            JScrollPane scrollPane = new JScrollPane();
            tabbedPane.addTab("Personal", null, scrollPane, null);
            scrollPane.setViewportView(rxContainer);
        }

        updateEntries();
    }

    public void refreshPersonalReceivers() {
        rxContainer.getAllReceiverComponents().forEach(cpt -> {
            ReceiverEntry entry = cpt.getEntry();
            entry.setQuerying();
            cpt.updateEntry();
            updateEntryAsync(cpt);
        });
        invalidate();
    }

    public void updateEntries() {
        rxContainer.removeAll();
        userStorage.getUserEntries().forEach(rxContainer::addEntry);
    }

    public void updateEntryAsync(ReceiverEntryComponent cpt) {
        updateExecutor.submit(() -> {
            cpt.getEntry().query();
            cpt.updateEntry();
        });
    }
}
