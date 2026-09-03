package io.github.defective4.sdr.owrxdesktop.application;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.defective4.sdr.owrxclient.model.ReceiverGPS;
import io.github.defective4.sdr.owrxdesktop.audio.FFMpeg;
import io.github.defective4.sdr.owrxdesktop.bandplan.SerializedBandplan;
import io.github.defective4.sdr.owrxdesktop.ui.settings.UITheme;

public class ApplicationSettings {
    private boolean autoDownloadPublicReceivers = true;
    private boolean autoRefreshPrivateReceivers = false;
    private boolean enableDiscordPresence = false;

    private String ffmpegPath = FFMpeg.probeFFMpeg().orElse("");
    private double latitude = 0;

    private List<SerializedBandplan> loadedBandplans = List.of();
    private double longitude = 0;
    private int maxNetworkWorkers = 3;
    private boolean notifyChatMessages = true;
    private UITheme theme = UITheme.FLAT_LAF_DARCULA;

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

    public UITheme getTheme() {
        return theme;
    }

    public boolean isAutoDownloadPublicReceivers() {
        return autoDownloadPublicReceivers;
    }

    public boolean isAutoRefreshPrivateReceivers() {
        return autoRefreshPrivateReceivers;
    }

    public boolean isEnableDiscordPresence() {
        return enableDiscordPresence;
    }

    public boolean isNotifyChatMessages() {
        return notifyChatMessages;
    }

    public void setAutoDownloadPublicReceivers(boolean autoDownloadPublicReceivers) {
        this.autoDownloadPublicReceivers = autoDownloadPublicReceivers;
    }

    public void setAutoRefreshPrivateReceivers(boolean autoRefreshPrivateReceivers) {
        this.autoRefreshPrivateReceivers = autoRefreshPrivateReceivers;
    }

    public void setEnableDiscordPresence(boolean enableDiscordPresence) {
        this.enableDiscordPresence = enableDiscordPresence;
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

    public void setNotifyChatMessages(boolean notifyChatMessages) {
        this.notifyChatMessages = notifyChatMessages;
    }

    public void setTheme(UITheme theme) {
        this.theme = Objects.requireNonNull(theme);
    }

}
