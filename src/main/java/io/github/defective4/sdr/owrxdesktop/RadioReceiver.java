package io.github.defective4.sdr.owrxdesktop;

import java.awt.Color;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sound.sampled.LineUnavailableException;

import io.github.defective4.sdr.owrxclient.client.OpenWebRXClient;
import io.github.defective4.sdr.owrxclient.event.OWRXAdapter;
import io.github.defective4.sdr.owrxclient.model.Band;
import io.github.defective4.sdr.owrxclient.model.Bandpass;
import io.github.defective4.sdr.owrxclient.model.Bookmark;
import io.github.defective4.sdr.owrxclient.model.DialFrequency;
import io.github.defective4.sdr.owrxclient.model.ReceiverMode;
import io.github.defective4.sdr.owrxclient.model.ReceiverProfile;
import io.github.defective4.sdr.owrxclient.model.ServerConfig;
import io.github.defective4.sdr.owrxdesktop.audio.AudioSinkManager;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;
import io.github.defective4.sdr.owrxdesktop.cache.ReceiverCache;
import io.github.defective4.sdr.owrxdesktop.ui.BookmarksDialog.MergedLabel;
import io.github.defective4.sdr.owrxdesktop.ui.ReceiverWindow;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel;
import io.github.defective4.sdr.owrxdesktop.ui.component.FFTLabel.Type;
import io.github.defective4.sdr.owrxdesktop.ui.event.UserInteractionListener;
import io.github.defective4.sdr.owrxdesktop.ui.settings.ReceiverUserSettings;
import io.github.defective4.sdr.owrxdesktop.ui.settings.waterfall.WaterfallThemeMode;

public class RadioReceiver {

    protected Color[] waterfallTheme = { Color.black, Color.white };
    private final AudioSinkManager audioSinkManager;
    private final ReceiverCache cache = new ReceiverCache();

    private final OpenWebRXClient client;
    private boolean freeTuned;
    private int jumpFreq = -1;
    private String jumpMode;

    private String modulation, profileId;
    private final ReceiverWindow rxWindow;

    private final ReceiverUserSettings settings;
    private final URI uri;

