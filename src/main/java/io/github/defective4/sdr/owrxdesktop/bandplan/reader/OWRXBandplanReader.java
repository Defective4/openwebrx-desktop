package io.github.defective4.sdr.owrxdesktop.bandplan.reader;

import java.awt.Color;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import io.github.defective4.sdr.owrxdesktop.application.util.ColorEncoder;
import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public class OWRXBandplanReader extends BandplanReader {

    public static record OWRXBand(String name, @SerializedName("lower_bound") long lower,
            @SerializedName("upper_bound") long upper, String[] tags) {
    }

    public static final BandplanReaderFactory<OWRXBandplanReader> FACTORY = OWRXBandplanReader::new;

    private final static Map<String, Color> COLORS;

    static {
        COLORS = new HashMap<>();
        Bandplan.COLORS.forEach((s, c) -> COLORS.put(s, Color.decode(c)));
    }

    public OWRXBandplanReader(Reader reader) {
        super(reader);
    }

    @Override
    public Bandplan readBandplan(String file) throws Exception {
        OWRXBand[] oBands = new Gson().fromJson(reader, OWRXBand[].class);
        if (oBands == null) throw new IllegalArgumentException();
        List<Band> bands = new ArrayList<>();
        for (OWRXBand band : oBands) {
            String name = band.name();
            long lower = band.lower();
            long upper = band.upper();
            String[] tags = band.tags();

            if (name == null || lower > Integer.MAX_VALUE || upper > Integer.MAX_VALUE) continue;
            String colorTag = null;
            if (tags != null && tags.length > 0) colorTag = tags[0];
            Color color = colorTag != null && COLORS.containsKey(colorTag) ? COLORS.get(colorTag)
                    : COLORS.values().iterator().next();
            color = ColorEncoder.setColorAlpha(color, 255);

            bands.add(new Band((int) lower, (int) upper, color, name));
        }

        if (bands.isEmpty()) throw new IllegalStateException();

        return new Bandplan(bands, COLORS, "[OWRX] " + file);
    }

}
