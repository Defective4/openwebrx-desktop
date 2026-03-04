package io.github.defective4.sdr.owrxdesktop.audio;

import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioSinkManager {

    private final SourceDataLine hiSDL, loSDL;

    private long lastHighSample, lastLowSample;
    private boolean mute;

    private final long timeout = 5000;
    private float volume = 1f;

    public AudioSinkManager() throws LineUnavailableException {
        hiSDL = AudioSystem.getSourceDataLine(new AudioFormat(48000, 16, 1, true, false));
        loSDL = AudioSystem.getSourceDataLine(new AudioFormat(12000, 16, 1, true, false));

        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (loSDL) {
                    if (System.currentTimeMillis() - lastLowSample > timeout) {
                        loSDL.stop();
                        loSDL.close();
                    }
                }
                synchronized (hiSDL) {
                    if (System.currentTimeMillis() - lastHighSample > timeout) {
                        hiSDL.stop();
                        hiSDL.close();
                    }
                }
            }
        }, timeout, timeout);
    }

    public float getVolume() {
        return volume;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public void setVolume(float vol) {
        volume = vol;
    }

    public void writeHighSamples(byte[] samples) throws LineUnavailableException {
        if (mute) return;
        synchronized (hiSDL) {
            if (volume < 1f) apply16BitVolume(samples);
            writeSamples(hiSDL, samples);
            lastHighSample = System.currentTimeMillis();
        }
    }

    public void writeLowSamples(byte[] samples) throws LineUnavailableException {
        if (mute) return;
        synchronized (loSDL) {
            if (volume < 1f) apply16BitVolume(samples);
            writeSamples(loSDL, samples);
            lastLowSample = System.currentTimeMillis();
        }
    }

    private void apply16BitVolume(byte[] samples) {
        for (int i = 0; i < samples.length; i += 2) {
            short sample = (short) (samples[i] & 0xff | (samples[i + 1] & 0xff) << 8);
            sample *= volume;
            samples[i] = (byte) sample;
            samples[i + 1] = (byte) (sample >> 8);
        }
    }

    private static void writeSamples(SourceDataLine sdl, byte[] samples) throws LineUnavailableException {
        if (!sdl.isOpen()) sdl.open();
        if (!sdl.isRunning()) sdl.start();
        sdl.write(samples, 0, samples.length);
    }
}
