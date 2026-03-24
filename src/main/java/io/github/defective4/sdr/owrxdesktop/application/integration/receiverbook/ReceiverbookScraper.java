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
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import io.github.defective4.sdr.owrxclient.model.ReceiverGPS;
import io.github.defective4.sdr.owrxdesktop.application.integration.PublicReceiver;
import io.github.defective4.sdr.owrxdesktop.application.integration.PublicReceiverEntry;
import io.github.defective4.sdr.owrxdesktop.application.integration.ReceiverScraper;
import io.github.defective4.sdr.owrxdesktop.application.integration.SearchSort;
import io.github.defective4.sdr.owrxdesktop.ui.ApplicationWindow;

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
    private final ApplicationWindow application;

    private final Gson gson = new Gson();

    private final List<PublicReceiverEntry> receivers = new ArrayList<>();

    public ReceiverbookScraper(ApplicationWindow application) {
        this.application = application;
//        receivers.add(new PublicReceiverEntry("Defective's Radio", "Test", "http://radio.raspberry.local", "OpenWebRX",
//                new ReceiverGPS(0, 0)));
    }

    @Override
    public List<PublicReceiverEntry> getReceivers() {
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
                                    receivers.add(new PublicReceiverEntry(rx.label(), rx.version(), rx.url(), rx.type(),
                                            new ReceiverGPS(station.location().coordinates()[1],
                                                    station.location().coordinates()[0])));
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
    public List<PublicReceiverEntry> searchReceivers(String phrase, int limit, SearchSort sort) {
        Stream<PublicReceiverEntry> stream = receivers.stream()
                .filter(rx -> rx.label().toLowerCase().contains(phrase.toLowerCase()));
        ReceiverGPS gps = application.getUserStorage().getApplicationSettings().getGPS();
        stream = switch (sort) {
            case ALPHABETICAL -> stream.sorted((o1, o2) -> o1.label().compareTo(o2.label()));
            case DISTANCE ->
                stream.sorted((o1, o2) -> (int) (distance(o1.location(), gps) - distance(o2.location(), gps)));
            default -> stream;
        };
        return stream.limit(limit).toList();
    }

    private static double distance(ReceiverGPS from, ReceiverGPS to) {
        double x = to.lon() - from.lon();
        double y = to.lat() - from.lat();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) * 100;
    }
}
