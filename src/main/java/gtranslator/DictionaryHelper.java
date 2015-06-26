package gtranslator;

import gtranslator.exception.SoundReceiverException;
import gtranslator.sound.GoogleSoundReceiver;
import gtranslator.sound.OxfordSoundReceiver;
import gtranslator.sound.SoundHelper;
import gtranslator.sound.SoundHelper.FileEntry;
import gtranslator.sound.SoundHelper.SoundException;
import gtranslator.sound.SoundReceiver;
import gtranslator.sound.SoundReceiver.LANG;
import gtranslator.translate.TranslationReceiver;
import gtranslator.ui.ProgressMonitorDemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class DictionaryHelper {
	final public static DictionaryHelper INSTANCE = new DictionaryHelper();
	static final Logger logger = Logger.getLogger(DictionaryHelper.class);
	private SoundReceiver soundReceiver = new OxfordSoundReceiver();

	public enum SOURCE_TYPE {
		HISTORY, DICTIONARY, TEXT
	}

	private DictionaryHelper() {
	};

	public static class DictionaryInput {
		public String path;
		public String resultDir;
		private String prefix;
		private Integer pauseSeconds;
		private Integer defisSeconds;
		public DictionaryHelper.SOURCE_TYPE sourceType;
		public boolean isAmPronunciation;
		public boolean isBrPronunciation;
		public boolean isRusTransled;
		public boolean isMultiRusTransled;
		public boolean isSort = true;

		public void setPrefix(String prefix) {
			this.prefix = StringUtils.isBlank(prefix) ? "" : prefix.trim()
					.toLowerCase() + "-";
		}

		public void setPauseSeconds(String secs) {
			pauseSeconds = StringUtils.isBlank(secs) ? AppProperties
					.getInstance().getDictionaryPauseSeconds() : Integer
					.valueOf(secs.trim());
		}

		public void setDefisSeconds(String secs) {
			defisSeconds = StringUtils.isBlank(secs) ? AppProperties
					.getInstance().getDictionaryDefisSeconds() : Integer
					.valueOf(secs.trim());
		}
	}

	public synchronized void createDictionaryFromHistory(DictionaryInput input)
			throws Exception {
		Map<String, String> words = HistoryHelper.INSTANCE.getWords();
		List<String> sortWords = new ArrayList<>();
		sortWords.addAll(words.keySet());
		if (input.isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, input.resultDir, input.isAmPronunciation,
				input.isBrPronunciation, input.isRusTransled,
				input.isMultiRusTransled, input.prefix, input.pauseSeconds,
				input.defisSeconds);
	}

	public synchronized void createDictionaryFromText(DictionaryInput input)
			throws Exception {
		String engText = readTextFromFile(input.path).replaceAll("[ ]+", " ")
				.trim().toLowerCase();
		List<String> sortWords = new ArrayList<>();
		Set<String> dublicate = new HashSet<>();
		for (String eng : engText.split("[^a-zA-Z]")) {
			String s = eng.trim();
			if (!dublicate.contains(s) && !StringUtils.isBlank(s)) {
				sortWords.add(s);
				dublicate.add(s);
			}
		}
		if (input.isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, input.resultDir, input.isAmPronunciation,
				input.isBrPronunciation, input.isRusTransled,
				input.isMultiRusTransled, input.prefix, input.pauseSeconds,
				input.defisSeconds);
	}

	public synchronized void createDictionaryFromDict(DictionaryInput input)
			throws Exception {
		String text = readTextFromFile(input.path);
		List<String> sortWords = new ArrayList<>();
		Map<String, String> dicMap = new HashMap<>();
		for (String st : text.split("[\n]")) {
			String[] ss = st.split("[=]");
			String key = ss[0].trim();
			String val = ss.length > 1 && !StringUtils.isBlank(ss[1].trim()) ? ss[1]
					.trim() : null;
			if (!dicMap.containsKey(key) && !StringUtils.isBlank(key)) {
				sortWords.add(key);
				dicMap.put(key, val);
			}
		}
		if (input.isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, input.resultDir, input.isAmPronunciation,
				input.isBrPronunciation, input.isRusTransled,
				input.isMultiRusTransled, dicMap, input.prefix,
				input.pauseSeconds, input.defisSeconds);
	}

	private String readTextFromFile(String textFilePath) throws IOException {
		Path path = Paths.get(new File(textFilePath).toURI());
		if (!path.toFile().exists()) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		long size = Files.copy(path, out);
		if (size == 0) {
			return null;
		}
		return new String(out.toByteArray(), "UTF-8");
	}

	private void writeTextToFile(String text, File filePath)
			throws FileNotFoundException, IOException {
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			out.write(text.getBytes("UTF-8"));
		}
	}

	private synchronized void createDictionary(List<String> sortWords,
			String resultDirPath, boolean isAmPronunciation,
			boolean isBrPronunciation, boolean isRusTransled,
			boolean isMultiRusTransled, String prefix, int pauseSeconds,
			int defisSeconds) throws IOException,
			UnsupportedAudioFileException, SoundException,
			SoundReceiverException {
		createDictionary(sortWords, resultDirPath, isAmPronunciation,
				isBrPronunciation, isRusTransled, isMultiRusTransled,
				Collections.emptyMap(), prefix, pauseSeconds, defisSeconds);
	}

	private synchronized void createDictionary(List<String> sortWords,
			String resultDirPath, boolean isAmPronunciation,
			boolean isBrPronunciation, boolean isRusTransled,
			boolean isMultiRusTransled, Map<String, String> dicMap,
			String prefix, int pauseSeconds, int defisSeconds)
			throws IOException, UnsupportedAudioFileException, SoundException,
			SoundReceiverException {
		File dicDir = new File(AppProperties.getInstance()
				.getDictionaryDirPath());
		if (!dicDir.exists()) {
			dicDir.mkdirs();
		}
		File resultDir = new File(resultDirPath);
		if (!resultDir.exists()) {
			resultDir.mkdirs();
		}
		Set<String> loadedEngSoundWords = loadSound(sortWords, dicDir);
		String words = wordsToString(sortWords, dicMap, loadedEngSoundWords, isRusTransled, true);
		writeTextToFile(words, new File(resultDir, prefix + "words.txt"));
		words = wordsToString(sortWords, dicMap, loadedEngSoundWords, isRusTransled, false);
		writeTextToFile(words, new File(resultDir, prefix + "words-sound.txt"));

		List<FileEntry> brFs = new ArrayList<>();
		List<FileEntry> amFs = new ArrayList<>();

		ProgressMonitorDemo progressMonitorDemo = ProgressMonitorDemo
				.createAndShowGUI("Generate sound files ...",
						loadedEngSoundWords.size());
		int i = 0;
		List<String> soundSortWords = new ArrayList<>(sortWords);
		soundSortWords.retainAll(loadedEngSoundWords);

		int limit = AppProperties.getInstance().getDictionaryBlockLimit();
		int fromIndex = 0;
		int toIndex = limit;
		try {
			while (fromIndex < soundSortWords.size()) {
				if (toIndex > soundSortWords.size()) {
					toIndex = soundSortWords.size();
				}
				List<String> partSortList = soundSortWords.subList(fromIndex,
						toIndex);
				for (String eng : partSortList) {
					File rusFile = null;
					if (isRusTransled) {
						String rus = dicMap.get(eng);
						if (StringUtils.isBlank(rus)) {
							rus = TranslationReceiver.INSTANCE
									.translateAndSimpleFormat(eng, false);
							if (!isMultiRusTransled) {
								rus = StringUtils.isBlank(rus) ? "" : rus
										.split("[;]")[0];
							}
						}
						rusFile = new File(
								GoogleSoundReceiver.INSTANCE.getFilePath(
										dicDir.getAbsolutePath(), rus, LANG.RUS));
						if (!rusFile.exists()) {
							GoogleSoundReceiver.INSTANCE.createSoundFile(
									dicDir, rus, LANG.RUS);
						}
					}
					File brDir = new File(dicDir, SoundReceiver.BR_SOUND_DIR);
					File amDir = new File(dicDir, SoundReceiver.AM_SOUND_DIR);
					File enDir = new File(dicDir, SoundReceiver.EN_SOUND_DIR);
					String engFileName = eng + ".mp3";
					if (isBrPronunciation) {
						File engFile = new File(brDir, engFileName);
						File sEngFile = new File(enDir, engFileName);
						if (engFile.exists()) {
							brFs.add(new FileEntry(engFile, null, rusFile));
						} else {
							brFs.add(new FileEntry(null, sEngFile, rusFile));
						}
					}
					if (isAmPronunciation) {
						File engFile = new File(amDir, engFileName);
						File sEngFile = new File(enDir, engFileName);
						if (engFile.exists()) {
							amFs.add(new FileEntry(engFile, null, rusFile));
						} else {
							amFs.add(new FileEntry(null, sEngFile, rusFile));
						}
					}
					progressMonitorDemo.nextProgress(i++);
					if (progressMonitorDemo.isCanceled()) {
						Thread.currentThread().stop();
					}
				}
				if (isBrPronunciation) {
					File outWaveFile = new File(resultDir, String.format(prefix
							+ "word-sound-br-%d-%d.wave", fromIndex, toIndex));
					SoundHelper.concatFiles(pauseSeconds, defisSeconds,
							outWaveFile, brFs);
				}
				if (isAmPronunciation) {
					File outWaveFile = new File(resultDir, String.format(prefix
							+ "word-sound-am-%d-%d.wave", fromIndex, toIndex));
					SoundHelper.concatFiles(pauseSeconds, defisSeconds,
							outWaveFile, amFs);
				}
				brFs.clear();
				amFs.clear();
				fromIndex = toIndex;
				toIndex = toIndex + limit;
			}
		} finally {
			progressMonitorDemo.close();
		}
	}

	public File findFile(boolean isBr, String targetDirPath, String word) {
		return isBr ? Paths.get(targetDirPath, SoundReceiver.BR_SOUND_DIR,
				word.concat(".mp3")).toFile() : Paths.get(targetDirPath,
				SoundReceiver.AM_SOUND_DIR, word.concat(".mp3")).toFile();
	}

	public String wordsToString(List<String> sortEngWords,
			Map<String, String> dicMap, Set<String> loadedSoundWords,
			boolean isRus, boolean isAllWords) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (String eng : sortEngWords) {
			if (!isAllWords && !loadedSoundWords.contains(eng)) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(eng);
			if (!loadedSoundWords.contains(eng)) {
				sb.append("~");
			} else {
				sb.append("=");
			}
			if (isRus) {
				sb.append(dicMap.containsKey(eng) ? dicMap.get(eng)
						: TranslationReceiver.INSTANCE
								.translateAndSimpleFormat(eng, false));
			}
		}
		return sb.toString();
	}

	public Set<String> loadSound(List<String> words, File dirFile)
			throws IOException {
		Set<String> loaded = new HashSet<>();
		ProgressMonitorDemo progressMonitorDemo = ProgressMonitorDemo
				.createAndShowGUI("Loading sound", words.size());
		try {
			int i = 0;
			for (String s : words) {
				try {
					if (soundReceiver.createSoundFile(dirFile, s)) {
						loaded.add(s);
					} else if (GoogleSoundReceiver.INSTANCE.createSoundFile(
							dirFile, s, LANG.ENG)) {
						loaded.add(s);
					}
					progressMonitorDemo.nextProgress(i++);
					if (progressMonitorDemo.isCanceled()) {
						Thread.currentThread().stop();
					}
				} catch (Exception ex) {
					logger.error(ex);
				}
			}
		} finally {
			progressMonitorDemo.close();
		}
		return loaded;
	}
}
