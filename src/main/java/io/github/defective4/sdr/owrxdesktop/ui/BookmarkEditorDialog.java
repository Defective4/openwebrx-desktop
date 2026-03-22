package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxdesktop.ui.component.JFrequencySpinner;
import io.github.defective4.sdr.owrxdesktop.ui.component.UserBookmark;

public class BookmarkEditorDialog extends JDialog {

    private final JComboBox<ReceiverMode> analogBox = new JComboBox<>();
    private UserBookmark bookmark = null;
    private final JComboBox<ReceiverMode> digitalBox = new JComboBox<>();
    private final JTextField nameField = new JTextField();

    private final JFrequencySpinner spinner = new JFrequencySpinner();

    private BookmarkEditorDialog(Window parent, Collection<ReceiverMode> modes, List<ReceiverMode> digital,
            int centerFrequency, ReceiverMode initialMode, ReceiverMode secondaryMode, String profile) {
        super(parent);
        setTitle("Bookmark editor");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel contentPanel = new JPanel();
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[] { 0, 0 };
        gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblName = new JLabel("Name");
            GridBagConstraints gbc_lblName = new GridBagConstraints();
            gbc_lblName.anchor = GridBagConstraints.WEST;
            gbc_lblName.insets = new Insets(0, 0, 5, 0);
            gbc_lblName.gridx = 0;
            gbc_lblName.gridy = 0;
            contentPanel.add(lblName, gbc_lblName);
        }
        {
            GridBagConstraints gbc_textField = new GridBagConstraints();
            gbc_textField.fill = GridBagConstraints.HORIZONTAL;
            gbc_textField.insets = new Insets(0, 0, 5, 0);
            gbc_textField.gridx = 0;
            gbc_textField.gridy = 1;
            contentPanel.add(nameField, gbc_textField);
            nameField.setColumns(10);
        }
        {
            JLabel lblFrequency = new JLabel("Frequency");
            GridBagConstraints gbc_lblFrequency = new GridBagConstraints();
            gbc_lblFrequency.insets = new Insets(0, 0, 5, 0);
            gbc_lblFrequency.anchor = GridBagConstraints.WEST;
            gbc_lblFrequency.gridx = 0;
            gbc_lblFrequency.gridy = 2;
            contentPanel.add(lblFrequency, gbc_lblFrequency);
        }
        {
            spinner.setValue(centerFrequency);

            GridBagConstraints gbc_spinner = new GridBagConstraints();
            gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
            gbc_spinner.insets = new Insets(0, 0, 5, 0);
            gbc_spinner.gridx = 0;
            gbc_spinner.gridy = 3;
            contentPanel.add(spinner, gbc_spinner);
        }
        {
            JLabel lblPrimaryMode = new JLabel("Mode");
            GridBagConstraints gbc_lblPrimaryMode = new GridBagConstraints();
            gbc_lblPrimaryMode.insets = new Insets(0, 0, 5, 0);
            gbc_lblPrimaryMode.anchor = GridBagConstraints.WEST;
            gbc_lblPrimaryMode.gridx = 0;
            gbc_lblPrimaryMode.gridy = 4;
            contentPanel.add(lblPrimaryMode, gbc_lblPrimaryMode);
        }
        {
            modes.stream().filter(Objects::nonNull).forEach(analogBox::addItem);
            analogBox.setSelectedItem(initialMode);
            GridBagConstraints gbc_comboBox = new GridBagConstraints();
            gbc_comboBox.insets = new Insets(0, 0, 5, 0);
            gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_comboBox.gridx = 0;
            gbc_comboBox.gridy = 5;
            contentPanel.add(analogBox, gbc_comboBox);
        }
        {
            JLabel lblSecondaryMode = new JLabel("Secondary mode");
            GridBagConstraints gbc_lblSecondaryMode = new GridBagConstraints();
            gbc_lblSecondaryMode.insets = new Insets(0, 0, 5, 0);
            gbc_lblSecondaryMode.anchor = GridBagConstraints.WEST;
            gbc_lblSecondaryMode.gridx = 0;
            gbc_lblSecondaryMode.gridy = 6;
            contentPanel.add(lblSecondaryMode, gbc_lblSecondaryMode);
        }
        {
            digital.stream().forEach(digitalBox::addItem);
            if (secondaryMode != null) digitalBox.setSelectedItem(secondaryMode);
            GridBagConstraints gbc_comboBox = new GridBagConstraints();
            gbc_comboBox.insets = new Insets(0, 0, 5, 0);
            gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_comboBox.gridx = 0;
            gbc_comboBox.gridy = 7;
            contentPanel.add(digitalBox, gbc_comboBox);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Save");
                okButton.setEnabled(false);
                okButton.addActionListener(e -> {
                    bookmark = new UserBookmark(nameField.getText(), (int) spinner.getValue(),
                            (ReceiverMode) analogBox.getSelectedItem(),
                            Optional.ofNullable((ReceiverMode) digitalBox.getSelectedItem()), profile);
                    dispose();
                });
                okButton.setActionCommand("Save");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);

                nameField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        okButton.setEnabled(!nameField.getText().isBlank());
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        okButton.setEnabled(!nameField.getText().isBlank());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        okButton.setEnabled(!nameField.getText().isBlank());
                    }
                });
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

    public static Optional<UserBookmark> show(Window parent, Collection<ReceiverMode> modes, List<ReceiverMode> digital,
            int centerFrequency, ReceiverMode initialMode, ReceiverMode secondaryMode, String profile) {
        BookmarkEditorDialog dialog = new BookmarkEditorDialog(parent, modes, digital, centerFrequency, initialMode,
                secondaryMode, profile);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.bookmark);
    }

}
