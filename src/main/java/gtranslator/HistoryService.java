package gtranslator;

import gtranslator.annotation.Singelton;
import gtranslator.translate.DefaultGoogleFormater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

public class HistoryService implements Configurable {
	static final Logger logger = Logger.getLogger(HistoryService.class);

	private static File rawHisFile;
	@Resource
	private H2Service h2Service;

	public interface StatisticListener {
		void execute(String message);
	}

	private Properties rawHis;
	private StatisticListener statisticListener;
	private int wordCount;
	private Set<String> runtimeWords = new HashSet<>();;

	@Singelton
	public static void createSingelton() {
		Registry.INSTANCE.add(new HistoryService());
	}

	private HistoryService() {
	}

	public void restore() throws FileNotFoundException, IOException {
		toDb(rawHis);
	}

	public void toDb(Properties properties) {
		for (Entry<Object, Object> ent : properties.entrySet()) {
			h2Service
					.addDic(ent.getKey().toString(), ent.getValue().toString());
		}
	}

	public void writeRaw(String key, String value) {
		String w = toNormal(key);
		h2Service.addDic(w, value);
		if (!rawHis.containsKey(w) && isWord(w)) {
			wordCount++;
		}
		rawHis.put(w, value);
		if (statisticListener != null) {
			statisticListener.execute(getStatistic());
		}
	}

	public String readRaw(String key) {
		String w = toNormal(key);
		if (isWord(w)) {
			runtimeWords.add(w);
		}
		if (statisticListener != null) {
			statisticListener.execute(getStatistic());
		}
		return h2Service.getDic(toNormal(key));
	}

	public void delete(String key) {
		String w = toNormal(key);
		h2Service.deleteDic(w);
		if (rawHis.containsKey(w) && isWord(w)) {
			wordCount--;
		}
		rawHis.remove(w);
		if (statisticListener != null) {
			statisticListener.execute(getStatistic());
		}
	}

	public String getStatistic() {
		loadHis();
		if (wordCount == 0) {
			wordCount = getWords().size();
		}
		return String.format("%d/%d/%d - runtime/word/phrase",
				runtimeWords.size(), wordCount, rawHis.size());
	}

	private boolean isWord(String phrase) {
		return toNormal(phrase).matches("[a-zA-Z]+");
	}

	public Map<String, String> getWords() {
		loadHis();
		Map<String, String> words = new HashMap<>();
		DefaultGoogleFormater formater = new DefaultGoogleFormater();
		String key = "";
		String val = "";
		for (Entry<Object, Object> ent : rawHis.entrySet()) {
			try {
				key = toNormal(ent.getKey().toString());
				val = toNormal(ent.getValue().toString());
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

	@Override
	public void init(AppProperties appProperties) {
		rawHisFile = Paths.get(
				AppProperties.getInstance().getDictionaryDirPath(), "db",
				"gtranslator-history-backup.xml").toFile();
	}

	@Override
	public void close() {
		try {
			if (!rawHisFile.exists()) {
				rawHisFile.createNewFile();
			}
			try (FileOutputStream out = new FileOutputStream(rawHisFile)) {
				rawHis.storeToXML(out, new Date().toString(), "UTF-8");
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	private void loadHis() {
		if (rawHis == null) {
			rawHis = new Properties();
			rawHis.putAll(h2Service.getsDic());
		}
		if (rawHisFile.exists()) {
			try (FileInputStream in = new FileInputStream(rawHisFile)) {
				rawHis.loadFromXML(in);
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
	}
}