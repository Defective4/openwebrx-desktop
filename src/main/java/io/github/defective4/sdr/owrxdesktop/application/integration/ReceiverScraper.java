package io.github.defective4.sdr.owrxdesktop.application.integration;

import java.io.IOException;
import java.util.List;

public interface ReceiverScraper {
    public List<PublicReceiverEntry> getReceivers();

    public boolean hasScraped();

    public void scrapeReceivers() throws IOException;

    public List<PublicReceiverEntry> searchReceivers(String phrase, int limit, SearchSort sort);
}
