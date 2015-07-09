package gtranslator;

import gtranslator.translate.DefaultGoogleFormater;
import gtranslator.translate.TranslationReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

public class HistoryHelper {
	static final Logger logger = Logger.getLogger(HistoryHelper.class);

	private static File rawHisFile;
	static {
		String dir = System.getProperty("user.home");
		rawHisFile = new File(dir, "gtranslator-history.xml");
	}

	public interface StatisticListener {
		void execute(String message);
	}

	public final static HistoryHelper INSTANCE = new HistoryHelper();
	private Properties rawHis = new Properties();
	private StatisticListener statisticListener;

	private HistoryHelper() {
	}

	public void load() throws FileNotFoundException, IOException {
		Map<String, String> dics = H2Helper.INSTANCE.getsDic();
		rawHis.putAll(dics);
		if (rawHisFile.exists()) {
			try (FileInputStream in = new FileInputStream(rawHisFile)) {
				rawHis.loadFromXML(in);
			}
		}
		merge();
	}

	private void merge() {
		for (Entry<Object, Object> ent : rawHis.entrySet()) {
			H2Helper.INSTANCE.addDic(ent.getKey().toString(), ent.getValue()
					.toString());
		}
	}

	public void writeRaw(String key, String value) {
		rawHis.put(toNormal(key), value);
		H2Helper.INSTANCE.addDic(key, value);
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
		H2Helper.INSTANCE.deleteDic(key);
		// wordHis.remove(n);
	}

	public void save() throws FileNotFoundException, IOException {
		if (!rawHisFile.exists()) {
			rawHisFile.createNewFile();
		}
		try (FileOutputStream out = new FileOutputStream(rawHisFile)) {
			rawHis.storeToXML(out, new Date().toString(), "UTF-8");
		}
		H2Helper.INSTANCE.close();
	}

	public String getStatistic() {
		return "word/phrase: " + getWords().size() + "/" + rawHis.size();
	}

	public Map<String, String> getWords() {
		Map<String, String> words = new HashMap<>();
		DefaultGoogleFormater formater = new DefaultGoogleFormater();
		String key = "";
		String val = "";
		for (Entry<Object, Object> ent : rawHis.entrySet()) {
			try {
				key = toNormal((String) ent.getKey());
				val = toNormal((String) ent.getValue());
				if (key.matches("[a-zA-Z]+")) {
					formater.format(val, true);
					String s = formater.formatSimple(val);
					words.put(key, s);
				}
			} catch (Exception ex) {
				logger.error(key, ex);
				throw ex;
			}
		}
		return Collections.unmodifiableMap(words);
	}

	public void setStatisticListener(StatisticListener listener) {
		statisticListener = listener;
	}

	private String toNormal(String key) {
		return key.trim().replaceAll("[ ]+", " ").toLowerCase();
	}
}