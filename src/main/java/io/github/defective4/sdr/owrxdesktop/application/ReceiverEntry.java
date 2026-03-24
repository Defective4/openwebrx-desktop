package io.github.defective4.sdr.owrxdesktop.application;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

import io.github.defective4.sdr.owrxdesktop.cache.ReceiverCache;
import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;

public class ReceiverEntry {

    private static final Gson GSON = new Gson();

    private final ReceiverCache cache = new ReceiverCache();
    private Exception queryException = null;
    private boolean querying = false;
    private StatusResponse receiverData;
    private BufferedImage receiverImage;
    private final String rootURL;
    private final ReceiverUserSettings settings;
    private final UUID uuid = UUID.randomUUID();

    public ReceiverEntry(String rootURL, ReceiverUserSettings settings) throws MalformedURLException {
        if (!rootURL.endsWith("/")) rootURL += "/";
        URI.create(rootURL).toURL();
        this.rootURL = rootURL;
        this.settings = settings;
    }

    public ReceiverCache getCache() {
        return cache;
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

    public URL getRootURL() {
        try {
            return URI.create(rootURL).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public ReceiverUserSettings getSettings() {
        return settings;
    }

    public URI getWebsocketURI() {
        try {
            URL url = URI.create(rootURL).toURL();
            String protocol = url.getProtocol();
            String wsProtocol = protocol.equalsIgnoreCase("https") ? "wss" : "ws";
            return URI.create(String.format("%s://%s%s/ws/", wsProtocol, url.getHost(),
                    url.getPort() <= 0 ? "" : ":" + url.getPort()));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
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
