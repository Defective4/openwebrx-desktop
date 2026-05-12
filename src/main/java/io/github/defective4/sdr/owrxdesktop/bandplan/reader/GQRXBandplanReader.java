package io.github.defective4.sdr.owrxdesktop.bandplan.reader;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public class GQRXBandplanReader extends BandplanReader {
    public static final BandplanReaderFactory<GQRXBandplanReader> FACTORY = GQRXBandplanReader::new;

    public GQRXBandplanReader(Reader reader) {
        super(reader);
    }

    @Override
    public Bandplan readBandplan(String name) throws IOException {
        List<Band> bands = new ArrayList<>();
        Map<String, Color> colors = new HashMap<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] parts = line.split(", ");
                if (parts.length < 6) continue;
                try {
                    long start = Long.parseLong(parts[0]);
                    long end = Long.parseLong(parts[1]);
                    if (start > Integer.MAX_VALUE || end > Integer.MAX_VALUE) continue;
                    String color = parts[4];
                    String label = String.join(", ", Arrays.copyOfRange(parts, 5, parts.length));
                    Color decoded = Color.decode(color);
                    Color corrected = new Color(decoded.getRed(), decoded.getGreen(), decoded.getBlue(), 100);
                    colors.put(color, corrected);
                    bands.add(new Band((int) start, (int) end, corrected, label));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        try {
            if (bands.isEmpty()) throw new IllegalStateException();
            return new Bandplan(bands, colors, "[GQRX] " + name);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
