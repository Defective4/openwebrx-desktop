package io.github.defective4.sdr.owrxdesktop.ui.event;

import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxclient.model.ReceiverProfile;

public interface UserInteractionListener {
    void modeChanged(ReceiverMode primary, ReceiverMode underlying);

    void profileChanged(ReceiverProfile profile);

    void tuned(int offset);
}
