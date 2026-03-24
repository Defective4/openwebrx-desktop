package io.github.defective4.sdr.owrxdesktop.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserStorage {
    private final List<ReceiverEntry> userEntries = new ArrayList<>();

    public boolean addEntry(ReceiverEntry e) {
        return userEntries.add(e);
    }

    public List<ReceiverEntry> getUserEntries() {
        return Collections.unmodifiableList(userEntries);
    }

}
