package io.github.defective4.sdr.owrxdesktop.ui.rx;

import io.github.defective4.sdr.owrxclient.model.ReceiverGPS;

public record StatusResponse(Receiver receiver, String version) {
    public record Receiver(String name, String admin, ReceiverGPS gps, String location) {
    }
}
