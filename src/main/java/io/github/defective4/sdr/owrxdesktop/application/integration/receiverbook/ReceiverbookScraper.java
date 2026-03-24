package io.github.defective4.sdr.owrxdesktop.application.integration.receiverbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import io.github.defective4.sdr.owrxdesktop.application.integration.PublicReceiver;
import io.github.defective4.sdr.owrxdesktop.application.integration.ReceiverScraper;

public class ReceiverbookScraper implements ReceiverScraper {
    private static final Pattern JSON_PATTERN = Pattern.compile("^\\s*var\\s+receivers\\s+=\\s+(\\[.*\\]);\\s*$");
    private static final URL RXBOOK_URL;
    static {
        try {
            RXBOOK_URL = URI.create("http://proxy.raspberry.local/map").toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Gson gson = new Gson();

    private final List<PublicReceiver> receivers = new ArrayList<>();

    public ReceiverbookScraper() {
        receivers.add(new PublicReceiver("Defective's Radio", "Test", "http://radio.raspberry.local", "OpenWebRX"));
    }

    @Override
    public List<PublicReceiver> getReceivers() {
        return Collections.unmodifiableList(receivers);
    }

    @Override
    public boolean hasScraped() {
        return !receivers.isEmpty();
    }

    @Override
    public void scrapeReceivers() throws IOException {
        receivers.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(RXBOOK_URL.openStream()));
                PrintWriter pw = new PrintWriter("/tmp/test.json")) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                Matcher matcher = JSON_PATTERN.matcher(line);
                if (matcher.matches()) {
                    JsonArray array = JsonParser.parseString(matcher.group(1)).getAsJsonArray();
                    array.forEach(element -> {
                        try {
                            ReceiverbookStation station = gson.fromJson(element, ReceiverbookStation.class);
                            if (station != null)
                                for (PublicReceiver rx : station.receivers()) if ("OpenWebRX".equals(rx.type())) {
                                    receivers.add(rx);
                                }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                }
            }
        }
    }

    @Override
    public List<PublicReceiver> searchReceivers(String phrase, int limit) {
        return receivers.stream().filter(rx -> rx.label().toLowerCase().contains(phrase.toLowerCase())).limit(limit)
                .toList();
    }
}
