package io.github.defective4.sdr.owrxdesktop.ui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import io.github.defective4.sdr.owrxdesktop.ui.component.FFTPanel;

public class ReceiverWindow extends JFrame {

    private final FFTPanel panel;

    public ReceiverWindow() {
        setBounds(100, 100, 768, 468);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.75);
        getContentPane().add(splitPane);

        JPanel controlPanel = new JPanel();
        splitPane.setRightComponent(controlPanel);

        panel = new FFTPanel();
        splitPane.setLeftComponent(panel);
    }

    public FFTPanel getPanel() {
        return panel;
    }

}
