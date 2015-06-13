package gtranslator.translate;

import gtranslator.DictionaryHelper;
import gtranslator.HistoryHelper;
import gtranslator.sound.SoundHelper;
import gtranslator.sound.SoundHelper.SoundException;
import gtranslator.sound.SoundReceiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.StringUtils;

public class BatchTranslationHelper {
	
	private static File rawHisFile;
	private static File wordHisFile;
	static {
		String dir = System.getProperty("user.home");
		rawHisFile = new File(dir, "gtranslator-raw-batch-his.xml");
		wordHisFile = new File(dir, "gtranslator-word-batch-his.xml");
	}
	
	public static final BatchTranslationHelper INSTANCE = new BatchTranslationHelper();
	
	@SuppressWarnings("unchecked")
	public void execute(String textFilePath,
			String dicDirPath, int blockLimit, int seconds, boolean isAll, boolean doLoadSound) throws IOException, UnsupportedAudioFileException, SoundException {
		
		HistoryHelper.INSTANCE.load(rawHisFile, wordHisFile);
		
		Path path = Paths.get(new File(textFilePath).toURI());
		if (!path.toFile().exists()) {
			return;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		long size = Files.copy(path, out);
		if (size == 0) {
			return;
		}
		String text = new String(out.toByteArray(), "UTF-8")
				.replaceAll("[ ]+", " ").trim().toLowerCase();
		String[] ss = text.split("[^a-zA-Z]");
		Map<String, String> words = new HashMap<String, String>();
		Set<String> dublicates = new HashSet<>();
		TranslationReceiver.INSTANCE.setAddition(false);
		TranslationReceiver.INSTANCE.setHistory(true);
		for (String s : ss) {
			String eng = s.matches("[a-zA-Z]+") ? TranslationReceiver.INSTANCE.toNormal(s) : "";			
			if (eng.length() > 1 && eng.matches("[a-zA-Z]+")) {
				if (!dublicates.contains(eng)) {
					try {
						words.put(eng, TranslationReceiver.INSTANCE.translateAndFormat(eng, false));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					dublicates.add(eng);
				}
				String t = HistoryHelper.INSTANCE.readRaw(eng);
				if (StringUtils.isBlank(t)) {
					HistoryHelper.INSTANCE.writeWord(eng, eng);
				}				
			}
		}
		HistoryHelper.INSTANCE.save(rawHisFile, wordHisFile);

		Set<String> loadedSoundWords; 
		if (doLoadSound) {
			loadedSoundWords = DictionaryHelper.INSTANCE.loadSound(
				words, new File(dicDirPath));
		} else  {
			loadedSoundWords = new HashSet<>();
			for (String eng : dublicates) {
				File f = DictionaryHelper.INSTANCE.findFile(true, dicDirPath, eng);
				if (f.exists()) {
					loadedSoundWords.add(eng);
				}
			}
		}
		
		String transText = DictionaryHelper.INSTANCE.wordsToString(words,
				loadedSoundWords, isAll);

		Path trgPath = Paths.get(textFilePath.concat(".tsl.txt"));
		Files.copy(new ByteArrayInputStream(transText.getBytes("UTF-8")), trgPath, 
				StandardCopyOption.REPLACE_EXISTING.REPLACE_EXISTING);
		
		String outWaveFile =  new File(new File(textFilePath).getParentFile(), "words-sound" + "-" + seconds).getAbsolutePath();  
		int n = 1;
		List<File> mp3Files = new ArrayList<>();
		List<String> sortList = new ArrayList<>(loadedSoundWords);
		sortList.sort(null);
		for (String s : sortList) {
			mp3Files.add(Paths.get(dicDirPath, SoundReceiver.BR_SOUND_DIR, s.concat(".mp3")).toFile());
			if (mp3Files.size() == blockLimit) {				
				SoundHelper.concatFiles(seconds, new File(outWaveFile.concat("-" + n).concat(".wave")), mp3Files); 
				mp3Files.clear();
				n++;
			}
		}
		if (!mp3Files.isEmpty()) {				
			SoundHelper.concatFiles(seconds, new File(outWaveFile.concat("-" + n++).concat(".wave")), mp3Files);
		}
	}
	
	public static void main(String... args) throws IOException, UnsupportedAudioFileException, SoundException {
		String dicDirPath = "~/gtranslator-dictionary";
		String textFilePath = "/ext/english/learningenglish.voanews.com/LinkedIn EF Offer Test Scores for English Learners/words.txt";
		int seconds = 0;
		int blockLimit = 50;
		boolean isAll = true;
		boolean doLoadSound = false;
		BatchTranslationHelper.INSTANCE.execute(textFilePath, dicDirPath, blockLimit, seconds, isAll, doLoadSound);
	}
}