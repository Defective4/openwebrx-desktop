package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import io.github.defective4.sdr.owrxdesktop.ui.text.FontAwesome;

public class JCloseableTab extends JPanel {
    public JCloseableTab(String text, JTabbedPane pane) {
        FlowLayout flowLayout = (FlowLayout) getLayout();
        flowLayout.setVgap(0);
        flowLayout.setHgap(0);
        flowLayout.setAlignment(FlowLayout.LEFT);

        JLabel lblTex = new JLabel(text);
        add(lblTex);

        JButton btnX = new JButton(FontAwesome.FA_X);
        FontAwesome.setFontAwesomeFont(btnX);
        setBackground(new Color(0, 0, 0, 0));
        btnX.setFont(btnX.getFont().deriveFont(8f));
        btnX.addActionListener(e -> {
            for (int i = 0; i < pane.getTabCount(); i++) {
                if (pane.getTabComponentAt(i) == this) pane.remove(i);
            }
        });
        add(new JLabel("  "));
        add(btnX);
    }
}
