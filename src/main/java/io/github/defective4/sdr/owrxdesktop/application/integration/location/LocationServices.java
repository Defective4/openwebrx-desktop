package io.github.defective4.sdr.owrxdesktop.application.integration.location;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.google.gson.Gson;

public class LocationServices {
    public static final URL SERVICE_URL;

    static {
        Properties props = new Properties();
        try (Reader reader = new InputStreamReader(
                LocationServices.class.getResourceAsStream("/service/location.properties"), StandardCharsets.UTF_8)) {
            props.load(reader);
            SERVICE_URL = URI.create(props.getProperty("serviceurl")).toURL();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Location locate() throws IOException {
        try (Reader reader = new InputStreamReader(SERVICE_URL.openStream(), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, Location.class);
        }
    }
}
