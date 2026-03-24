package io.github.defective4.sdr.owrxdesktop.application.integration.receiverbook;

import io.github.defective4.sdr.owrxdesktop.application.integration.PublicReceiver;
import io.github.defective4.sdr.owrxdesktop.application.integration.PublicReceiverLocation;

public record ReceiverbookStation(String label, PublicReceiver[] receivers, PublicReceiverLocation location) {

}
