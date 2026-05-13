package io.github.defective4.sdr.owrxdesktop.audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FFMpeg {
    private final String path;

    public FFMpeg(String path) {
        this.path = path;
    }

    public void convertToMP3(File from, File to) throws IOException {
        if (!isAvailable()) throw new IllegalStateException("ffmpeg is not available");
        Process proc = null;
        try {
            Process fproc = new ProcessBuilder(path, "-i", from.getPath(), "-aq", "2", to.getPath()).start();
            proc = fproc;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> fproc.destroyForcibly()));
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (proc != null) proc.destroyForcibly();
        }
    }

    public boolean isAvailable() {
        try {
            Process proc = new ProcessBuilder(path).start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> proc.destroyForcibly()));
            try (BufferedReader reader = proc.errorReader()) {
                return reader.readLine().startsWith("ffmpeg version ");
            } finally {
                proc.destroyForcibly();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            FFMpeg ffmpeg = new FFMpeg("/bin/ffmpeg");
            ffmpeg.convertToMP3(new File("/home/defective/Recording 2026-05-13-17-37-15.wav"),
                    new File("/tmp/test.mp3"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
