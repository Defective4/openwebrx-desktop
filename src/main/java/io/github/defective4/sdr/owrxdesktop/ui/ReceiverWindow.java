package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import io.github.defective4.sdr.owrxdesktop.ui.component.WaterfallPanel;

public class ReceiverWindow extends JFrame {

    private final WaterfallPanel panel;

    public ReceiverWindow() {
        setBounds(100, 100, 768, 468);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.75);
        getContentPane().add(splitPane);

        JPanel controlPanel = new JPanel();
        splitPane.setRightComponent(controlPanel);

        panel = new WaterfallPanel(Arrays.stream(
                "0x000020, 0x000030, 0x000050, 0x000091, 0x1E90FF, 0xFFFFFF, 0xFFFF00, 0xFE6D16, 0xFF0000, 0xC60000, 0x9F0000, 0x750000, 0x4A0000"
                        .replace("0x", "#").split(", "))
                .map(Color::decode).toArray(Color[]::new));
        splitPane.setLeftComponent(panel);
    }

    public WaterfallPanel getPanel() {
        return panel;
    }

}
