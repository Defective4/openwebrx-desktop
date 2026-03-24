package io.github.defective4.sdr.owrxdesktop.application;

import io.github.defective4.sdr.owrxclient.model.ReceiverGPS;

public class ApplicationSettings {
    private boolean autoDownloadPublicReceivers = true;
    private boolean autoRefreshPrivateReceivers = false;
    private double latitude = 0;

    private double longitude = 0;
    private int maxNetworkWorkers = 3;

    public ReceiverGPS getGPS() {
        return new ReceiverGPS(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMaxNetworkWorkers(int maxNetworkWorkers) {
        this.maxNetworkWorkers = maxNetworkWorkers;
    }

}
