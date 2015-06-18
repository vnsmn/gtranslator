package gtranslator.translate;

import gtranslator.DictionaryHelper;
import gtranslator.HistoryHelper;
import gtranslator.sound.RusGoogleSoundReceiver;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.StringUtils;

public class BatchTranslationHelper {
	
	public static final BatchTranslationHelper INSTANCE = new BatchTranslationHelper();
	
	@SuppressWarnings("unchecked")
	public void execute(String textFilePath,
			String dicDirPath, String targetSoundFileName, int blockLimit, 
			int seconds, int secondsDefis, 
			boolean isAll, boolean doLoadSound, boolean isRus) throws IOException, UnsupportedAudioFileException, SoundException {
		
		HistoryHelper.INSTANCE.load();
		
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
						String rus = TranslationReceiver.INSTANCE.translateAndFormat(eng, false);
						if (doLoadSound) {
							RusGoogleSoundReceiver.INSTANCE.createSoundFile(new File(dicDirPath), rus);
						}
						rus = TranslationReceiver.INSTANCE.formatSimpleFromHistory(eng);
						words.put(eng, rus);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					dublicates.add(eng);
				}
				String t = HistoryHelper.INSTANCE.readRaw(eng);
				if (StringUtils.isBlank(t)) {
					HistoryHelper.INSTANCE.writeRaw(eng, "[[[\"\",\"\",\"\",\"\"]],,\"en\",,,,,,,12]");
				}				
			}
		}
		HistoryHelper.INSTANCE.save();

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
		
		String outWaveFile =  new File(new File(textFilePath).getParentFile(), targetSoundFileName + "-" + seconds + "-" + secondsDefis).getAbsolutePath();  
		int n = 1;
		TreeMap<File, File> mp3Files = new TreeMap<>(new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.compareTo(f2);
			}
		});
		List<String> sortList = new ArrayList<>(loadedSoundWords);
		sortList.sort(null);
		for (String s : sortList) {
			File engFile = Paths.get(dicDirPath, SoundReceiver.BR_SOUND_DIR, s.concat(".mp3")).toFile();			
			String rus = words.get(s);
			File rusFile = new File(RusGoogleSoundReceiver.INSTANCE.getFilePath(dicDirPath, rus));
			if (isRus && rusFile.exists()) {
				mp3Files.put(engFile, rusFile);
			} else {
				mp3Files.put(engFile, null);
			}
			if (mp3Files.size() == blockLimit) {
				SoundHelper.concatFiles(seconds, secondsDefis, new File(outWaveFile.concat("-" + n).concat(".wave")), mp3Files); 
				mp3Files.clear();
				n++;
			}
		}
		if (!mp3Files.isEmpty()) {				
			SoundHelper.concatFiles(seconds, secondsDefis, new File(outWaveFile.concat("-" + n++).concat(".wave")), mp3Files);
		}
	}

	public static void main(String... args) throws IOException, UnsupportedAudioFileException, SoundException {
		String dicDirPath = "/home/vns/gtranslator-dictionary";
		String textFilePath = "/ext/english/learningenglish.voanews.com/LinkedIn EF Offer Test Scores for English Learners/words.txt";
		String targetSoundFileName = "words-sound";
		int seconds = 1;
		int secondsDefis = 1;
		int blockLimit = 50;
		boolean isAllTranslated = false;
		boolean doLoadSound = false;
		boolean isRus = false;		
		BatchTranslationHelper.INSTANCE.execute(textFilePath, dicDirPath, targetSoundFileName, blockLimit, 
				seconds, secondsDefis, isAllTranslated, doLoadSound, isRus);
	}
	
	public static void main1(String... args) throws IOException, UnsupportedAudioFileException, SoundException {
		String dicDirPath = "/home/vns/gtranslator-dictionary";
		String textFilePath = "/ext/english/learningenglish.voanews.com/LinkedIn EF Offer Test Scores for English Learners/words.txt";
		String targetSoundFileName = "words-sound-ru";
		int seconds = 1;
		int secondsDefis = 1;
		int blockLimit = 50;
		boolean isAllTranslated = false;
		boolean doLoadSound = false;
		boolean isRus = true;		
		BatchTranslationHelper.INSTANCE.execute(textFilePath, dicDirPath, targetSoundFileName, blockLimit, 
				seconds, secondsDefis, isAllTranslated, doLoadSound, isRus);
	}
}