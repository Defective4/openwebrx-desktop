package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import io.github.defective4.sdr.owrxdesktop.application.ReceiverEntry;

public class ReceiverEntryContainer extends JPanel {

    private final GridBagLayout gbl_rxContainer = new GridBagLayout();
    private final BufferedImage rxPlaceholder;

    public ReceiverEntryContainer() {
        try (InputStream is = getClass().getResourceAsStream("/rx-null.png")) {
            rxPlaceholder = ImageIO.read(is);
        } catch (IOException e1) {
            throw new IllegalStateException(e1);
        }
        gbl_rxContainer.columnWidths = new int[] { 0, 0 };
        gbl_rxContainer.rowHeights = new int[] { 0, 0, 0, 0 };
        gbl_rxContainer.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
        gbl_rxContainer.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
        setLayout(gbl_rxContainer);
    }

    public ReceiverEntryComponent addEntry(ReceiverEntry entry,
            Function<ReceiverEntryComponent, Collection<JButton>> buttonFunction) {
        ReceiverEntryComponent component = new ReceiverEntryComponent(entry, rxPlaceholder, buttonFunction);
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.WEST;
        gbc_panel.fill = GridBagConstraints.VERTICAL;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = getComponentCount();
        double[] weight = new double[gbc_panel.gridy + 1];
        weight[weight.length - 1] = Double.MIN_VALUE;
        gbl_rxContainer.rowWeights = weight;
        add(component, gbc_panel);
        invalidate();
        return component;
    }

    public List<ReceiverEntryComponent> getAllReceiverComponents() {
        List<ReceiverEntryComponent> cpts = new ArrayList<>();
        for (Component cpt : getComponents()) if (cpt instanceof ReceiverEntryComponent rxEntry) cpts.add(rxEntry);
        return Collections.unmodifiableList(cpts);
    }
}
