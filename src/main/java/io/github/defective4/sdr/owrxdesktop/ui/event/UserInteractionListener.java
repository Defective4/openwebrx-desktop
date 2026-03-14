package io.github.defective4.sdr.owrxdesktop.ui.event;

import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxclient.model.ReceiverProfile;
import io.github.defective4.sdr.owrxdesktop.ui.BookmarksDialog.MergedLabel;

public interface UserInteractionListener {
    void bookmarkJumped(MergedLabel label);

    void freeTune(int freq);

    void modeChanged(ReceiverMode primary, ReceiverMode underlying);

    void muteToggled(boolean muted);

    void profileChanged(ReceiverProfile profile);

    void scopeChanged(int scopeLower, int scopeUpper);

    void settingsChanged();

    void tuned(int offset);

    void volumeChanged(float value);
}
