package io.github.defective4.sdr.owrxdesktop.application;

public class ApplicationSettings {
    private boolean autoDownloadPublicReceivers = true;
    private boolean autoRefreshPrivateReceivers = false;
    private int maxNetworkWorkers = 3;

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

    public void setMaxNetworkWorkers(int maxNetworkWorkers) {
        this.maxNetworkWorkers = maxNetworkWorkers;
    }

}
