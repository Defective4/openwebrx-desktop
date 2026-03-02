package io.github.defective4.sdr.owrxdesktop.ui.event;

import io.github.defective4.sdr.owrxclient.model.ReceiverMode;

public interface UserInteractionListener {
    void modeChanged(ReceiverMode primary, ReceiverMode underlying);

    void tuned(int offset);
}
