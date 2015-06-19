package gtranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class AppProperties {
	private static final Logger logger = Logger.getLogger(AppProperties.class);
	private static AppProperties instance;
	private Properties properties;

	private static final String DICTIONARY_TARGET_DIR = "dictionary.target.dir";
	private static final String DICTIONARY_PAUSE_SECONDS = "dictionary.pause.seconds";
	private static final String DICTIONARY_DEFIS_SECONDS = "dictionary.defis.seconds";
	private static final String DICTIONARY_BLOCK_LIMIT = "dictionary.block.limit";
	private static final String HISTORY = "history";
	private static final String COOKIE = "cookie";

	private AppProperties() {
	}

	public static AppProperties getInstance() {
		if (instance == null) {
			instance = new AppProperties();
		}
		return instance;
	}

	public String getDictionaryDirPath() {
		String dirPath = properties.getProperty(DICTIONARY_TARGET_DIR);
		if (StringUtils.isBlank(dirPath)) {
			dirPath = System.getProperty("user.home")
					+ "/gtranslator-dictionary";
			setDictionaryDirPath(dirPath);
		}
		return dirPath;
	}

	public void setDictionaryDirPath(String s) {
		properties.setProperty(DICTIONARY_TARGET_DIR, s);
	}

	public boolean isHistory() {
		String s = properties.getProperty(HISTORY);
		try {
			return parseBoolean(s);
		} catch (java.text.ParseException ex) {
			logger.error(ex);
			return true;
		}
	}

	public void setHistory(boolean b) {
		properties.put(HISTORY, Boolean.toString(b));
	}

	public int getDictionaryPauseSeconds() {
		String s = properties.getProperty(DICTIONARY_PAUSE_SECONDS, "")
				.replaceAll("\n", "");
		return StringUtils.isBlank(s) ? 1 : Integer.parseInt(s);
	}

	public void setDictionaryPauseSeconds(int i) {
		properties.setProperty(DICTIONARY_PAUSE_SECONDS, Integer.toString(i));
	}

	public int getDictionaryDefisSeconds() {
		String s = properties.getProperty(DICTIONARY_DEFIS_SECONDS, "")
				.replaceAll("\n", "");
		return StringUtils.isBlank(s) ? 1 : Integer.parseInt(s);
	}

	public void setDictionaryDefisSeconds(int i) {
		properties.setProperty(DICTIONARY_DEFIS_SECONDS, Integer.toString(i));
	}

	public int getDictionaryBlockLimit() {
		String s = properties.getProperty(DICTIONARY_BLOCK_LIMIT, "")
				.replaceAll("\n", "");
		return StringUtils.isBlank(s) ? 10 : Integer.parseInt(s);
	}

	public void setDictionaryBlockLimit(int i) {
		properties.setProperty(DICTIONARY_BLOCK_LIMIT, Integer.toString(i));
	}

	public String getCookie() {
		return properties.getProperty(COOKIE, "").replaceAll("\n", "");
	}

	public void setCookie(String s) {
		properties.setProperty(COOKIE, s);
	}

	public void save() {
		throw new java.lang.UnsupportedOperationException();
	}

	public void load(String propPath) throws IOException, ParseException {
		properties = new Properties();
		if (StringUtils.isBlank(propPath)) {
			propPath = System.getProperty("user.dir");
		}
		Properties defProps = new Properties();
		try (InputStream in = App.class.getClassLoader().getResourceAsStream(
				"settings.xml")) {
			defProps.loadFromXML(in);
		}
		if (!StringUtils.isBlank(propPath)) {
			File f = new File(propPath);
			if (f.isDirectory()) {
				f = new File(f, "settings.xml");
			}
			try (FileInputStream fis = new FileInputStream(f)) {
				properties.loadFromXML(fis);
			}
		}
		for (Entry<Object, Object> ent : defProps.entrySet()) {
			properties.putIfAbsent(ent.getKey(), ent.getValue());
		}
	}

	private boolean parseBoolean(String s) throws java.text.ParseException {
		if (!StringUtils.isBlank(s)
				&& !s.toLowerCase().matches("(y|yes|true|on|n|no|false|off)")) {
			throw new java.text.ParseException(s, 0);
		}
		return StringUtils.isBlank(s) ? false : s.toLowerCase().matches(
				"(y|yes|true|on)");
	}
}
