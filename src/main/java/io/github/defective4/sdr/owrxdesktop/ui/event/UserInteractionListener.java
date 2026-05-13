package io.github.defective4.sdr.owrxdesktop.ui.event;

import java.io.File;
import java.io.IOException;

import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxclient.model.ReceiverProfile;
import io.github.defective4.sdr.owrxdesktop.ui.BookmarksDialog.MergedLabel;

public interface UserInteractionListener {
    void appExit() throws IOException;

    void bookmarkJumped(MergedLabel label);

    void freeTune(int freq);

    void modeChanged(ReceiverMode primary, ReceiverMode underlying);

    void muteToggled(boolean muted);

    void profileChanged(ReceiverProfile profile);

    boolean recordingToggled(File dir) throws IOException;

    void scopeChanged(int scopeLower, int scopeUpper);

    void settingsChanged();

    void tuned(int offset);

    void volumeChanged(float value);
}
