package io.github.defective4.sdr.owrxdesktop;

import java.awt.Color;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.defective4.sdr.owrxclient.client.OpenWebRXClient;
import io.github.defective4.sdr.owrxclient.event.OWRXAdapter;
import io.github.defective4.sdr.owrxclient.model.Band;
import io.github.defective4.sdr.owrxclient.model.Bandpass;
import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxclient.model.ServerConfig;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;
import io.github.defective4.sdr.owrxdesktop.ui.ReceiverWindow;

public class RadioReceiver {

    private final OpenWebRXClient client;
    private final ReceiverWindow rxWindow;
    private final URI uri;

    public RadioReceiver(URI uri) {
        this.uri = uri;
        rxWindow = new ReceiverWindow();
        client = prepareClient();
    }

    public void connect() throws InterruptedException {
        client.connect();
    }

    public void setVisible(boolean b) {
        rxWindow.setVisible(b);
    }

    private OpenWebRXClient prepareClient() {
        OpenWebRXClient client = new OpenWebRXClient(uri);

        client.addListener(new OWRXAdapter() {

            private String modulation;

            @Override
            public void bandsUpdated(Band[] bands) {
                Bandplan bandplan = rxWindow.getBandplan();
                bandplan.setBands(Arrays.stream(bands).map(band -> {
                    Color color = bandplan.getDefaultTagColor();
                    if (band.tags() != null && band.tags().length > 0) for (String tag : band.tags()) {
                        Optional<Color> tagColor = bandplan.getColorForTag(tag);
                        if (tagColor.isPresent()) {
                            color = tagColor.get();
                            break;
                        }
                    }
                    return new io.github.defective4.sdr.owrxdesktop.bandplan.Band(band.lowerFrequency(),
                            band.higherFrequency(), color, band.name());
                }).collect(Collectors.toSet()));
                rxWindow.updateBandplan();
            }

            @Override
            public void fftUpdated(float[] fft) {
                rxWindow.drawFFT(fft, 18);
            }

            @Override
            public void handshakeReceived(String server, String version) {
                // TODO start dsp
            }

            @Override
            public void receiverModesUpdated(ReceiverMode[] modes) {
                rxWindow.updateModes(modes);
                if (modulation != null) client.getModeByName(modulation).ifPresent(mode -> {
                    Bandpass bandpass = mode.bandpass();
                    if (bandpass != null) {
                        rxWindow.setScopeLower(bandpass.lowCut());
                        rxWindow.setScopeUpper(bandpass.highCut());
                    }
                    rxWindow.setStartingMode(mode);
                });

            }

            @Override
            public void serverConfigChanged(ServerConfig config) {
                if (config.sampleRate() != null) rxWindow.setBandwidth(config.sampleRate());
                if (config.tuningStep() != null) rxWindow.setTuningStep(config.tuningStep());
                if (config.centerFrequency() != null) rxWindow.setCenterFrequency(config.centerFrequency());
                if (config.startOffsetFrequency() != null) {
                    rxWindow.tune(config.startOffsetFrequency(), false);
                }
                if (config.startModulation() != null) {
                    modulation = config.startModulation();
                    client.getModeByName(modulation).ifPresent(mode -> {
                        Bandpass bandpass = mode.bandpass();
                        if (bandpass != null) {
                            rxWindow.setScopeLower(bandpass.lowCut());
                            rxWindow.setScopeUpper(bandpass.highCut());
                        }
                        rxWindow.setStartingMode(mode);
                    });
                }
                if (config.waterfallColors() != null) {
                    rxWindow.setWaterfallTheme(config.mappedWaterfallColors());
                }

                if (config.waterfallLevels() != null) {
                    rxWindow.setServerLevels(config.waterfallLevels());
                }

                rxWindow.setTuningReady(true);

                rxWindow.getFftPanel().resetMaxFFT();
                rxWindow.resetAutoFFT();
            }
        });

        return client;
    }
}
