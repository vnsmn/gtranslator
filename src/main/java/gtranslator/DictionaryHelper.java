package gtranslator;

import gtranslator.exception.SoundReceiverException;
import gtranslator.sound.OxfordSoundReceiver;
import gtranslator.sound.RusGoogleSoundReceiver;
import gtranslator.sound.SoundHelper;
import gtranslator.sound.SoundHelper.FileEntry;
import gtranslator.sound.SoundHelper.SoundException;
import gtranslator.sound.SoundReceiver;
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

	public synchronized void createDictionaryFromHistory(String resultDir,
			boolean isAmPronunciation, boolean isBrPronunciation,
			boolean isRusTransled, boolean isMultiRusTransled, boolean isSort)
			throws Exception {
		Map<String, String> words = HistoryHelper.INSTANCE.getWords();
		List<String> sortWords = new ArrayList<>();
		sortWords.addAll(words.keySet());
		if (isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, resultDir, isAmPronunciation,
				isBrPronunciation, isRusTransled, isMultiRusTransled);
	}

	public synchronized void createDictionaryFromText(String textFilePath,
			String resultDir, boolean isAmPronunciation,
			boolean isBrPronunciation, boolean isRusTransled,
			boolean isMultiRusTransled, boolean isSort) throws Exception {
		String engText = readTextFromFile(textFilePath).replaceAll("[ ]+", " ")
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
		if (isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, resultDir, isAmPronunciation,
				isBrPronunciation, isRusTransled, isMultiRusTransled);
	}

	public synchronized void createDictionaryFromDict(String dicFilePath,
			String resultDir, boolean isAmPronunciation,
			boolean isBrPronunciation, boolean isRusTransled,
			boolean isMultiRusTransled, boolean isSort) throws Exception {
		String text = readTextFromFile(dicFilePath);
		List<String> sortWords = new ArrayList<>();
		Set<String> dublicate = new HashSet<>();
		for (String st : text.split("[\n]")) {
			String[] ss = st.split("[=]");
			String s = ss[0].trim();
			if (!dublicate.contains(s) && !StringUtils.isBlank(s)) {
				sortWords.add(s);
				dublicate.add(s);
			}
		}
		if (isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, resultDir, isAmPronunciation,
				isBrPronunciation, isRusTransled, isMultiRusTransled);
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
			boolean isMultiRusTransled) throws IOException,
			UnsupportedAudioFileException, SoundException,
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
		String words = wordsToString(sortWords, loadedEngSoundWords, true);
		writeTextToFile(words, new File(resultDir, "words.txt"));
		words = wordsToString(sortWords, loadedEngSoundWords, false);
		writeTextToFile(words, new File(resultDir, "words-sound.txt"));

		List<FileEntry> brFs = new ArrayList<>();
		List<FileEntry> amFs = new ArrayList<>();

		ProgressMonitorDemo progressMonitorDemo = ProgressMonitorDemo
				.createAndShowGUI("Generate sound files ...",
						loadedEngSoundWords.size());
		int i = 0;
		List soundSortWords = new ArrayList<>(sortWords);
		soundSortWords.retainAll(loadedEngSoundWords);
		int seconds = AppProperties.getInstance().getDictionaryPauseSeconds();
		int secondsDefis = AppProperties.getInstance()
				.getDictionaryDefisSeconds();
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
						String rus = TranslationReceiver.INSTANCE
								.translateAndSimpleFormat(eng, false);
						if (!isMultiRusTransled) {
							rus = StringUtils.isBlank(rus) ? "" : rus
									.split("[;]")[0];
						}
						rusFile = new File(
								RusGoogleSoundReceiver.INSTANCE.getFilePath(
										dicDir.getAbsolutePath(), rus));
						if (!rusFile.exists()) {
							RusGoogleSoundReceiver.INSTANCE.createSoundFile(
									dicDir, rus);
						}
					}
					File brDir = new File(dicDir, SoundReceiver.BR_SOUND_DIR);
					File amDir = new File(dicDir, SoundReceiver.AM_SOUND_DIR);
					String engFileName = eng + ".mp3";
					if (isBrPronunciation) {
						brFs.add(new FileEntry(new File(brDir, engFileName),
								rusFile));
					}
					if (isAmPronunciation) {
						amFs.add(new FileEntry(new File(amDir, engFileName),
								rusFile));
					}
					progressMonitorDemo.nextProgress(i++);
					if (progressMonitorDemo.isCanceled()) {
						Thread.currentThread().stop();
					}
				}
				if (isBrPronunciation) {
					File outWaveFile = new File(resultDir, String.format(
							"word-sound-br-%d-%d.wave", fromIndex, toIndex));
					SoundHelper.concatFiles(seconds, secondsDefis, outWaveFile,
							brFs);
				}
				if (isAmPronunciation) {
					File outWaveFile = new File(resultDir, String.format(
							"word-sound-am-%d-%d.wave", fromIndex, toIndex));
					SoundHelper.concatFiles(seconds, secondsDefis, outWaveFile,
							amFs);
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
			Set<String> loadedSoundWords, boolean isAllWords)
			throws IOException {
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
			sb.append(TranslationReceiver.INSTANCE.translateAndSimpleFormat(
					eng, false));
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
