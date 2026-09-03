package io.github.defective4.sdr.owrxdesktop.ui.settings;

import javax.swing.LookAndFeel;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public enum UITheme {
    FLAT_LAF_DARCULA(FlatDarculaLaf.class, "Darcula (Flat LaF)"),
    FLAT_LAF_INTELLIJ(FlatIntelliJLaf.class, "Intellij (Flat LaF)"),
    FLAT_LAF_MAC_DARK(FlatMacDarkLaf.class, "Mac Dark (Flat LaF)"),
    FLAT_LAF_MAC_LIGHT(FlatMacLightLaf.class, "Mac Light (Flat LaF)"), METAL(MetalLookAndFeel.class, "Metal"),
    NIMBUS(NimbusLookAndFeel.class, "Nimbus"), WINDOWS("com.sun.java.swing.plaf.windows.WindowsLookAndFeel", "Windows");

    private final Class<? extends LookAndFeel> lafClass;
    private final String name;

    UITheme(Class<? extends LookAndFeel> lafClass, String name) {
        this.lafClass = lafClass;
        this.name = name;
    }

    private UITheme(String className, String name) {
        this(resolveClass(className), name);
    }

    public boolean isValid() {
        return lafClass != null;
    }

    public Class<? extends LookAndFeel> getLafClass() {
        if (!isValid()) return FLAT_LAF_DARCULA.getLafClass();
        return lafClass;
    }

    @Override
    public String toString() {
        return name;
    }

    private static Class<? extends LookAndFeel> resolveClass(String className) {
        Class<? extends LookAndFeel> c;
        try {
            c = (Class<? extends LookAndFeel>) Class.forName(className);
        } catch (Exception e) {
            c = null;
        }
        return c;
    }

}
