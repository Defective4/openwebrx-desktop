package io.github.defective4.sdr.owrxdesktop;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;

import io.github.defective4.sdr.owrxdesktop.application.UserStorage;
import io.github.defective4.sdr.owrxdesktop.ui.ApplicationWindow;

public class Main {
    public static void main(String[] args) {
        try {
            UserStorage storage = new UserStorage();
            FlatLaf.setUseNativeWindowDecorations(false);
            UIManager.setLookAndFeel(storage.getApplicationSettings().getTheme().getLafClass().getName());

            ApplicationWindow window = new ApplicationWindow(storage);
            window.setVisible(true);
            window.getPresence().updatePresence();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
