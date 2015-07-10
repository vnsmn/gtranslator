package gtranslator.sound;

import gtranslator.App;
import gtranslator.AppProperties;
import gtranslator.exception.GTranslatorException;
import gtranslator.exception.SoundReceiverException;
import gtranslator.ui.Constants;
import gtranslator.ui.Constants.PHONETICS;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
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
	static AudioFormat WAVE_FORMAT_16000;

	static AudioFormat WAVE_FORMAT_44100;

	private static final Logger logger = Logger.getLogger(SoundHelper.class);
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

	public static void concatFiles(int seconds, int secondsDefis,
			File outWaveFile, List<FileEntry> soundFiles)
			throws UnsupportedAudioFileException, IOException {
		List<AudioInputStream> streamList = new ArrayList<>();
		Long frameLength = -1l;
		streamList.add(createEmptyWaveFile(2));
		for (FileEntry mp3File : soundFiles) {
			AudioInputStream in = getAudioInputStreamOfMp3File(mp3File.leftFile);
			if (in == null) {
				throw new GTranslatorException(
						"the file is not support convert:"
								+ mp3File.leftFile.getAbsolutePath());
			}
			streamList.add(in);
			if (mp3File.rightFile != null && mp3File.rightFile.exists()) {
				streamList.add(createEmptyWaveFile(secondsDefis));
				in = getAudioInputStreamOfMp3File(mp3File.rightFile);
				streamList.add(in);
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

	public static AudioInputStream convertWave(File mp3File,
			AudioFormat audioFormat) throws UnsupportedAudioFileException,
			IOException {
		AudioInputStream mp3In = AudioSystem.getAudioInputStream(mp3File);
		MpegFormatConversionProvider cnv = new MpegFormatConversionProvider();
		return cnv.isConversionSupported(audioFormat, mp3In.getFormat()) ? cnv
				.getAudioInputStream(audioFormat, mp3In) : null;
	}

	public static AudioInputStream createEmptyWaveFile(int seconds) {
		int bufferLength = seconds * (int) WAVE_FORMAT_44100.getSampleRate();
		final byte[] byteBuffer = new byte[bufferLength * 2];
		Arrays.fill(byteBuffer, (byte) 0);
		ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
		return new AudioInputStream(bais, WAVE_FORMAT_44100, bufferLength);
	}

	public static File getEngFile(String phrase, PHONETICS ph) {
		File f = App.getOxfordReceiverService().getSound(phrase, ph);
		if (f == null) {
			if (AppProperties.getInstance().isDictionarySynthesizer())
				try {
					f = App.getGoogleSoundReceiverService().getSound(phrase,
							Constants.LANG.ENG);
				} catch (SoundReceiverException ex) {
					logger.error(ex);
				}
		}
		return f;
	}

	public static File getRusFile(String phrase) {
		try {
			return App.getGoogleSoundReceiverService().getSound(phrase,
					Constants.LANG.RUS);
		} catch (SoundReceiverException ex) {
			logger.error(ex);
			return null;
		}
	}

	public static void playEngWord(String engWord) {
		String normal = App.getTranslationService().toNormal(engWord);
		if (normal.matches("[a-zA-Z]+")) {
			File f = getEngFile(normal, AppProperties.getInstance()
					.getDictionaryPhonetic());
			if (f != null && f.exists()) {
				SoundHelper.playFile(f);
			}
		}
	}

	public static void playFile(File mp3File) {
		AudioInputStream in;
		try {
			in = getAudioInputStreamOfMp3File(mp3File);
		} catch (UnsupportedAudioFileException | IOException ex) {
			throw new GTranslatorException(ex.getMessage());
		}
		try {
			SourceDataLine line = null;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,
					WAVE_FORMAT_44100);
			try {
				line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(in.getFormat());
			} catch (Exception ex) {
				throw new GTranslatorException(ex.getMessage());
			}

			line.start();
			int EXTERNAL_BUFFER_SIZE = 1024; // 128Kb
			int nBytesRead = 0;
			byte[] data = new byte[EXTERNAL_BUFFER_SIZE];
			while (nBytesRead != -1) {
				try {
					nBytesRead = in.read(data, 0, data.length);
				} catch (IOException ex) {
					throw new GTranslatorException(ex.getMessage());
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

	private static AudioInputStream getAudioInputStreamOfMp3File(File file)
			throws UnsupportedAudioFileException, IOException {
		AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
		AudioInputStream in = null;
		if (aff.getFormat().getSampleRate() == 16000f) {
			in = convertWave(file, WAVE_FORMAT_16000);
			in = AudioSystem.getAudioInputStream(WAVE_FORMAT_44100, in);
		} else {
			in = convertWave(file, WAVE_FORMAT_44100);
		}
		return in;
	}

	public static class FileEntry {
		public File leftFile;
		public File rightFile;

		public FileEntry(File leftFile, File rightFile) {
			this.leftFile = leftFile;
			this.rightFile = rightFile;
		}
	}
}
