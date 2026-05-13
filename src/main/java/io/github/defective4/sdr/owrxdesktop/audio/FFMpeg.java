package io.github.defective4.sdr.owrxdesktop.audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FFMpeg {
    private static final List<String> FFMPEG_PATHES = List.of("/bin/ffmpeg", "/sbin/ffmpeg", "ffmpeg", "ffmpeg.exe");
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

    public static Optional<String> probeFFMpeg() {
        for (String path : FFMPEG_PATHES) {
            if (new FFMpeg(path).isAvailable()) return Optional.of(path);
        }
        return Optional.empty();
    }
}
