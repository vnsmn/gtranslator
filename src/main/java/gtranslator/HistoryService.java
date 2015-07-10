package gtranslator;

import gtranslator.annotation.Singelton;
import gtranslator.translate.DefaultGoogleFormater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

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

	@Singelton
	public static void createSingelton() {
		Registry.INSTANCE.add(new HistoryService());
	}

	private HistoryService() {
	}

	public void toDb(Properties properties) {
		for (Entry<Object, Object> ent : properties.entrySet()) {
			h2Service
					.addDic(ent.getKey().toString(), ent.getValue().toString());
		}
	}

	public void writeRaw(String key, String value) {
		h2Service.addDic(toNormal(key), value);
		rawHis.put(toNormal(key), value);
		if (statisticListener != null) {
			statisticListener.execute(getStatistic());
		}
	}

	public String readRaw(String key) {
		return h2Service.getDic(toNormal(key));
	}

	public void delete(String key) {
		h2Service.deleteDic(toNormal(key));
		rawHis.remove(toNormal(key));
	}

	public String getStatistic() {
		loadHis();
		return "word/phrase: " + getWords().size() + "/" + rawHis.size();
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
	}
}