package io.github.defective4.sdr.owrxdesktop.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioRecorder {

    private OutputStream output;
    private File target;

    public boolean isStarted() {
        return output != null;
    }

    public void start(File target) throws FileNotFoundException {
        this.target = target;
        output = new FileOutputStream(target);
    }

    public void stop() throws IOException {
        if (output != null) output.close();
        output = null;

        if (target.isFile()) {
            File tmp = Files.createTempFile("owrxrecord", ".wav").toFile();
            long len = target.length() / 2;
            Files.move(target.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try (AudioInputStream in = new AudioInputStream(new FileInputStream(tmp), AudioSinkManager.HI_FORMAT,
                    len)) {
                AudioSystem.write(in, Type.WAVE, target);
            }
        }
    }

    public void writeData(byte[] data) throws IOException {
        if (output != null) output.write(data);
    }
}
