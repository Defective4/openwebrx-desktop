package io.github.defective4.sdr.owrxdesktop.ui.text;

import java.awt.Font;
import java.awt.Insets;
import java.io.InputStream;

import javax.swing.JButton;

public class FontAwesome {

    private static final Font FA_REGULAR;

    static {
        try (InputStream in = FontAwesome.class.getResourceAsStream("/font/fa-regular.otf")) {
            FA_REGULAR = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private FontAwesome() {}

    public static void setFontAwesomeFont(JButton cpt) {
        Font ft = FA_REGULAR.deriveFont(cpt.getFont().getSize2D());
        cpt.setMargin(new Insets(4, 4, 4, 4));
        cpt.setFont(ft);
    }
}
