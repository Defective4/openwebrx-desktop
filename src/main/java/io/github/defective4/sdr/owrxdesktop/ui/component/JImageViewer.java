package io.github.defective4.sdr.owrxdesktop.ui.component;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JComponent;

public class JImageViewer extends JComponent {
    private BufferedImage image;

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.addRenderingHints(Map.of(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
                ));
        if (image != null) g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

}
