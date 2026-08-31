package io.github.defective4.sdr.owrxdesktop.ui.component.demodulation;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import io.github.defective4.sdr.owrxclient.model.metadata.DRMMetadata;
import io.github.defective4.sdr.owrxdesktop.ui.component.JLED;

public class DRMPanel extends JPanel {
    private static final String[] INTERLEAVE = new String[] { "Short", "Long" };
    private static final String[] QAM = new String[] { "4-QAM", "16-QAM", "64-QAM" };
    private JLED jldAudio;
    private JLED jldData;
    private JLED jldFac;
    private JLED jldFrame;
    private JLED jldGuide;
    private JLED jldIo;
    private JLED jldJournaline;
    private JLED jldMsc;
    private JLED jldSdc;
    private JLED jldSlideshow;
    private JLED jldTime;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[][] { { "IF Level", null, "SNR", null }, { "Mode", null, "Bandwidth", null },
                    { "SDC", null, "MSC", null }, { "Interleave", null, "Protection", null }, },
            new String[] { "", "", "", "" }) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

    };

    private JTable table;

    public DRMPanel() {
        setBorder(new EmptyBorder(8, 16, 8, 16));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        setLayout(gridBagLayout);

        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setHgap(10);
        flowLayout.setAlignment(FlowLayout.LEFT);
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        add(panel, gbc_panel);

        jldIo = new JLED((String) null);
        jldIo.setText("IO");
        panel.add(jldIo);

        jldTime = new JLED((String) null);
        jldTime.setText("Time");
        panel.add(jldTime);

        jldFrame = new JLED((String) null);
        jldFrame.setText("Frame");
        panel.add(jldFrame);

        jldFac = new JLED((String) null);
        jldFac.setText("FAC");
        panel.add(jldFac);

        jldSdc = new JLED((String) null);
        jldSdc.setText("SDC");
        panel.add(jldSdc);

        jldMsc = new JLED((String) null);
        jldMsc.setText("MSC");
        panel.add(jldMsc);

        JSeparator separator = new JSeparator();
        GridBagConstraints gbc_separator = new GridBagConstraints();
        gbc_separator.fill = GridBagConstraints.HORIZONTAL;
        gbc_separator.insets = new Insets(0, 0, 5, 0);
        gbc_separator.gridx = 0;
        gbc_separator.gridy = 1;
        add(separator, gbc_separator);

        table = new JTable();
        JTableHeader header = table.getTableHeader();
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column % 2 == 0) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD));

                }
                return label;
            }
        });

        table.setModel(model);
        GridBagConstraints gbc_table = new GridBagConstraints();
        gbc_table.insets = new Insets(0, 0, 5, 0);
        gbc_table.fill = GridBagConstraints.BOTH;
        gbc_table.gridx = 0;
        gbc_table.gridy = 2;
        add(table, gbc_table);

        JSeparator separator_1 = new JSeparator();
        GridBagConstraints gbc_separator_1 = new GridBagConstraints();
        gbc_separator_1.insets = new Insets(0, 0, 5, 0);
        gbc_separator_1.fill = GridBagConstraints.HORIZONTAL;
        gbc_separator_1.gridx = 0;
        gbc_separator_1.gridy = 3;
        add(separator_1, gbc_separator_1);

        JPanel panel_1 = new JPanel();
        FlowLayout flowLayout_1 = (FlowLayout) panel_1.getLayout();
        flowLayout_1.setHgap(10);
        flowLayout_1.setAlignment(FlowLayout.LEFT);
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 4;
        add(panel_1, gbc_panel_1);

        jldAudio = new JLED((String) null);
        jldAudio.setText("Audio");
        panel_1.add(jldAudio);

        jldData = new JLED((String) null);
        jldData.setText("Data");
        panel_1.add(jldData);

        jldGuide = new JLED((String) null);
        jldGuide.setText("Guide");
        panel_1.add(jldGuide);

        jldJournaline = new JLED((String) null);
        jldJournaline.setText("Journaline");
        panel_1.add(jldJournaline);

        jldSlideshow = new JLED((String) null);
        jldSlideshow.setText("Slideshow");
        panel_1.add(jldSlideshow);
    }

    public void update(DRMMetadata data) {
        data.getStatus().ifPresent(status -> {
            jldIo.setState(status.io());
            jldTime.setState(status.time());
            jldFrame.setState(status.frame());
            jldFac.setState(status.fac());
            jldSdc.setState(status.sdc());
            jldMsc.setState(status.msc());
        });

        data.getMedia().ifPresent(media -> {
            jldGuide.setState(media.programGuide());
            jldJournaline.setState(media.journaline());
            jldSlideshow.setState(media.slideshow());
        });

        data.getServices().ifPresent(svc -> {
            jldAudio.setState(svc.audio() > 0);
            jldData.setState(svc.data() > 0);
        });

        data.getSignal().ifPresent(levels -> {
            model.setValueAt("%s dB".formatted(levels.ifDb()), 0, 1);
            model.setValueAt("%s dB".formatted(levels.snr()), 0, 3);
        });

        data.getCoding().ifPresent(coding -> {
            boolean a = coding.protA() > 0;
            boolean b = coding.protB() > 0;
            String val;
            if (a && b)
                val = "A + B";
            else if (a)
                val = "A";
            else if (b)
                val = "B";
            else
                val = "";
            model.setValueAt(val, 3, 3);
            model.setValueAt(QAM[coding.sdc()], 2, 1);
            model.setValueAt(QAM[coding.msc()], 2, 3);
        });

        data.getMode().ifPresent(mode -> {
            model.setValueAt("%s KHz".formatted(mode.bandwidthKhz()), 1, 3);
            model.setValueAt(INTERLEAVE[mode.interleaver()], 3, 1);
            model.setValueAt((char) ('A' + mode.robustness()), 1, 1);
        });
    }
}
