package io.github.defective4.sdr.owrxdesktop.application;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;

public class ReceiverEntry {

    private static final Gson GSON = new Gson();

    private Exception queryException = null;
    private boolean querying = false;
    private StatusResponse receiverData;
    private BufferedImage receiverImage;
    private final String rootURL;
    private final ReceiverUserSettings settings;
    private final UUID uuid = UUID.randomUUID();

    public ReceiverEntry(String rootURL, ReceiverUserSettings settings) {
        if (!rootURL.endsWith("/")) rootURL += "/";
        this.rootURL = rootURL;
        this.settings = settings;
    }

    public Exception getQueryException() {
        return queryException;
    }

    public Optional<StatusResponse> getReceiverData() {
        return Optional.ofNullable(receiverData);
    }

    public Optional<BufferedImage> getReceiverImage() {
        return Optional.ofNullable(receiverImage);
    }

    public ReceiverUserSettings getSettings() {
        return settings;
    }

    public boolean isQuerying() {
        return querying;
    }

    public void query() {
        receiverData = null;
        receiverImage = null;
        querying = true;
        try {
            StatusResponse response;
            try (Reader reader = new InputStreamReader(URI.create(rootURL + "status.json").toURL().openStream())) {
                response = GSON.fromJson(reader, StatusResponse.class);
            }
            try (InputStream is = URI.create(rootURL + "static/gfx/openwebrx-avatar.png").toURL().openStream()) {
                receiverImage = ImageIO.read(is);
                receiverData = response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            queryException = e;
        } finally {
            querying = false;
        }
    }

    public void setQuerying() {
        querying = true;
        receiverData = null;
        receiverImage = null;
    }

    public void setReceiverData(StatusResponse receiverData) {
        this.receiverData = receiverData;
    }

    public void setReceiverImage(BufferedImage receiverImage) {
        this.receiverImage = receiverImage;
    }

}
