package io.github.defective4.sdr.owrxdesktop;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import io.github.defective4.sdr.owrxdesktop.ui.ReceiverWindow;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

            ReceiverWindow rxWindow = new ReceiverWindow();
            rxWindow.setVisible(true);
//            rxWindow.setTuningStep((int) 50e3f);
//            rxWindow.setCenterFrequency((int) 100e6);
//            rxWindow.setScopeLower((int) -75e3f);
//            rxWindow.setScopeUpper((int) 75e3f);
//            rxWindow.setTuningReady(true);

//            OpenWebRXClient client = new OpenWebRXClient(URI.create("wss://radio.raspberry.local/ws/"));
//            client.addListener(new OWRXAdapter() {
//
//                private String modulation;
//
//                @Override
//                public void bandsUpdated(io.github.defective4.sdr.owrxclient.model.Band[] bands) {
//                    bandplan.setBands(Arrays.stream(bands).map(band -> {
//                        Color color = bandplan.getDefaultTagColor();
//                        if (band.tags() != null && band.tags().length > 0) for (String tag : band.tags()) {
//                            Optional<Color> tagColor = bandplan.getColorForTag(tag);
//                            if (tagColor.isPresent()) {
//                                color = tagColor.get();
//                                break;
//                            }
//                        }
//                        return new Band(band.lowerFrequency(), band.higherFrequency(), color, band.name());
//                    }).collect(Collectors.toSet()));
//                    rxWindow.updateBandplan();
//                }
//
//                @Override
//                public void fftUpdated(float[] fft) {
//                    rxWindow.drawFFT(fft, 18);
//                }
//
//                @Override
//                public void receiverModesUpdated(ReceiverMode[] modes) {
//                    if (modulation != null) {
//                        client.getModeByName(modulation).ifPresent(mode -> {
//                            Bandpass bandpass = mode.bandpass();
//                            rxWindow.setScopeLower(bandpass.lowCut());
//                            rxWindow.setScopeUpper(bandpass.highCut());
//                        });
//                    }
//                }
//
//                @Override
//                public void receiverProfilesUpdated(ReceiverProfile[] profiles) {
//                    client.switchProfile(Arrays.stream(profiles).filter(profile -> profile.name().equals("RTL-SDR 2m"))
//                            .findAny().get());
//                }
//
//                @Override
//                public void serverConfigChanged(ServerConfig config) {
//                    if (config.sampleRate() != null) rxWindow.setBandwidth(config.sampleRate());
//                    if (config.tuningStep() != null) rxWindow.setTuningStep(config.tuningStep());
//                    if (config.centerFrequency() != null) rxWindow.setCenterFrequency(config.centerFrequency());
//                    if (config.startOffsetFrequency() != null) {
//                        rxWindow.tune(config.startOffsetFrequency(), false);
//                    }
//                    if (config.startModulation() != null) {
//                        modulation = config.startModulation();
//                        client.getModeByName(modulation).ifPresent(mode -> {
//                            Bandpass bandpass = mode.bandpass();
//                            rxWindow.setScopeLower(bandpass.lowCut());
//                            rxWindow.setScopeUpper(bandpass.highCut());
//                        });
//                    }
//                    if (config.waterfallColors() != null) {
//                        rxWindow.setWaterfallTheme(config.mappedWaterfallColors());
//                    }
//
//                    rxWindow.setTuningReady(true);
//                }
//            });
//            client.connect();

//            new Timer(true).scheduleAtFixedRate(new TimerTask() {
//                private float last;
//                private final float max = -30;
//                private final float min = -88;
//                private final int minMin = 120;
//                private boolean up;
//
//                {
//                    last = min;
//                }
//
//                @Override
//                public void run() {
//                    double mult = System.currentTimeMillis() % 10000 / 10000d;
//                    float[] fft = new float[8096];
//                    for (int i = 0; i < fft.length; i++) {
//                        fft[i] = min;
//                    }
//
//                    double x = Math.sin(Math.toRadians(360 * mult));
//                    x += 3;
//                    x /= 6;
//
//                    int i = (int) Math.round(fft.length * x);
//
//                    float diff = max - min;
//                    for (int j = 0; j <= 180; j++) {
//                        double sin = Math.sin(Math.toRadians(j));
//                        fft[i + j - 90] = min + (float) (diff * sin);
//                    }
//
//                    rxWindow.drawFFT(fft, 0);
//                }
//            }, 100, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
