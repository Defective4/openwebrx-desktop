package io.github.defective4.sdr.owrxdesktop.application;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;

public class UserStorage {
    private final ReceiverUserSettings defaultSettings = new ReceiverUserSettings();
    private final List<ReceiverEntry> userEntries = new ArrayList<>();

    public UserStorage() {
        try {
            userEntries.add(new ReceiverEntry("http://radio.raspberry.local", defaultSettings.clone()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public boolean addEntry(ReceiverEntry e) {
        return userEntries.add(e);
    }

    public ReceiverUserSettings getDefaultSettings() {
        return defaultSettings;
    }

    public List<ReceiverEntry> getUserEntries() {
        return Collections.unmodifiableList(userEntries);
    }

    public void removeEntry(ReceiverEntry entry) {
        userEntries.remove(entry);
    }

}
