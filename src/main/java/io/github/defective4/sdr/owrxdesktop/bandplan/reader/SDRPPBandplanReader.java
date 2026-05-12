package io.github.defective4.sdr.owrxdesktop.bandplan.reader;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public class SDRPPBandplanReader extends BandplanReader {
    public static record SDRPPBand(String name, String type, long start, long end) {
    }

    public static record SDRPPBandplan(String name, @SerializedName("author_name") String author,
            List<SDRPPBand> bands) {
    }

    public static final BandplanReaderFactory<SDRPPBandplanReader> FACTORY = SDRPPBandplanReader::new;

    private static final Map<String, Color> DEFAULT_COLORS = Map.of("amateur", Color.red, "aviation", Color.green,
            "broadcast", Color.blue, "marine", Color.decode("#00FFFF"), "military", Color.decode("#FFFF00"));

    private Map<String, Color> colors = DEFAULT_COLORS;

    public SDRPPBandplanReader(Reader reader) {
        super(reader);
    }

    @Override
    public Bandplan readBandplan(String n) throws IOException {
        SDRPPBandplan plan = new Gson().fromJson(reader, SDRPPBandplan.class);
        String name = String.format("[SDR++] %s by %s", plan.name, plan.author() == null ? "Unknown" : plan.author());
        List<Band> band = new ArrayList<>();
        for (SDRPPBand b : plan.bands()) {
            if (b.name() == null || b.type() == null) continue;
            long start = b.start();
            long end = b.end();
            if (start > Integer.MAX_VALUE || end > Integer.MAX_VALUE) continue;
            band.add(new Band((int) start, (int) end, colors.getOrDefault(b.type(), Color.red), b.name()));
        }
        return new Bandplan(band, colors, name);
    }

    public void setColors(Map<String, Color> colors) {
        this.colors = colors;
    }

}
