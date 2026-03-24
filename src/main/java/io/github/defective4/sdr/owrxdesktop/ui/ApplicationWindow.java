package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import io.github.defective4.sdr.owrxdesktop.RadioReceiver;
import io.github.defective4.sdr.owrxdesktop.application.ReceiverEntry;
import io.github.defective4.sdr.owrxdesktop.application.StatusResponse;
import io.github.defective4.sdr.owrxdesktop.application.UserStorage;
import io.github.defective4.sdr.owrxdesktop.application.integration.PublicReceiverEntry;
import io.github.defective4.sdr.owrxdesktop.application.integration.ReceiverScraper;
import io.github.defective4.sdr.owrxdesktop.application.integration.SearchSort;
import io.github.defective4.sdr.owrxdesktop.application.integration.receiverbook.ReceiverbookScraper;
import io.github.defective4.sdr.owrxdesktop.ui.component.ReceiverEntryComponent;
import io.github.defective4.sdr.owrxdesktop.ui.component.ReceiverEntryContainer;

public class ApplicationWindow extends JFrame {
    private final ReceiverEntryContainer publicContainer = new ReceiverEntryContainer();
    private final ReceiverEntryContainer rxContainer = new ReceiverEntryContainer();
    private final ReceiverScraper scraper = new ReceiverbookScraper(this);
    private final ExecutorService updateExecutor;
    private final UserStorage userStorage = new UserStorage();



