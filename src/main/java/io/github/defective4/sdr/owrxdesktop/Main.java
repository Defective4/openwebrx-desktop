package io.github.defective4.sdr.owrxdesktop;

import java.net.URI;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

            ReceiverUserSettings settings = new ReceiverUserSettings();

            RadioReceiver radio = new RadioReceiver(
                    URI.create(args.length > 0 ? args[0] : "wss://radio.raspberry.local/ws/"), settings);
            radio.setVisible(true);

            radio.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
