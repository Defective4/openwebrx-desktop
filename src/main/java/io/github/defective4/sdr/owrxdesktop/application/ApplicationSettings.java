package io.github.defective4.sdr.owrxdesktop.application;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.defective4.sdr.owrxclient.model.ReceiverGPS;
import io.github.defective4.sdr.owrxdesktop.audio.FFMpeg;
import io.github.defective4.sdr.owrxdesktop.bandplan.SerializedBandplan;

public class ApplicationSettings {
    private boolean autoDownloadPublicReceivers = true;
    private boolean autoRefreshPrivateReceivers = false;
    private String ffmpegPath = FFMpeg.probeFFMpeg().orElse("");

    private double latitude = 0;
    private List<SerializedBandplan> loadedBandplans = List.of();

    private double longitude = 0;
    private int maxNetworkWorkers = 3;

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public ReceiverGPS getGPS() {
        return new ReceiverGPS(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public List<SerializedBandplan> getLoadedBandplans() {
        return Collections.unmodifiableList(loadedBandplans);
    }

    public double getLongitude() {
        return longitude;
    }

    public int getMaxNetworkWorkers() {
        return maxNetworkWorkers;
    }

    public boolean isAutoDownloadPublicReceivers() {
        return autoDownloadPublicReceivers;
    }

    public boolean isAutoRefreshPrivateReceivers() {
        return autoRefreshPrivateReceivers;
    }

    public void setAutoDownloadPublicReceivers(boolean autoDownloadPublicReceivers) {
        this.autoDownloadPublicReceivers = autoDownloadPublicReceivers;
    }

    public void setAutoRefreshPrivateReceivers(boolean autoRefreshPrivateReceivers) {
        this.autoRefreshPrivateReceivers = autoRefreshPrivateReceivers;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLoadedBandplans(List<SerializedBandplan> loadedBandplans) {
        this.loadedBandplans = Objects.requireNonNull(loadedBandplans);
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMaxNetworkWorkers(int maxNetworkWorkers) {
        this.maxNetworkWorkers = maxNetworkWorkers;
    }

}