    public ApplicationWindow() {
        updateExecutor = Executors.newFixedThreadPool(userStorage.getApplicationSettings().getMaxNetworkWorkers());
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
                        ReceiverEntry entry = new ReceiverEntry(url, userStorage.getDefaultSettings().clone());
                        userStorage.addEntry(entry);
                        entry.setQuerying();
                        ReceiverEntryComponent cpt = addPersonalEntry(entry);
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

            JMenuItem mntmApplicationSettings = new JMenuItem("Application settings");
            mntmApplicationSettings.addActionListener(
                    e -> new ApplicationSettingsDialog(this, userStorage.getApplicationSettings()).setVisible(true));
            mntmApplicationSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            mnEdit.add(mntmApplicationSettings);
            mntmDefaultReceiverSettings
                    .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
            mnEdit.add(mntmDefaultReceiverSettings);

            JSeparator separator = new JSeparator();
            mnEdit.add(separator);

            JMenuItem mntmRefreshAll = new JMenuItem("Refresh all");
            mntmRefreshAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
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

        JPanel publicPanel = new JPanel();
        tabbedPane.addTab("Public", null, publicPanel, null);
        GridBagLayout gbl_publicPanel = new GridBagLayout();
        gbl_publicPanel.columnWidths = new int[] { 0, 0 };
        gbl_publicPanel.rowHeights = new int[] { 0, 0, 0 };
        gbl_publicPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_publicPanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        publicPanel.setLayout(gbl_publicPanel);

        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
        GridBagConstraints gbc_searchPanel = new GridBagConstraints();
        gbc_searchPanel.insets = new Insets(0, 0, 5, 0);
        gbc_searchPanel.fill = GridBagConstraints.BOTH;
        gbc_searchPanel.gridx = 0;
        gbc_searchPanel.gridy = 0;
        publicPanel.add(searchPanel, gbc_searchPanel);
        GridBagLayout gbl_searchPanel = new GridBagLayout();
        gbl_searchPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_searchPanel.rowHeights = new int[] { 0 };
        gbl_searchPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_searchPanel.rowWeights = new double[] { 0.0 };
        searchPanel.setLayout(gbl_searchPanel);

        JLabel lblSearch = new JLabel("Search");
        GridBagConstraints gbc_lblSearch = new GridBagConstraints();
        gbc_lblSearch.insets = new Insets(0, 0, 0, 5);
        gbc_lblSearch.anchor = GridBagConstraints.EAST;
        gbc_lblSearch.gridx = 0;
        gbc_lblSearch.gridy = 0;
        searchPanel.add(lblSearch, gbc_lblSearch);

        JTextField searchField = new JTextField();
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.insets = new Insets(0, 0, 0, 5);
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.gridx = 1;
        gbc_textField.gridy = 0;
        searchPanel.add(searchField, gbc_textField);

        JLabel lblLimit = new JLabel("Limit");
        GridBagConstraints gbc_lblLimit = new GridBagConstraints();
        gbc_lblLimit.insets = new Insets(0, 0, 0, 5);
        gbc_lblLimit.gridx = 2;
        gbc_lblLimit.gridy = 0;
        searchPanel.add(lblLimit, gbc_lblLimit);

        JSpinner limitSpinner = new JSpinner();
        limitSpinner.setModel(new SpinnerNumberModel(10, 1, 500, 1));
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.insets = new Insets(0, 0, 0, 5);
        gbc_spinner.gridx = 3;
        gbc_spinner.gridy = 0;
        searchPanel.add(limitSpinner, gbc_spinner);

        JLabel lblSort = new JLabel("Sort");
        GridBagConstraints gbc_lblSort = new GridBagConstraints();
        gbc_lblSort.anchor = GridBagConstraints.EAST;
        gbc_lblSort.insets = new Insets(0, 0, 0, 5);
        gbc_lblSort.gridx = 4;
        gbc_lblSort.gridy = 0;
        searchPanel.add(lblSort, gbc_lblSort);

        JComboBox<SearchSort> comboBox = new JComboBox<>();
        for(SearchSort sort : SearchSort.sortedValues())
            comboBox.addItem(sort);
        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.insets = new Insets(0, 0, 0, 5);
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.gridx = 5;
        gbc_comboBox.gridy = 0;
        searchPanel.add(comboBox, gbc_comboBox);

        JButton btnSearch = new JButton("Search");
        GridBagConstraints gbc_btnSearch = new GridBagConstraints();
        gbc_btnSearch.gridx = 6;
        gbc_btnSearch.gridy = 0;
        searchPanel.add(btnSearch, gbc_btnSearch);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 1;
        publicPanel.add(scrollPane, gbc_scrollPane);

        scrollPane.setViewportView(publicContainer);

        btnSearch.addActionListener(e -> {
            ProgressDialog.show(this, "Downloading receivers...", (dialog) -> {
                try {
                    if (!scraper.hasScraped()) scraper.scrapeReceivers();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Couldn't download receivers list:\n" + e1.toString(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }, t -> {

                String phrase = searchField.getText();
                int limit = (int) limitSpinner.getValue();

                List<PublicReceiverEntry> receivers = scraper.searchReceivers(phrase, limit, (SearchSort) comboBox.getSelectedItem());
                publicContainer.removeAll();
                receivers.forEach(receiver -> {
                    try {
                        ReceiverEntry entry = new ReceiverEntry(receiver.url(),
                                userStorage.getDefaultSettings().clone());
                        entry.setReceiverData(new StatusResponse(
                                new StatusResponse.Receiver(receiver.label(), null, receiver.location(), null), receiver.version()));
                        publicContainer.addEntry(entry, cpt -> {
                            JButton connectButton = new JButton("Connect");
                            connectButton.addActionListener(e2 -> {
                                setVisible(false);
                                ReceiverEntry rxEntry = cpt.getEntry();
                                try {
                                    RadioReceiver rx = new RadioReceiver(rxEntry.getWebsocketURI(),
                                            rxEntry.getSettings(), this, rxEntry.getCache());
                                    rx.setVisible(true);
                                    rx.connect();
                                } catch (LineUnavailableException | InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            });

                            JButton addButton = new JButton("Add to personal");
                            addButton.addActionListener(e2 -> {
                                ReceiverEntry rxEntry = cpt.getEntry();
                                userStorage.addEntry(rxEntry);
                                ReceiverEntryComponent newCpt = addPersonalEntry(rxEntry);
                                rxEntry.setQuerying();
                                newCpt.updateEntry();
                                updateEntryAsync(newCpt);
                            });

                            JButton queryButton = new JButton("Query");
                            queryButton.addActionListener(e2 -> {
                                ReceiverEntry rxEntry = cpt.getEntry();
                                rxEntry.setQuerying();
                                cpt.updateEntry();
                                updateEntryAsync(cpt);
                            });

                            return List.of(connectButton, addButton, queryButton);
                        });
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            });
            invalidate();
            repaint();
        });

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1
                    && userStorage.getApplicationSettings().isAutoDownloadPublicReceivers()) {
                btnSearch.doClick();
            }
        });

        updateEntries();
        if (userStorage.getApplicationSettings().isAutoRefreshPrivateReceivers()) {
            refreshPersonalReceivers();
        }
    }

    public UserStorage getUserStorage() {
        return userStorage;
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

    public void removeEntry(ReceiverEntryComponent entry) {
        userStorage.removeEntry(entry.getEntry());
        updateEntries();
        invalidate();
        repaint();
    }

    public void updateEntries() {
        rxContainer.removeAll();
        userStorage.getUserEntries().forEach(this::addPersonalEntry);
    }

    public void updateEntryAsync(ReceiverEntryComponent cpt) {
        updateExecutor.submit(() -> {
            cpt.getEntry().query();
            cpt.updateEntry();
        });
    }

    private ReceiverEntryComponent addPersonalEntry(ReceiverEntry entry) {
        ReceiverEntryComponent cpt = rxContainer.addEntry(entry, rxcpt -> {
            JButton connect = new JButton("Connect");
            connect.addActionListener(e -> {
                setVisible(false);
                ReceiverEntry rxEntry = rxcpt.getEntry();
                try {
                    RadioReceiver rx = new RadioReceiver(rxEntry.getWebsocketURI(), rxEntry.getSettings(), this,
                            rxEntry.getCache());
                    rx.setVisible(true);
                    rx.connect();
                } catch (LineUnavailableException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
            JButton refresh = new JButton("Refresh");
            refresh.addActionListener(e -> {
                rxcpt.getEntry().setQuerying();
                rxcpt.updateEntry();
                updateEntryAsync(rxcpt);
            });
            JButton more = new JButton("...");
            more.addActionListener(e -> {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem editRx = new JMenuItem("Edit receiver settings...");
                JMenuItem deleteRx = new JMenuItem("Remove");

                deleteRx.addActionListener(e2 -> removeEntry(rxcpt));
                editRx.addActionListener(e2 -> SettingsDialog.show(this, rxcpt.getEntry().getSettings()));

                menu.add(editRx);
                menu.add(deleteRx);
                menu.show(more, 0, 0);
            });
            return List.of(connect, refresh, more);
        });
        return cpt;
    }
}
