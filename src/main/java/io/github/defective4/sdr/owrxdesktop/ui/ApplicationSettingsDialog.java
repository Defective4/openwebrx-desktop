package io.github.defective4.sdr.owrxdesktop.ui;

import static io.github.defective4.sdr.owrxdesktop.ui.text.FontAwesome.*;
import static java.nio.charset.StandardCharsets.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.defective4.sdr.owrxdesktop.application.ApplicationSettings;
import io.github.defective4.sdr.owrxdesktop.application.integration.location.Location;
import io.github.defective4.sdr.owrxdesktop.application.integration.location.LocationServices;
import io.github.defective4.sdr.owrxdesktop.audio.FFMpeg;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;
import io.github.defective4.sdr.owrxdesktop.bandplan.SerializedBandplan;
import io.github.defective4.sdr.owrxdesktop.bandplan.reader.BandplanReader;
import io.github.defective4.sdr.owrxdesktop.bandplan.reader.BandplanReaderFactory;
import io.github.defective4.sdr.owrxdesktop.bandplan.reader.GQRXBandplanReader;
import io.github.defective4.sdr.owrxdesktop.bandplan.reader.OWRXBandplanReader;
import io.github.defective4.sdr.owrxdesktop.bandplan.reader.SDRConsoleBandplanReader;
import io.github.defective4.sdr.owrxdesktop.bandplan.reader.SDRPPBandplanReader;
import io.github.defective4.sdr.owrxdesktop.bandplan.reader.SDRSharpBandplanReader;
import io.github.defective4.sdr.owrxdesktop.bandplan.render.BandplanListRenderer;
import io.github.defective4.sdr.owrxdesktop.ui.text.FontAwesome;

public class ApplicationSettingsDialog extends JDialog {

    private final JCheckBox autoDownloadCheck = new JCheckBox("Auto-download public receiver listings");
    private final JCheckBox autoRefreshCheck = new JCheckBox("Auto-refresh receivers on startup");
    private final JList<SerializedBandplan> bandsList = new JList<>();
    private final DefaultListModel<SerializedBandplan> bandsModel = new DefaultListModel<>();
    private final JTextField ffmpegPath;
    private final JSpinner latSpinner = new JSpinner();
    private final JSpinner lonSpinner = new JSpinner();
    private final JSpinner networkWorkers = new JSpinner();
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

