package io.github.defective4.sdr.owrxdesktop.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;

public class UserStorage {
    private final ReceiverUserSettings defaultSettings = new ReceiverUserSettings();
    private final List<ReceiverEntry> userEntries = new ArrayList<>();

    public boolean addEntry(ReceiverEntry e) {
        return userEntries.add(e);
    }

    public ReceiverUserSettings getDefaultSettings() {
        return defaultSettings;
    }

    public List<ReceiverEntry> getUserEntries() {
        return Collections.unmodifiableList(userEntries);
    }

}
