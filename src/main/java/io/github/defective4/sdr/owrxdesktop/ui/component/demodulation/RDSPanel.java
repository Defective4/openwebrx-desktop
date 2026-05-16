package io.github.defective4.sdr.owrxdesktop.ui.component.demodulation;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import io.github.defective4.sdr.owrxclient.model.metadata.RDSMetadata;

public class RDSPanel extends JPanel {
    private final JCheckBox musicCheck = new JCheckBox("Music");
    private final JTextField pi = new JTextField();
    private final JTextField pt = new JTextField();
    private final JTextField radiotext = new JTextField();
    private final JTextField station = new JTextField();
    private final JCheckBox taCheck = new JCheckBox("TA");
    private final JCheckBox tpCheck = new JCheckBox("TP");

    public RDSPanel() {

        ActionListener ls = e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            box.setSelected(!box.isSelected());
        };

        musicCheck.addActionListener(ls);
        taCheck.addActionListener(ls);
        tpCheck.addActionListener(ls);

        setLayout(new GridLayout(0, 2, 0, 0));
        JLabel lblStation = new JLabel("Station");
        add(lblStation);

        station.setEditable(false);
        add(station);
        station.setColumns(10);

        JLabel lblRadiotext = new JLabel("Radiotext");
        add(lblRadiotext);

        radiotext.setEditable(false);
        add(radiotext);
        radiotext.setColumns(10);

        JLabel lblProgramType = new JLabel("Program type");
        add(lblProgramType);

        pt.setEditable(false);
        add(pt);
        pt.setColumns(10);

        JLabel lblPi = new JLabel("PI");
        add(lblPi);

        pi.setEditable(false);
        add(pi);
        pi.setColumns(10);
        add(taCheck);

        add(tpCheck);

        add(musicCheck);
    }

    public void setData(RDSMetadata meta) {
        if (meta != null) {
            musicCheck.setSelected(meta.isMusic().orElse(false));
            taCheck.setSelected(meta.isTA().orElse(false));
            tpCheck.setSelected(meta.isTP().orElse(false));
            if (!station.getText().equals(meta.getStation().orElse(null)))
                station.setText(meta.getStation().orElse(""));
            pi.setText(meta.getPI().orElse(""));
            if (!radiotext.getText().equals(meta.getRadiotext().orElse(null)))
                radiotext.setText(meta.getRadiotext().orElse(""));
            pt.setText(meta.getProgramType().orElse(""));
        }
    }

}