    public ApplicationSettingsDialog(Window parent, ApplicationSettings settings) {
        super(parent);
        setTitle("Application settings");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            contentPanel.add(tabbedPane, BorderLayout.CENTER);
            {
                JPanel panel = new JPanel();
                panel.setBorder(new EmptyBorder(0, 5, 0, 5));
                tabbedPane.addTab("Network", ICO_NETWORK, panel, null);
                GridBagLayout gbl_panel = new GridBagLayout();
                gbl_panel.columnWidths = new int[] { 0, 0 };
                gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
                gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
                gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
                panel.setLayout(gbl_panel);
                {
                    JPanel networkWorkersPanel = new JPanel();
                    networkWorkersPanel.setBorder(new TitledBorder(null, "Max network workers", TitledBorder.LEADING,
                            TitledBorder.TOP, null, null));
                    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
                    gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
                    gbc_panel_1.insets = new Insets(0, 0, 5, 0);
                    gbc_panel_1.anchor = GridBagConstraints.NORTH;
                    gbc_panel_1.gridx = 0;
                    gbc_panel_1.gridy = 0;
                    panel.add(networkWorkersPanel, gbc_panel_1);
                    networkWorkersPanel.setLayout(new BoxLayout(networkWorkersPanel, BoxLayout.Y_AXIS));
                    networkWorkers.setAlignmentX(Component.LEFT_ALIGNMENT);
                    networkWorkers.setModel(new SpinnerNumberModel(1, 1, 100, 1));
                    networkWorkers.setValue(settings.getMaxNetworkWorkers());
                    networkWorkersPanel.add(networkWorkers);
                    {
                        JLabel lblThisChangeRequires = new JLabel("This change requires application restart");
                        networkWorkersPanel.add(lblThisChangeRequires);
                    }
                }
                {
                    GridBagConstraints gbc_chckbxAutorefreshReceiversOn = new GridBagConstraints();
                    gbc_chckbxAutorefreshReceiversOn.fill = GridBagConstraints.HORIZONTAL;
                    gbc_chckbxAutorefreshReceiversOn.insets = new Insets(0, 0, 5, 0);
                    gbc_chckbxAutorefreshReceiversOn.gridx = 0;
                    gbc_chckbxAutorefreshReceiversOn.gridy = 1;
                    autoRefreshCheck.setSelected(settings.isAutoRefreshPrivateReceivers());
                    panel.add(autoRefreshCheck, gbc_chckbxAutorefreshReceiversOn);
                }
                {
                    GridBagConstraints gbc_chckbxAuto = new GridBagConstraints();
                    gbc_chckbxAuto.fill = GridBagConstraints.HORIZONTAL;
                    gbc_chckbxAuto.gridx = 0;
                    gbc_chckbxAuto.gridy = 2;
                    autoDownloadCheck.setSelected(settings.isAutoDownloadPublicReceivers());
                    panel.add(autoDownloadCheck, gbc_chckbxAuto);
                }
            }
            {
                JPanel panel = new JPanel();
                panel.setBorder(new EmptyBorder(16, 8, 16, 8));
                tabbedPane.addTab("Bandplan", ICO_TAGS, panel, null);
                GridBagLayout gbl_panel = new GridBagLayout();
                gbl_panel.columnWidths = new int[] { 0, 0, 0 };
                gbl_panel.rowHeights = new int[] { 0, 0 };
                gbl_panel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
                gbl_panel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
                panel.setLayout(gbl_panel);
                bandsList.setCellRenderer(new BandplanListRenderer());
                bandsList.setModel(bandsModel);
                {
                    JScrollPane scrollPane = new JScrollPane();
                    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
                    gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
                    gbc_scrollPane.fill = GridBagConstraints.BOTH;
                    gbc_scrollPane.gridx = 0;
                    gbc_scrollPane.gridy = 0;
                    panel.add(scrollPane, gbc_scrollPane);
                    {
                        scrollPane.setViewportView(bandsList);
                    }
                }
                {
                    JPanel panel_1 = new JPanel();
                    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
                    gbc_panel_1.anchor = GridBagConstraints.WEST;
                    gbc_panel_1.fill = GridBagConstraints.VERTICAL;
                    gbc_panel_1.gridx = 1;
                    gbc_panel_1.gridy = 0;
                    panel.add(panel_1, gbc_panel_1);
                    panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
                    JButton importButton = new JButton(FA_PLUS);
                    setFontAwesomeFont(importButton);
                    panel_1.add(importButton);
                    JButton delButton = new JButton(FA_MINUS);
                    setFontAwesomeFont(delButton);
                    delButton.setEnabled(false);
                    panel_1.add(delButton);

                    settings.getLoadedBandplans().forEach(i -> bandsModel.addElement(i));

                    importButton.addActionListener(e -> {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem gqrx = new JMenuItem("GQRX CSV file");

                        gqrx.addActionListener(e2 -> {
                            showBandplanChooser(settings, "GQRX CSV Files", "csv",
                                    "This is not a valid GQRX bandplan file", GQRXBandplanReader.FACTORY, UTF_8);
                        });

                        JMenuItem sdrpp = new JMenuItem("SDR++ JSON file");

                        sdrpp.addActionListener(e2 -> {
                            showBandplanChooser(settings, "SDR++ JSON Files", "json",
                                    "This is not a valid SDR++ bandplan file", SDRPPBandplanReader.FACTORY, UTF_8);
                        });

                        JMenuItem sdrsharp = new JMenuItem("SDR# XML file");

                        sdrsharp.addActionListener(e2 -> {
                            showBandplanChooser(settings, "SDR# XML Files", "xml",
                                    "This is not a valid SDR# bandplan file", SDRSharpBandplanReader.FACTORY, UTF_8);
                        });

                        JMenuItem sdrconsole = new JMenuItem("SDR Console XML file");

                        sdrconsole.addActionListener(e2 -> {
                            showBandplanChooser(settings, "SDR Console XML Files", "xml",
                                    "This is not a valid SDR Console bandplan file", SDRConsoleBandplanReader.FACTORY,
                                    UTF_16LE);
                        });

                        JMenuItem owrx = new JMenuItem("OpenWebRX JSON file");

                        owrx.addActionListener(e2 -> {
                            showBandplanChooser(settings, "OpenWebRX JSON Files", "json",
                                    "This is not a valid OpenWebRX bandplan file", OWRXBandplanReader.FACTORY, UTF_8);
                        });

                        JMenuItem blank = new JMenuItem("Blank band plan");

                        blank.addActionListener(e2 -> {
                            List<SerializedBandplan> bps = new ArrayList<>(settings.getLoadedBandplans());
                            bps.add(new Bandplan(Set.of(), Map.of("default", Color.red), "[Custom] New band plan")
                                    .serialize());
                            settings.setLoadedBandplans(bps);
                            bandsModel.removeAllElements();
                            settings.getLoadedBandplans().forEach(i -> bandsModel.addElement(i));
                        });

                        menu.add(blank);
                        menu.add(new JSeparator());
                        menu.add(gqrx);
                        menu.add(sdrpp);
                        menu.add(sdrsharp);
                        menu.add(sdrconsole);
                        menu.add(new JSeparator());
                        menu.add(owrx);
                        menu.show(importButton, 0, importButton.getHeight());
                    });

                    delButton.addActionListener(e -> {
                        delButton.setEnabled(false);
                        List<SerializedBandplan> bands = new ArrayList<>(settings.getLoadedBandplans());
                        SerializedBandplan value = bandsList.getSelectedValue();
                        bandsModel.removeElement(value);
                        bands.remove(value);
                        settings.setLoadedBandplans(bands);
                    });

                    bandsList.addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting()) {
                            SerializedBandplan value = bandsList.getSelectedValue();
                            delButton.setEnabled(value != null);
                        }
                    });
                }
            }
            {
                JPanel panel = new JPanel();
                tabbedPane.addTab("Listings", FontAwesome.ICO_TASKS, panel, null);
                GridBagLayout gbl_panel = new GridBagLayout();
                gbl_panel.columnWidths = new int[] { 0, 0 };
                gbl_panel.rowHeights = new int[] { 0, 0 };
                gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
                gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
                panel.setLayout(gbl_panel);
                {
                    JPanel panel_1 = new JPanel();
                    panel_1.setBorder(new TitledBorder(null, "Your location", TitledBorder.LEADING, TitledBorder.TOP,
                            null, null));
                    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
                    gbc_panel_1.fill = GridBagConstraints.BOTH;
                    gbc_panel_1.gridx = 0;
                    gbc_panel_1.gridy = 0;
                    panel.add(panel_1, gbc_panel_1);
                    panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
                    JPanel panel_2 = new JPanel();
                    panel_2.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panel_1.add(panel_2);
                    panel_2.setLayout(new GridLayout(2, 2, 16, 0));
                    panel_2.add(new JLabel("Latitude"));
                    panel_2.add(new JLabel("Longitude"));

                    JLabel label = new JLabel(" ");
                    panel_2.add(label);
                    latSpinner.setModel(new SpinnerNumberModel(0.0, -90.0, 90.0, 1.0));
                    panel_2.add(latSpinner);
                    lonSpinner.setModel(new SpinnerNumberModel(0.0, -180.0, 180.0, 1.0));
                    panel_2.add(lonSpinner);
                    JLabel lblThisCanBe = new JLabel("This can be used to find nearest receivers");
                    panel_1.add(lblThisCanBe);

                    latSpinner.setValue(settings.getLatitude());
                    lonSpinner.setValue(settings.getLongitude());

                    JButton btnLocate = new JButton("Locate", FontAwesome.ICO_LOCATION);
                    btnLocate.addActionListener(e -> {
                        if (JOptionPane.showOptionDialog(this,
                                "Do you want us to try to guess your location automatically?\n"
                                        + "If you choose yes, your IP address will be sent to %s.\n"
                                                .formatted(LocationServices.SERVICE_URL.getHost())
                                        + "The address will not be logged or stored in any way, and the result may not be accurate (although probably good enough for server listings)",
                                "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
                                null) == JOptionPane.YES_OPTION) {
                            ProgressDialog.show(this, "Guessing your location...", t -> {
                                try {
                                    Location location = LocationServices.locate();
                                    return Optional.of(location);
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                    return Optional.ofNullable((Location) null);
                                }
                            }, t -> {
                                t.ifPresentOrElse(location -> {
                                    latSpinner.setValue(location.lat());
                                    lonSpinner.setValue(location.lon());
                                }, () -> JOptionPane.showMessageDialog(this,
                                        "Couldn't guess your location, please try again later", "Error",
                                        JOptionPane.ERROR_MESSAGE));
                            });
                        }
                    });
                    panel_2.add(btnLocate);
                }
            }
        }

        JPanel panel = new JPanel();
        tabbedPane.addTab("Audio", FontAwesome.ICO_AUDIO, panel, null);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] { 0, 0 };
        gbl_panel.rowHeights = new int[] { 0, 0 };
        gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        panel.setLayout(gbl_panel);

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "Recording", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 0;
        panel.add(panel_1, gbc_panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));

        JLabel lblFfmpegPath = new JLabel("ffmpeg path");
        panel_1.add(lblFfmpegPath);

        JPanel panel_2 = new JPanel();
        panel_2.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel_1.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        ffmpegPath = new JTextField(settings.getFfmpegPath());
        panel_2.add(ffmpegPath);
        ffmpegPath.setAlignmentX(Component.LEFT_ALIGNMENT);
        ffmpegPath.setColumns(10);

        panel_2.add(new JLabel(" "));

        JButton btnFFmpegCheck = new JButton("Check");
        btnFFmpegCheck.addActionListener(e -> {
            boolean available = new FFMpeg(ffmpegPath.getText()).isAvailable();
            btnFFmpegCheck.setEnabled(!available);
            btnFFmpegCheck.setText(available ? "OK" : "Error!");
        });
        ffmpegPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                update(btnFFmpegCheck);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update(btnFFmpegCheck);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(btnFFmpegCheck);
            }

            private void update(JButton btnFFmpegCheck) {
                btnFFmpegCheck.setEnabled(true);
                btnFFmpegCheck.setText("Check");
            }
        });
        panel_2.add(btnFFmpegCheck);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(e -> {

                    settings.setAutoDownloadPublicReceivers(autoDownloadCheck.isSelected());
                    settings.setAutoRefreshPrivateReceivers(autoRefreshCheck.isSelected());
                    settings.setMaxNetworkWorkers((int) networkWorkers.getValue());

                    settings.setLatitude((double) latSpinner.getValue());
                    settings.setLongitude((double) lonSpinner.getValue());

                    settings.setFfmpegPath(ffmpegPath.getText());

                    dispose();
                });
                saveButton.setActionCommand("Save");
                buttonPane.add(saveButton);
                getRootPane().setDefaultButton(saveButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(e -> dispose());
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        pack();
        setLocationRelativeTo(parent);
    }

    public void setSelectedIndex(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    private void showBandplanChooser(ApplicationSettings settings, String extensionName, String extension,
            String genericErrorMessage, BandplanReaderFactory<?> factory, Charset charset) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load band plan");
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter(extensionName, extension));

        if (chooser.showDialog(this, "Load") == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try (BandplanReader reader = factory.create(new FileReader(selected, charset))) {
                if (reader instanceof SDRPPBandplanReader sdrpp) {
                    while (true) {
                        if (JOptionPane.showOptionDialog(this,
                                "Do you want to load a custom color map for this SDR++ band plan?\n"
                                        + "If your band plan does not come with one and you don't know what this means, press \"No\"",
                                "Custom color map", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
                                0) == JOptionPane.YES_OPTION) {
                            JFileChooser mapChooser = new JFileChooser();
                            mapChooser.setDialogTitle("Loading SDR++ color map");
                            if (mapChooser.showDialog(this, "Load") == JFileChooser.APPROVE_OPTION) {
                                try {
                                    File selectedMap = mapChooser.getSelectedFile();
                                    String fs = Files.readString(selectedMap.toPath()).trim();
                                    if (fs.endsWith(",")) fs = fs.substring(0, fs.length() - 1);
                                    String json = String.format("{ %s }", fs);
                                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                                    Map<String, Color> colors = new HashMap<>();
                                    obj.asMap().forEach((key, val) -> {
                                        try {
                                            String v = val.getAsString();
                                            if (v.length() == 9 && v.startsWith("#"))
                                                v = v.substring(0, v.length() - 2);
                                            colors.put(key, Color.decode(v));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    sdrpp.setColors(colors);
                                    break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(this, "Failed to load SDR++ color map", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else
                            break;
                    }
                }
                List<SerializedBandplan> bps = new ArrayList<>(settings.getLoadedBandplans());
                bps.add(reader.readBandplan(selected.getName()).serialize());
                settings.setLoadedBandplans(bps);
                bandsModel.removeAllElements();
                settings.getLoadedBandplans().forEach(i -> bandsModel.addElement(i));
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, genericErrorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
