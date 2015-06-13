package gtranslator.sound;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegFileFormatType;

public class SoundHelper {
	public static class SoundException extends Exception {
		private static final long serialVersionUID = 1L;

		public SoundException(String message, Throwable th) {
			super(message, th);
		}

		public SoundException(String message) {
			super(message);
		}
	}

	static AudioFormat WAVE_FORMAT;

	static {
		int sampleRate = 44100;
		boolean bigEndian = false;
		boolean signed = true;
		int bits = 16;
		int channels = 2;
		WAVE_FORMAT = new AudioFormat(sampleRate, bits, channels, signed,
				bigEndian);
	}

	public static AudioInputStream createEmptyWaveFile(int seconds) {
		int bufferLength = seconds * (int) WAVE_FORMAT.getSampleRate();
		final byte[] byteBuffer = new byte[bufferLength * 2];
		Arrays.fill(byteBuffer, (byte) 0);
		ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
		return new AudioInputStream(bais, WAVE_FORMAT, bufferLength);
	}

	public static AudioInputStream convertWave(File mp3File)
			throws UnsupportedAudioFileException, IOException {
		AudioInputStream mp3In = AudioSystem.getAudioInputStream(mp3File);
		MpegFormatConversionProvider cnv = new MpegFormatConversionProvider();
		return cnv.isConversionSupported(WAVE_FORMAT, mp3In.getFormat()) ? cnv
				.getAudioInputStream(WAVE_FORMAT, mp3In) : null;
	}

	public static void concatFiles(int seconds, File outWaveFile,
			List<File> mp3Files) throws UnsupportedAudioFileException,
			IOException, SoundException {
		List<AudioInputStream> streamList = new ArrayList<>();
		Long frameLength = -1l;
		for (File mp3File : mp3Files) {
			AudioInputStream in = convertWave(mp3File);
			if (in == null) {
				throw new SoundHelper.SoundException(
						"the file is not support convert:"
								+ mp3File.getAbsolutePath());
			}
			streamList.add(in);
			streamList.add(createEmptyWaveFile(seconds));
			// frameLength += in.getFrameLength();
		}

		AudioInputStream ins = new AudioInputStream(new SequenceInputStream(
				Collections.enumeration(streamList)), WAVE_FORMAT, frameLength);
		AudioSystem.write(ins, MpegFileFormatType.WAVE, outWaveFile);
		for (AudioInputStream in : streamList) {
			in.close();
		}
	}

	public static void concatFiles(int seconds, String sourceWordsFilePath,
			String sourceMp3DirPath, String targetDir, String targetFileName,
			int blockLimit) throws SoundException {
		int suffics = 0;
		try {
			List<String> ss = Files
					.readAllLines(Paths.get(sourceWordsFilePath));
			List<File> fs = new ArrayList<File>();
			for (String s : ss) {
				String word = s.split("[=]")[0];
				fs.add(new File(sourceMp3DirPath, word + ".mp3"));
				if (fs.size() >= blockLimit) {
					concatFiles(
							seconds,
							Paths.get(
									targetDir,
									String.format("%s_%d.wav", targetFileName,
											suffics)).toFile(), fs);
					fs.clear();
					suffics++;
				}
			}
			if (fs.size() > 0) {
				concatFiles(
						seconds,
						Paths.get(
								targetDir,
								String.format("%s_%d.wav", targetFileName,
										suffics)).toFile(), fs);
			}
		} catch (Exception ex) {
			throw new SoundException(ex.getMessage());
		}
	}

	public static void play(File mp3File) throws SoundException {
		AudioInputStream in;
		try {
			in = convertWave(mp3File);
		} catch (UnsupportedAudioFileException | IOException ex) {
			throw new SoundException(ex.getMessage());
		}
		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				WAVE_FORMAT);
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(in.getFormat());
		} catch (Exception ex) {
			throw new SoundException(ex.getMessage());
		}

		line.start();
		int EXTERNAL_BUFFER_SIZE = 1024; // 128Kb
		int nBytesRead = 0;
		byte[] data = new byte[EXTERNAL_BUFFER_SIZE];
		while (nBytesRead != -1) {
			try {
				nBytesRead = in.read(data, 0, data.length);
			} catch (IOException ex) {
				throw new SoundException(ex.getMessage());
			}
			if (nBytesRead >= 0) {
				line.write(data, 0, nBytesRead);
			}
		}
		line.drain();
		line.close();
	}

	public static void main1(String[] args) throws Exception {
		File f1 = new File("gtranslator-dictionary/string.mp3");
		File f2 = new File("gtranslator-dictionary/format.mp3");
		File f3 = new File("gtranslator-dictionary/index.mp3");
		File f = new File("gtranslator-dictionary/en.wav");
		List<File> fs = new ArrayList<File>();
		fs.add(f1);
		fs.add(f2);
		fs.add(f3);
		// concatFiles(2, f, fs);
		// play(f3);
	}

	/*
	 * public static void createEmptyFile() throws Exception { double sampleRate
	 * = 44100.0; double frequency = 440; double frequency2 = 90; double
	 * amplitude = 0.0; double seconds = 2.0; double twoPiF = 2 * Math.PI *
	 * frequency; double piF = Math.PI * frequency2; float[] buffer = new
	 * float[(int) (seconds * sampleRate)]; for (int sample = 0; sample <
	 * buffer.length; sample++) { double time = sample / sampleRate;
	 * buffer[sample] = (float) (amplitude * Math.cos((double)piF *time)*
	 * Math.sin(twoPiF * time)); } final byte[] byteBuffer = new
	 * byte[buffer.length * 2]; int bufferIndex = 0; for (int i = 0; i <
	 * byteBuffer.length; i++) { final int x = (int) (buffer[bufferIndex++] *
	 * 32767.0); byteBuffer[i] = (byte) x; i++; byteBuffer[i] = (byte) (x >>>
	 * 8); } File out = new File("/home/vns/gtranslator-dictionary/test1.wav");
	 * boolean bigEndian = false; boolean signed = true; int bits = 16; int
	 * channels = 1; AudioFormat format; format = new
	 * AudioFormat((float)sampleRate, bits, channels, signed, bigEndian);
	 * ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
	 * AudioInputStream audioInputStream; audioInputStream = new
	 * AudioInputStream(bais, format,buffer.length);
	 * AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
	 * audioInputStream.close();
	 */

}