    public RadioReceiver(URI uri, ReceiverUserSettings settings) throws LineUnavailableException {
        this.settings = settings;
        audioSinkManager = new AudioSinkManager();
        this.uri = uri;
        rxWindow = new ReceiverWindow(settings, cache);
        client = prepareClient();
        rxWindow.addListener(new UserInteractionListener() {

            @Override
            public void bookmarkJumped(MergedLabel label) {
                rxWindow.getProfileById(label.profile()).ifPresent(profile -> {
                    FFTLabel lbl = label.label();
                    if (profile.uuids()[1].equals(profileId)) {
                        System.out.println(1);
                        int offset = lbl.freq() - rxWindow.getCenterFrequency();
                        rxWindow.tune(offset, true, false);
                        client.getModeByName(lbl.mode()).ifPresent(m -> { client.setModulation(m); });
                        return;
                    }
                    client.switchProfile(profile);
                    jumpFreq = lbl.freq();
                    jumpMode = lbl.mode();
                });
            }

            @Override
            public void freeTune(int freq) {
                jumpFreq = freq;
                freeTuned = true;
                client.setCenterFrequency(freq, settings.getMagicKey());
            }

            @Override
            public void modeChanged(ReceiverMode primary, ReceiverMode underlying) {
                client.setModulation(primary, underlying);
            }

            @Override
            public void muteToggled(boolean muted) {
                audioSinkManager.setMute(muted);
            }

            @Override
            public void profileChanged(ReceiverProfile profile) {
                client.switchProfile(profile);
            }

            @Override
            public void scopeChanged(int scopeLower, int scopeUpper) {
                client.setDemodulatorScope(scopeUpper, scopeLower);
            }

            @Override
            public void settingsChanged() {
                Color[] theme = switch (settings.getWaterfallThemeMode()) {
                    default -> waterfallTheme;
                    case BUILTIN ->
                        ReceiverUserSettings.getTheme(settings.getSelectedBuiltinWaterfallTheme().getReference())
                                .orElse(waterfallTheme);
                    case CUSTOM -> settings.getWaterfallCustomTheme().stream().map(Color::decode).toArray(Color[]::new);
                };
                rxWindow.setWaterfallTheme(theme);
                rxWindow.setColorMixing(settings.isDynamicColorMixing());
            }

            @Override
            public void tuned(int offset) {
                client.setOffsetFrequency(offset);
            }

            @Override
            public void volumeChanged(float value) {
                audioSinkManager.setVolume(value);
            }
        });

        rxWindow.getListeners().forEach(ls -> ls.settingsChanged());

        rxWindow.addFFTPanelListener(label -> {
            int offset = label.freq() - rxWindow.getCenterFrequency();
            rxWindow.tune(offset, true, false);
            client.getModeByName(label.mode()).ifPresent(mode -> {
                if (label.underlying() != null) {
                    Optional<ReceiverMode> uOpt = client.getModeByName(label.underlying());
                    if (uOpt.isPresent()) {
                        client.setModulation(mode, uOpt.get());
                        rxWindow.setStartingMode(mode);
                        return;
                    }
                }
                client.setModulation(mode);
                rxWindow.setStartingMode(mode);
            });
        });
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
            public void bookmarksUpdated(Bookmark[] bookmarks) {
                List<FFTLabel> labels = Arrays.stream(bookmarks)
                        .map(bookmark -> new FFTLabel(bookmark.frequency(), bookmark.name(), Color.yellow,
                                Color.decode("#979700"), Type.BOOKMARK, bookmark.modulation(), bookmark.underlying()))
                        .toList();
                rxWindow.setLabels(labels);
                if (!freeTuned) cache.setLabels(profileId, labels);
            }

            @Override
            public void cpuUsageUpdated(float cpuUsage) {
                rxWindow.setCPUUsage(cpuUsage);
            }

            @Override
            public void dialFrequenciesUpdated(DialFrequency[] frequencies) {
                List<FFTLabel> labels = Arrays.stream(frequencies).map(freq -> new FFTLabel(freq.frequency(),
                        freq.mode(), Color.green, Color.decode("#009000"), Type.DIAL, freq.mode(), null)).toList();
                rxWindow.setLabels(labels);
                if (!freeTuned) cache.setLabels(profileId, labels);
            }

            @Override
            public void fftUpdated(float[] fft) {
                rxWindow.drawFFT(fft, 18);
            }

            @Override
            public void handshakeReceived(String server, String version) {
                client.startDSP();
            }

            @Override
            public void highQualityAudioReceived(byte[] data) {
                try {
                    audioSinkManager.writeHighSamples(data);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void lowQualityAudioReceived(byte[] data) {
                try {
                    audioSinkManager.writeLowSamples(data);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void numberOfClientsUpdated(int clients) {
                rxWindow.setClients(clients);
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
            public void receiverProfilesUpdated(ReceiverProfile[] profiles) {
                rxWindow.updateProfiles(profiles);
                if (profileId != null) {
                    Optional<ReceiverProfile> profile = rxWindow.getProfileById(profileId);
                    if (profile.isPresent()) {
                        rxWindow.updateProfile(profile.get());
                    }
                }
            }

            @Override
            public void serverConfigChanged(ServerConfig config) {
                if (config.sampleRate() != null) {
                    rxWindow.setBandwidth(config.sampleRate());
                }
                if (config.tuningStep() != null) rxWindow.setTuningStep(config.tuningStep());
                if (config.centerFrequency() != null) {
                    rxWindow.setCenterFrequency(config.centerFrequency());
                }
                if (config.startOffsetFrequency() != null) {
                    rxWindow.tune(config.startOffsetFrequency(), true, false);
                }
                if (config.startOffsetFrequency() != null && config.centerFrequency() != null) {
                    if (jumpFreq >= 0) {
                        int offset = jumpFreq - config.centerFrequency();
                        jumpFreq = -1;
                        rxWindow.tune(offset);
                    }
                }
                if (config.profileId() != null) {
                    freeTuned = false;
                    profileId = config.profileId();
                    Optional<ReceiverProfile> profile = rxWindow.getProfileById(profileId);
                    if (profile.isPresent()) {
                        rxWindow.updateProfile(profile.get());
                    }
                }
                if (config.startModulation() != null) {
                    modulation = config.startModulation();
                    if (jumpMode != null) {
                        client.getModeByName(jumpMode).ifPresent(mode -> client.setModulation(mode));
                        modulation = jumpMode;
                        jumpMode = null;
                    }
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
                    waterfallTheme = config.mappedWaterfallColors();
                    if (settings.getWaterfallThemeMode() == WaterfallThemeMode.SERVER)
                        rxWindow.setWaterfallTheme(config.mappedWaterfallColors());
                }

                if (config.waterfallLevels() != null) {
                    rxWindow.setServerLevels(config.waterfallLevels());
                }

                rxWindow.setTuningReady(true);

                rxWindow.getFftPanel().resetMaxFFT();
                rxWindow.resetAutoFFT();
                if (config.maxClients() != null) rxWindow.setMaxClients(config.maxClients());
            }

            @Override
            public void signalMeterUpdated(float signalLevel) {
                rxWindow.updateSignal(signalLevel);
            }

            @Override
            public void temperatureUpdated(int temperatureC) {
                rxWindow.setTemperature(temperatureC);
            }
        });

        return client;
    }
}
