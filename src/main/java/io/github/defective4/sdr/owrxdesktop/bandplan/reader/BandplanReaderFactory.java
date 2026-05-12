package io.github.defective4.sdr.owrxdesktop.bandplan.reader;

import java.io.Reader;

public interface BandplanReaderFactory<T extends BandplanReader> {
    T create(Reader reader);
}
