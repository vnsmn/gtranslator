package gtranslator.sound;

import gtranslator.AppProperties;
import gtranslator.DictionaryHelper;
import gtranslator.sound.SoundReceiver.LANG;
import gtranslator.translate.TranslationReceiver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegFileFormatType;

import org.apache.log4j.Logger;

public class SoundHelper {
	private static final Logger logger = Logger.getLogger(SoundHelper.class);

	public static class SoundException extends Exception {
		private static final long serialVersionUID = 1L;

		public SoundException(String message, Throwable th) {
			super(message, th);
		}

		public SoundException(String message) {
			super(message);
		}
	}

	static AudioFormat WAVE_FORMAT_44100;
	static AudioFormat WAVE_FORMAT_16000;

	static {
		int sampleRate_44100 = 44100;
		int sampleRate_16000 = 16000;
		boolean bigEndian = false;
		boolean signed = true;
		int bits = 16;
		int channels = 2;
		WAVE_FORMAT_44100 = new AudioFormat(sampleRate_44100, bits, channels,
				signed, bigEndian);
		WAVE_FORMAT_16000 = new AudioFormat(sampleRate_16000, bits, channels,
				signed, bigEndian);
	}

	public static AudioInputStream createEmptyWaveFile(int seconds) {
		int bufferLength = seconds * (int) WAVE_FORMAT_44100.getSampleRate();
		final byte[] byteBuffer = new byte[bufferLength * 2];
		Arrays.fill(byteBuffer, (byte) 0);
		ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
		return new AudioInputStream(bais, WAVE_FORMAT_44100, bufferLength);
	}

	public static AudioInputStream convertWave(File mp3File,
			AudioFormat audioFormat) throws UnsupportedAudioFileException,
			IOException {
		AudioInputStream mp3In = AudioSystem.getAudioInputStream(mp3File);
		MpegFormatConversionProvider cnv = new MpegFormatConversionProvider();
		return cnv.isConversionSupported(audioFormat, mp3In.getFormat()) ? cnv
				.getAudioInputStream(audioFormat, mp3In) : null;
	}

	public static class FileEntry {
		public File engFile;
		public File rusFile;
		public File synthEngFile;

		public FileEntry(File engFile, File synthEngFile, File rusFile) {
			this.engFile = engFile;
			this.synthEngFile = synthEngFile;
			this.rusFile = rusFile;
		}
	}

	public static void concatFiles(int seconds, int secondsDefis,
			File outWaveFile, List<FileEntry> soundFiles)
			throws UnsupportedAudioFileException, IOException, SoundException {
		List<AudioInputStream> streamList = new ArrayList<>();
		Long frameLength = -1l;
		streamList.add(createEmptyWaveFile(2));
		for (FileEntry mp3File : soundFiles) {
			AudioInputStream in = null;
			if (mp3File.engFile != null) {
				in = convertWave(mp3File.engFile, WAVE_FORMAT_44100);
			} else if (mp3File.synthEngFile != null) {
				in = convertWave(mp3File.synthEngFile, WAVE_FORMAT_16000);
				in = AudioSystem.getAudioInputStream(WAVE_FORMAT_44100, in);
			}
			if (in == null) {
				throw new SoundHelper.SoundException(
						"the file is not support convert:"
								+ mp3File.engFile.getAbsolutePath());
			}
			streamList.add(in);
			if (mp3File.rusFile != null) {
				AudioInputStream rusIn = convertWave(mp3File.rusFile,
						WAVE_FORMAT_16000);
				if (rusIn != null) {
					streamList.add(createEmptyWaveFile(secondsDefis));
					rusIn = AudioSystem.getAudioInputStream(WAVE_FORMAT_44100,
							rusIn);
					streamList.add(rusIn);
				}
			}
			streamList.add(createEmptyWaveFile(seconds));
			// frameLength += in.getFrameLength();
		}
		AudioInputStream ins = new AudioInputStream(new SequenceInputStream(
				Collections.enumeration(streamList)), WAVE_FORMAT_44100,
				frameLength);

		AudioSystem.write(ins, MpegFileFormatType.WAVE, outWaveFile);
		for (AudioInputStream in : streamList) {
			in.close();
		}
		LameSoundHelper.INSTANCE.convert(outWaveFile.getAbsolutePath(),
				new File(outWaveFile.getParent(), outWaveFile.getName()
						.replaceAll(".wave", ".mp3")).getAbsolutePath());
	}

