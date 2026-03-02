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
    private final long timeout = 5000;

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

    public void writeHighSamples(byte[] samples) throws LineUnavailableException {
        synchronized (hiSDL) {
            writeSamples(hiSDL, samples);
            lastHighSample = System.currentTimeMillis();
        }
    }

    public void writeLowSamples(byte[] samples) throws LineUnavailableException {
        synchronized (loSDL) {
            writeSamples(loSDL, samples);
            lastLowSample = System.currentTimeMillis();
        }
    }

    private static void writeSamples(SourceDataLine sdl, byte[] samples) throws LineUnavailableException {
        if (!sdl.isOpen()) sdl.open();
        if (!sdl.isRunning()) sdl.start();
        sdl.write(samples, 0, samples.length);
    }
}
