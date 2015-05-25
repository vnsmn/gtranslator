package gtranslator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;

public class DictionaryHelper {
	final public static DictionaryHelper INSTANCE = new DictionaryHelper();
	static final Logger logger = Logger.getLogger(DictionaryHelper.class);
	private OxfordSoundReceiver soundReceiver = new OxfordSoundReceiver();
	
	private DictionaryHelper() {};
	
	public synchronized void createDictionary(Map<String, String> words, String targetDirPath) throws FileNotFoundException, IOException {
		File dir = new File(targetDirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}		
		
		File soundDir = new File(dir, "sounds");
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
	}
	
	private String wordsToString(Map<String, String> words, Set<String> loadedSoundWords, boolean isAll) {
		TreeMap<String, String> sortMap = new TreeMap<>(new Comparator<String>() {
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
	
	private Set<String> loadSound(Map<String, String> words, File dirFile) throws IOException {
		Set<String> loaded = new HashSet<>();
		for (Entry<String, String> ent : words.entrySet()) {
			try {
				if (soundReceiver.writeSound(dirFile, ent.getKey())) {
					loaded.add(ent.getKey());
				}
			} catch (HttpStatusException ex) {
				logger.error(ex.getMessage());
			}
		}
		return loaded;
	}
	
	/*
	private boolean writeSound(File dirFile, String word) throws IOException {
		String request = "http://ssl.gstatic.com/dictionary/static/sounds/de/0/" + word + ".mp3";
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		conn.setUseCaches(false);
		File f = new File(dirFile, word + ".mp3");
		long size = 0;
		try {
			InputStream in = conn.getInputStream();
			size = Files.copy(in, Paths.get(f.toURI()), StandardCopyOption.REPLACE_EXISTING);		
			in.close();		
		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage());
		}
		return size > 0;
	}
	*/	
}
