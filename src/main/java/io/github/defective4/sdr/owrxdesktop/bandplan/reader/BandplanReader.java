package io.github.defective4.sdr.owrxdesktop.bandplan.reader;

import java.io.IOException;
import java.io.Reader;

import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public abstract class BandplanReader implements AutoCloseable {

    protected final Reader reader;

    public BandplanReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public abstract Bandplan readBandplan(String name) throws Exception;

}
