package io.github.defective4.sdr.owrxdesktop.ui.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class FontAwesome {

    public static final String FA_AREA_CHART = "\uf1fe";
    public static final String FA_AUDIO = "\uf028";

    public static final String FA_BROADCAST = "\uf519";
    public static final String FA_CHART = "\uf201";
    public static final String FA_COG = "\uf013";
    public static final String FA_GLOBE = "\uf0ac";
    public static final String FA_MINUS = "\uf068";
    public static final String FA_NETWORK = "\uf6ff";
    public static final String FA_PALETTE = "\uf53f";
    public static final String FA_PLUS = "\uf067";
    public static final Font FA_REGULAR;
    public static final String FA_SIGN_IN = "\uf2f6";
    public static final String FA_SYNC = "\uf021";
    public static final String FA_TAGS = "\uf02c";
    public static final String FA_TASKS = "\uf0ae";
    public static final String FA_USER = "\uf007";
    public static final Icon ICO_NETWORK, ICO_TAGS, ICO_TASKS, ICO_AUDIO, ICO_BROADCAST, ICO_CHART, ICO_AREA_CHART,
            ICO_PALETTE, ICO_SIGN_IN, ICO_SYNC, ICO_PLUS, ICO_USER, ICO_GLOBE;

    static {
        try (InputStream in = FontAwesome.class.getResourceAsStream("/font/fa-regular.otf")) {
            FA_REGULAR = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        ICO_NETWORK = new ImageIcon(createFAImage(FA_NETWORK));
        ICO_TAGS = new ImageIcon(createFAImage(FA_TAGS));
        ICO_TASKS = new ImageIcon(createFAImage(FA_TASKS));
        ICO_AUDIO = new ImageIcon(createFAImage(FA_AUDIO));
        ICO_BROADCAST = new ImageIcon(createFAImage(FA_BROADCAST));
        ICO_CHART = new ImageIcon(createFAImage(FA_CHART));
        ICO_AREA_CHART = new ImageIcon(createFAImage(FA_AREA_CHART));
        ICO_PALETTE = new ImageIcon(createFAImage(FA_PALETTE));
        ICO_SIGN_IN = new ImageIcon(createFAImage(FA_SIGN_IN));
        ICO_SYNC = new ImageIcon(createFAImage(FA_SYNC));
        ICO_PLUS = new ImageIcon(createFAImage(FA_PLUS));
        ICO_USER = new ImageIcon(createFAImage(FA_USER));
        ICO_GLOBE = new ImageIcon(createFAImage(FA_GLOBE));
    }

    private FontAwesome() {}

    public static void setFontAwesomeFont(JButton cpt) {
        Font ft = FA_REGULAR.deriveFont(cpt.getFont().getSize2D());
        cpt.setMargin(new Insets(4, 4, 4, 4));
        cpt.setFont(ft);
    }

    private static BufferedImage createFAImage(String icon) {
        JLabel label = new JLabel();
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(FA_REGULAR.deriveFont(13f));
        g2.setColor(label.getForeground());
        g2.drawString(icon, 0, 13);
        return image;
    }
}
