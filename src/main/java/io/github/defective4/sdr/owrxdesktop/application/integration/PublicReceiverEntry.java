package io.github.defective4.sdr.owrxdesktop.application.integration;

import io.github.defective4.sdr.owrxclient.model.ReceiverGPS;

public record PublicReceiverEntry(String label, String version, String url, String type,
        ReceiverGPS location) {

}