	public static void playFile(File mp3File, boolean isSynthes)
			throws SoundException {
		AudioInputStream in;
		try {
			in = isSynthes ? convertWave(mp3File, WAVE_FORMAT_16000)
					: convertWave(mp3File, WAVE_FORMAT_44100);
		} catch (UnsupportedAudioFileException | IOException ex) {
			throw new SoundException(ex.getMessage());
		}
		try {
			SourceDataLine line = null;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,
					WAVE_FORMAT_44100);
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
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				logger.error(ex);
			}
		}
	}

	public static void playEngWord(String engWord, boolean doSoundLoad) {
		String normal = TranslationReceiver.INSTANCE.toNormal(engWord);
		if (normal.matches("[a-zA-Z]+")) {
			String dicDirPath = AppProperties.getInstance()
					.getDictionaryDirPath();
			boolean isBr = SoundReceiver.BR.equalsIgnoreCase(AppProperties
					.getInstance().getDictionaryPronunciation());
			File f = DictionaryHelper.INSTANCE.findFile(isBr, dicDirPath,
					normal);
			try {
				if (doSoundLoad && !f.exists()) {
					List<String> words = new ArrayList<>();
					words.add(normal);
					Set<String> loaded = DictionaryHelper.INSTANCE.loadSound(
							words, new File(dicDirPath), AppProperties
									.getInstance().isDictionarySynthesizer());
					if (loaded.isEmpty()) {
						logger.error("the file " + f.getAbsolutePath()
								+ " not found.");
						return;
					}
				}
				if (!f.exists()
						&& AppProperties.getInstance()
								.isDictionarySynthesizer()) {
					f = new File(GoogleSoundReceiver.INSTANCE.getFilePath(
							dicDirPath, normal, LANG.ENG));
					SoundHelper.playFile(f, true);
				} else {
					SoundHelper.playFile(f, false);
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}
		}
	}

	/*
	 * public static void main1(String[] args) throws Exception { File f1 = new
	 * File("gtranslator-dictionary/string.mp3"); File f2 = new
	 * File("gtranslator-dictionary/format.mp3"); File f3 = new
	 * File("gtranslator-dictionary/index.mp3"); File f = new
	 * File("gtranslator-dictionary/en.wav"); List<File> fs = new
	 * ArrayList<File>(); fs.add(f1); fs.add(f2); fs.add(f3); // concatFiles(2,
	 * f, fs); // play(f3); }
	 */
	// static class Mp3Encoder {
	// public static AudioInputStream getConvertedStream(
	// AudioInputStream sourceStream,
	// AudioFormat.Encoding targetEncoding) throws Exception {
	// AudioFormat sourceFormat = sourceStream.getFormat();
	//
	// AudioInputStream targetStream = null;
	// if (!AudioSystem
	// .isConversionSupported(targetEncoding, sourceFormat)) {
	// AudioFormat intermediateFormat = new AudioFormat(
	// AudioFormat.Encoding.PCM_SIGNED,
	// sourceFormat.getSampleRate(), 16,
	// sourceFormat.getChannels(),
	// 2 * sourceFormat.getChannels(), // frameSize
	// sourceFormat.getSampleRate(), false);
	// if (AudioSystem.isConversionSupported(intermediateFormat,
	// sourceFormat)) {
	// // intermediate conversion is supported
	// sourceStream = AudioSystem.getAudioInputStream(
	// intermediateFormat, sourceStream);
	// }
	// }
	// targetStream = AudioSystem.getAudioInputStream(targetEncoding,
	// sourceStream);
	// if (targetStream == null) {
	// throw new Exception("conversion not supported");
	// }
	// return targetStream;
	// }
	// }

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
