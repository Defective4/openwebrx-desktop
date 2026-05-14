package io.github.defective4.sdr.owrxdesktop.application.integration.listings.receiverbook;

import io.github.defective4.sdr.owrxdesktop.application.integration.listings.PublicReceiver;
import io.github.defective4.sdr.owrxdesktop.application.integration.listings.PublicReceiverLocation;

public record ReceiverbookStation(String label, PublicReceiver[] receivers, PublicReceiverLocation location) {

}
