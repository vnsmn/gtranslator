package gtranslator;

import gtranslator.annotation.Singelton;
import gtranslator.exception.SoundReceiverException;
import gtranslator.sound.OxfordReceiverService;
import gtranslator.sound.SoundHelper;
import gtranslator.sound.SoundHelper.FileEntry;
import gtranslator.translate.TranslationService;
import gtranslator.ui.Constants.PHONETICS;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class DictionaryService implements Configurable {
	static final Logger logger = Logger.getLogger(DictionaryService.class);
	@Resource
	private TranslationService translationService;
	@Resource
	private OxfordReceiverService oxfordReceiverService;
	@Resource
	private HistoryService historyService;

	public enum SOURCE_TYPE {
		HISTORY, DICTIONARY, TEXT, IRREGULAR_VERB, RUNTIME_WORDS
	}

	private DictionaryService() {
	};

	@Singelton
	public static void createSingelton() {
		Registry.INSTANCE.add(new DictionaryService());
	}

	public static class DictionaryInput {
		public String path;
		public String resultDir;
		private String prefix;
		private Integer pauseSeconds;
		private Integer defisSeconds;
		public DictionaryService.SOURCE_TYPE sourceType;
		public PHONETICS phonetic;
		public Collection<String> runtimeWords;
		public boolean isRusTransled;
		public boolean isMultiRusTransled;
		public boolean isSort = true;
		public boolean isPhonetics;
		public boolean isFirstEng = true;

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
		Map<String, String> words = historyService.getWords();
		List<String> sortWords = new ArrayList<>();
		sortWords.addAll(words.keySet());
		if (input.isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, Collections.emptyMap(), input);
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
		createDictionary(sortWords, Collections.emptyMap(), input);
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
		createDictionary(sortWords, dicMap, input);
	}

	public synchronized void createDictionaryFromIrregularVerb(
			DictionaryInput input) throws Exception {
		String text = readTextFromFile(input.path);
		List<String> sortWords = new ArrayList<>();
		Map<String, String> dicMap = new HashMap<>();
		for (String st : text.split("[\n]")) {
			String[] ss = st.split("[=]");
			String[] keys = ss[0].trim().split("[,]");
			String val = ss.length > 1 && !StringUtils.isBlank(ss[1].trim()) ? ss[1]
					.trim() : null;
			boolean b = true;
			for (String key : keys) {
				String w = translationService.toNormal(key);
				if (!StringUtils.isBlank(w)) {
					sortWords.add(w);
					if (!dicMap.containsKey(w)) {
						dicMap.put(w, b ? val : ";");
					}
					b = false;
				}
			}
		}
		createDictionary(sortWords, dicMap, input);
	}

	public synchronized void createDictionaryFromRuntimeWords(
			DictionaryInput input) throws Exception {
		List<String> sortWords = new ArrayList<>(input.runtimeWords);
		if (input.isSort) {
			Collections.sort(sortWords);
		}
		createDictionary(sortWords, Collections.emptyMap(), input);
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
			Map<String, String> dicMap, DictionaryInput input)
			throws IOException, UnsupportedAudioFileException,
			SoundReceiverException {
		String resultDirPath = input.resultDir;
		PHONETICS phonetic = input.phonetic;
		boolean isRusTransled = input.isRusTransled;
		boolean isMultiRusTransled = input.isMultiRusTransled;
		String prefix = input.prefix;
		int pauseSeconds = input.pauseSeconds;
		int defisSeconds = input.defisSeconds;
		boolean isPhonetics = input.isPhonetics;
		boolean isFirstEng = input.isFirstEng;

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
		String subPrefix = input.isRusTransled ? "dic-ru" : "dic";
		if (StringUtils.isBlank(prefix)) {
			prefix = subPrefix;
		} else {
			if (!prefix.endsWith("-")) {
				prefix += "-";
			}
			prefix += subPrefix;
		}
		String words = wordsToString(sortWords, dicMap, loadedEngSoundWords,
				isRusTransled, true, phonetic, isPhonetics, isFirstEng);
		writeTextToFile(words, new File(resultDir, prefix + "-"
				+ phonetic.name().toLowerCase() + ".txt"));
		words = wordsToString(sortWords, dicMap, loadedEngSoundWords,
				isRusTransled, false, phonetic, isPhonetics, isFirstEng);
		writeTextToFile(words, new File(resultDir, prefix + "-"
				+ phonetic.name().toLowerCase() + ".txt"));
		writeTextToFile(wordsToString(sortWords), new File(resultDir, prefix
				+ "-blank.txt"));

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
							rus = translationService.translateAndSimpleFormat(
									eng, false);
							if (!isMultiRusTransled) {
								rus = StringUtils.isBlank(rus) ? "" : rus
										.split("[;]")[0];
							}
						}
						if (!";".equals(rus.trim())) {
							rusFile = SoundHelper.getRusFile(rus);
						}
					}
					File engFile = SoundHelper.getEngFile(eng, phonetic);
					if (PHONETICS.BR == phonetic) {
						if (isFirstEng) {
							brFs.add(new FileEntry(engFile, rusFile));
						} else {
							brFs.add(new FileEntry(rusFile, engFile));
						}
					}
					if (PHONETICS.AM == phonetic) {
						if (isFirstEng) {
							amFs.add(new FileEntry(engFile, rusFile));
						} else {
							amFs.add(new FileEntry(rusFile, engFile));
						}
					}
					progressMonitorDemo.nextProgress(i++);
					if (progressMonitorDemo.isCanceled()) {
						Thread.currentThread().stop();
					}
				}
				if (PHONETICS.BR == phonetic) {
					File outWaveFile = new File(resultDir, String.format(prefix
							+ "-br-%d-%d.wave", fromIndex, toIndex));
					SoundHelper.concatFiles(pauseSeconds, defisSeconds,
							outWaveFile, brFs);
				}
				if (PHONETICS.AM == phonetic) {
					File outWaveFile = new File(resultDir, String.format(prefix
							+ "-am-%d-%d.wave", fromIndex, toIndex));
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

	public String wordsToString(List<String> sortEngWords) {
		StringBuilder sb = new StringBuilder();
		for (String eng : sortEngWords) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(eng);
			sb.append("=");
		}
		return sb.toString();
	}

	public String wordsToString(List<String> sortEngWords,
			Map<String, String> dicMap, Set<String> loadedSoundWords,
			boolean isRusTransled, boolean isAllWords, PHONETICS phonetic,
			boolean isPhonetics, boolean isFirstEng) throws IOException {
		ProgressMonitorDemo progressMonitorDemo = ProgressMonitorDemo
				.createAndShowGUI("Words to string", sortEngWords.size());
		int i = 0;
		StringBuffer sb = new StringBuffer();
		try {
			for (String eng : sortEngWords) {
				if (!isAllWords && !loadedSoundWords.contains(eng)) {
					continue;
				}
				if (sb.length() > 0) {
					sb.append("\n");
				}
				String rus = null;
				String phon = "";
				if (isPhonetics) {
					if (PHONETICS.AM == phonetic
							&& !StringUtils
									.isBlank(phon = oxfordReceiverService
											.getPhonetic(eng, PHONETICS.AM))) {
						phon = "[" + phon + "]";
					}
					if (PHONETICS.BR == phonetic
							&& !StringUtils
									.isBlank(phon = oxfordReceiverService
											.getPhonetic(eng, PHONETICS.BR))) {
						phon = "[" + phon + "]";
					}
				}
				if (isRusTransled) {
					rus = dicMap.containsKey(eng)
							&& !StringUtils.isBlank(dicMap.get(eng)) ? dicMap
							.get(eng) : translationService
							.translateAndSimpleFormat(eng, false);
				}
				sb.append(isFirstEng ? eng + phon : StringUtils
						.trimToEmpty(rus));
				sb.append(!loadedSoundWords.contains(eng) ? "~" : "=");
				sb.append(isFirstEng ? StringUtils.trimToEmpty(rus) : eng
						+ phon);
				progressMonitorDemo.nextProgress(i++);
				if (progressMonitorDemo.isCanceled()) {
					Thread.currentThread().stop();
				}
			}
		} finally {
			progressMonitorDemo.close();
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
				File f = SoundHelper.getEngFile(s, AppProperties.getInstance()
						.getDictionaryPhonetic());
				if (f != null) {
					loaded.add(s);
				}
				progressMonitorDemo.nextProgress(i++);
				if (progressMonitorDemo.isCanceled()) {
					Thread.currentThread().stop();
				}
			}
		} finally {
			progressMonitorDemo.close();
		}
		return loaded;
	}

	@Override
	public void init(AppProperties appProperties) {
	}

	@Override
	public void close() {
	}
}
