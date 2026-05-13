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

    public static final String FA_BOOKMARK = "\uf02e";
    public static final String FA_BROADCAST = "\uf519";
    public static final String FA_CHART = "\uf201";
    public static final String FA_COG = "\uf013";
    public static final String FA_DELETE = "\uf2ed";
    public static final String FA_EDIT = "\uf304";
    public static final String FA_GLOBE = "\uf0ac";
    public static final String FA_MINUS = "\uf068";
    public static final String FA_NETWORK = "\uf6ff";
    public static final String FA_PALETTE = "\uf53f";
    public static final String FA_PLUS = "\uf067";
    public static final Font FA_REGULAR;
    public static final String FA_SETTINGS = "\uf1de";
    public static final String FA_SIGN_IN = "\uf2f6";
    public static final String FA_SIGN_OUT = "\uf2f5";
    public static final String FA_SYNC = "\uf021";
    public static final String FA_TAGS = "\uf02c";
    public static final String FA_TASKS = "\uf0ae";
    public static final String FA_USER = "\uf007";
    public static final Icon ICO_NETWORK, ICO_TAGS, ICO_TASKS, ICO_AUDIO, ICO_BROADCAST, ICO_CHART, ICO_AREA_CHART,
            ICO_PALETTE, ICO_SIGN_IN, ICO_SYNC, ICO_PLUS, ICO_USER, ICO_GLOBE, ICO_SIGN_OUT, ICO_SETTINGS, ICO_EDIT,
            ICO_DELETE, ICO_COG, ICO_BOOKMARK;

    static {
        try (InputStream in = FontAwesome.class.getResourceAsStream("/font/fa-regular.otf")) {
            FA_REGULAR = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        ICO_NETWORK = createFAImage(FA_NETWORK);
        ICO_TAGS = createFAImage(FA_TAGS);
        ICO_TASKS = createFAImage(FA_TASKS);
        ICO_AUDIO = createFAImage(FA_AUDIO);
        ICO_BROADCAST = createFAImage(FA_BROADCAST);
        ICO_CHART = createFAImage(FA_CHART);
        ICO_AREA_CHART = createFAImage(FA_AREA_CHART);
        ICO_PALETTE = createFAImage(FA_PALETTE);
        ICO_SIGN_IN = createFAImage(FA_SIGN_IN);
        ICO_SYNC = createFAImage(FA_SYNC);
        ICO_PLUS = createFAImage(FA_PLUS);
        ICO_USER = createFAImage(FA_USER);
        ICO_GLOBE = createFAImage(FA_GLOBE);
        ICO_SIGN_OUT = createFAImage(FA_SIGN_OUT);
        ICO_SETTINGS = createFAImage(FA_SETTINGS);
        ICO_EDIT = createFAImage(FA_EDIT);
        ICO_DELETE = createFAImage(FA_DELETE);
        ICO_COG = createFAImage(FA_COG);
        ICO_BOOKMARK = createFAImage(FA_BOOKMARK);
    }

    private FontAwesome() {}

    public static void setFontAwesomeFont(JButton cpt) {
        Font ft = FA_REGULAR.deriveFont(cpt.getFont().getSize2D());
        cpt.setMargin(new Insets(4, 4, 4, 4));
        cpt.setFont(ft);
    }

    private static ImageIcon createFAImage(String icon) {
        JLabel label = new JLabel();
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(FA_REGULAR.deriveFont(13f));
        g2.setColor(label.getForeground());
        g2.drawString(icon, 0, 13);
        return new ImageIcon(image);
    }
}
