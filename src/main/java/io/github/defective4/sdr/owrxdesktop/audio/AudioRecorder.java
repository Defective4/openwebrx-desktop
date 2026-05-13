package io.github.defective4.sdr.owrxdesktop.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioRecorder {
    private FFMpeg ffmpeg = new FFMpeg("/bin/ffmpeg");
    private boolean hi = false;
    private OutputStream output;
    private boolean processMP3 = false;
    private final ByteBuffer resamplingBuffer = ByteBuffer.allocate(40960);

    private File target;

    public FFMpeg getFfmpeg() {
        return ffmpeg;
    }

    public boolean isProcessMP3() {
        return processMP3;
    }

    public boolean isStarted() {
        return output != null;
    }

    public void setFfmpeg(FFMpeg ffmpeg) {
        this.ffmpeg = ffmpeg;
    }

    public void setProcessMP3(boolean processMP3) {
        this.processMP3 = processMP3;
    }

    public void start(File target, boolean hi) throws FileNotFoundException {
        this.target = target;
        this.hi = hi;
        output = new FileOutputStream(target);
    }

    public void stop() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }

        if (target != null && target.isFile()) {
            File tmp = Files.createTempFile("owrxrecord", ".wav").toFile();
            long len = target.length() / 2;
            Files.move(target.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try (AudioInputStream in = new AudioInputStream(new FileInputStream(tmp),
                    hi ? AudioSinkManager.HI_FORMAT : AudioSinkManager.LO_FORMAT, len)) {
                AudioSystem.write(in, Type.WAVE, target);
            }
            tmp.delete();
            if (processMP3) {
                File mp3File = new File(target.getPath().substring(0, target.getPath().length() - 4) + ".mp3");
                ffmpeg.convertToMP3(target, mp3File);
                target.delete();
            }
        }
        target = null;
    }

    public void writeData(byte[] data, boolean hi) throws IOException {
        if (output != null) {
            byte[] buffered = writeAndFlushBuffer(data);
            if (buffered.length > 0) {
                if (this.hi != hi) {
                    if (hi) {
                        ByteBuffer resampled = ByteBuffer.allocate(buffered.length / 4);
                        for (int i = 0; i < resampled.capacity(); i += 2) {
                            resampled.put(buffered[i * 4]);
                            resampled.put(buffered[i * 4 + 1]);
                        }
                        output.write(resampled.array());
                    } else {
                        ByteBuffer resampled = ByteBuffer.allocate(buffered.length * 4).order(ByteOrder.LITTLE_ENDIAN);
                        for (int i = 0; i < buffered.length - 1; i += 2) {
                            resampled.put(buffered[i]);
                            resampled.put(buffered[i + 1]);
                            int pos = resampled.position();
                            if (pos > 10) {
                                resampled.position(pos - 2);
                                short point2 = resampled.getShort();
                                resampled.position(pos - 10);
                                short point1 = resampled.getShort();

                                short upper = (short) Math.max(point2, point1);
                                short lower = (short) Math.min(point2, point1);

                                short p1 = (short) (upper * 0.75 + lower * 0.25);
                                short p2 = (short) ((point2 + point1) / 2);
                                short p3 = (short) (upper * 0.25 + lower * 0.75);

                                resampled.putShort(p1);
                                resampled.putShort(p2);
                                resampled.putShort(p3);
                                resampled.position(pos);
                            }
                            resampled.position(resampled.position() + 6);
                        }
                        output.write(resampled.array());
                    }
                } else {
                    output.write(buffered);
                }
            }
        }
    }

    private byte[] writeAndFlushBuffer(byte[] data) {
        if (data.length > resamplingBuffer.remaining()) {
            byte[] partial = new byte[resamplingBuffer.remaining()];
            byte[] remaining = new byte[data.length - resamplingBuffer.remaining()];

            System.arraycopy(data, 0, partial, 0, partial.length);
            System.arraycopy(data, partial.length, remaining, 0, remaining.length);

            resamplingBuffer.put(partial);
            byte[] fullData = Arrays.copyOf(resamplingBuffer.array(), resamplingBuffer.capacity());
            resamplingBuffer.position(0);
            resamplingBuffer.put(remaining);
            return fullData;
        }
        resamplingBuffer.put(data);
        return new byte[0];
    }
}
