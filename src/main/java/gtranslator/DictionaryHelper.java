package gtranslator;

import gtranslator.SoundHelper.SoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

public class DictionaryHelper {
	final public static DictionaryHelper INSTANCE = new DictionaryHelper();
	static final Logger logger = Logger.getLogger(DictionaryHelper.class);
	private SoundReceiver soundReceiver = new OxfordSoundReceiver();
	private String SOUNDS = "sounds";
	private int pauseSeconds;
	private int blockLimit;

	private DictionaryHelper() {
	};

	public synchronized void createDictionary(Map<String, String> words,
			String targetDirPath) throws FileNotFoundException, IOException {
		File dir = new File(targetDirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File soundDir = new File(dir, SOUNDS);
		if (!soundDir.exists()) {
			soundDir.mkdirs();
		}
		Set<String> loadedSoundWords = loadSound(words, soundDir);

		String text = wordsToString(words, loadedSoundWords, true);
		File wf = new File(dir, "words.txt");
		try (FileOutputStream out = new FileOutputStream(wf)) {
			out.write(text.getBytes("UTF-8"));
		}
		text = wordsToString(words, loadedSoundWords, false);
		wf = new File(dir, "words-sound.txt");
		try (FileOutputStream out = new FileOutputStream(wf)) {
			out.write(text.getBytes("UTF-8"));
		}

		try {
			File brDir = new File(soundDir, SoundReceiver.BR);
			File amDir = new File(soundDir, SoundReceiver.AM);
			SoundHelper.concatFiles(pauseSeconds, wf.getAbsolutePath(),
					brDir.getAbsolutePath(), dir.getAbsolutePath(),
					"words-sound-br", blockLimit);
			SoundHelper.concatFiles(pauseSeconds, wf.getAbsolutePath(),
					amDir.getAbsolutePath(), dir.getAbsolutePath(),
					"words-sound-am", blockLimit);
		} catch (SoundException ex) {
			logger.error(ex.getMessage());
		}
	}

	public File findFile(boolean isBr, String targetDirPath, String word) {
		return isBr ? Paths.get(targetDirPath, SOUNDS, SoundReceiver.BR,
				word + ".mp3").toFile() : Paths.get(targetDirPath, SOUNDS,
				SoundReceiver.AM, word + ".mp3").toFile();
	}

	public synchronized void setPauseSeconds(int pauseSeconds) {
		this.pauseSeconds = pauseSeconds;
	}

	public synchronized void setBlockLimit(int blockLimit) {
		this.blockLimit = blockLimit;
	}

	private String wordsToString(Map<String, String> words,
			Set<String> loadedSoundWords, boolean isAll) {
		TreeMap<String, String> sortMap = new TreeMap<>(
				new Comparator<String>() {
					@Override
					public int compare(String s1, String s2) {
						return s1.compareTo(s2);
					}
				});
		sortMap.putAll(words);
		StringBuffer sb = new StringBuffer();
		for (Entry<String, String> ent : sortMap.entrySet()) {
			if (!isAll && !loadedSoundWords.contains(ent.getKey())) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(ent.getKey());
			if (!loadedSoundWords.contains(ent.getKey())) {
				sb.append("~");
			} else {
				sb.append("=");
			}
			sb.append(ent.getValue());
		}
		return sb.toString();
	}

	private Set<String> loadSound(Map<String, String> words, File dirFile)
			throws IOException {
		Set<String> loaded = new HashSet<>();
		for (Entry<String, String> ent : words.entrySet()) {
			try {
				if (soundReceiver.createSound(dirFile, ent.getKey())) {
					loaded.add(ent.getKey());
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}
		}
		return loaded;
	}
}
