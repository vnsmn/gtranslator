package gtranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class HistoryHelper {
	public interface StatisticListener {
		void execute(String message);
	}
	
	public final static HistoryHelper INSTANCE = new HistoryHelper();
	private Properties rawHis = new Properties();
	private Properties wordHis = new Properties();
	private StatisticListener statisticListener;

	private HistoryHelper() {}
	
	public void load(File rawHisFile, File wordHisFile) throws FileNotFoundException, IOException {
		if (rawHisFile.exists())
			try (FileInputStream in = new FileInputStream(rawHisFile)) {
				rawHis.loadFromXML(in);
			}		
		if (wordHisFile.exists())
			try (FileInputStream in = new FileInputStream(wordHisFile)) {
				wordHis.loadFromXML(in);
			}
	}
	
	public void writeRaw(String key, String value) {
		rawHis.put(toNormal(key), value);
		if (statisticListener != null) {
			statisticListener.execute(getStatistic());
		}
	}
	
	public   void writeWord(String key, String value) {		
		wordHis.put(toNormal(key), value);
		if (statisticListener != null) {
			statisticListener.execute(getStatistic());
		}
	}
	
	public String readRaw(String key) {
		return rawHis.getProperty(toNormal(key), null);
	}
	
	public void delete(String key) {
		String n = toNormal(key);
		rawHis.remove(n);
		wordHis.remove(n);
	}
	
	public void save(File rawHisFile, File wordHisFile) throws FileNotFoundException, IOException {
		if (!rawHisFile.exists()) {
			rawHisFile.createNewFile();
		}
		try (FileOutputStream out = new FileOutputStream(rawHisFile)) {
			rawHis.storeToXML(out, new Date().toString(), "UTF-8");
		}
		if (!wordHisFile.exists()) {
			wordHisFile.createNewFile();
		}
		try (FileOutputStream out = new FileOutputStream(wordHisFile)) {
			wordHis.storeToXML(out, new Date().toString(), "UTF-8");
		}
	}
	
	public String getStatistic() {		
		return "" + wordHis.size() + "/" + rawHis.size();
	}
	
	public Map<String, String> getWords() {
		Map<String, String> words = new HashMap<>();
		for (Entry<Object, Object> ent : wordHis.entrySet()) {
			words.put(ent.getKey().toString(), ent.getValue().toString());
		}
		return Collections.unmodifiableMap(words);
	}
	
	public void setStatisticListener(StatisticListener listener) {
		statisticListener = listener;
	}
	
	private String toNormal(String key) {
		String normal = key.trim();
		while (normal.indexOf("  ") != -1) {
			normal = normal.replaceAll("  ", " ");
		}
		return normal.toLowerCase();
	}
}