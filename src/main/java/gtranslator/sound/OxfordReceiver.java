package gtranslator.sound;

import gtranslator.AppProperties;
import gtranslator.DictionaryHelper;
import gtranslator.ui.Constants;
import gtranslator.ui.Constants.PHONETICS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonParsingException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class OxfordReceiver {
	static final Logger logger = Logger.getLogger(DictionaryHelper.class);
	public final static OxfordReceiver INSTANCE = new OxfordReceiver();
	private static final String REQUEST = "http://www.oxfordlearnersdictionaries.com/definition/english/%s_1?q=%s";
	private File phonFile;
	private Properties phonProperties = new Properties();

	private OxfordReceiver() {
		String dir = System.getProperty("user.home");
		phonFile = new File(dir, "gtranslator-oxford.xml");
		if (phonFile.exists())
			try (FileInputStream in = new FileInputStream(phonFile)) {
				phonProperties.loadFromXML(in);
			} catch (Exception ex) {
				logger.error(ex);
			}
	}

	public synchronized String getPhonetic(String word, PHONETICS phon) {
		String normal = toNormal(word);
		receive(normal);
		Phons phons = createPhons(word);
		Set<String> setOfPhon = phons.get(phon.name());
		StringBuilder sb = new StringBuilder();
		for (String s : setOfPhon) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public synchronized File getSound(String word, PHONETICS ph) {
		PHONETIC phon = PHONETIC.valueOf(ph.name());
		String normal = toNormal(word);
		File dir = new File(AppProperties.getInstance().getDictionaryDirPath(),
				phon.soundSubDir);
		File f = new File(dir, normal + ".mp3");
		if (!f.exists()) {
			receive(normal);
		}
		return f.exists() ? f : null;
	}

	private void capture(String captureWord, String request)
			throws IOException, URISyntaxException {
		Document doc = Jsoup.connect(request).timeout(3000).get();
		for (PHONETIC phon : PHONETIC.values()) {
			File dir = new File(AppProperties.getInstance()
					.getDictionaryDirPath(), phon.soundSubDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			Set<String> setOfPhon = new LinkedHashSet<>();
			Elements elements = doc.select(phon.css);
			if (elements.size() > 0) {
				for (Element el : elements) {
					Pattern pattern = Pattern.compile(phon.pattern);
					Matcher matcher = pattern.matcher(el.text());
					if (matcher.find()) {
						Elements els = el.parent().select(phon.soundCss);
						if (els.size() > 0) {
							String url = els.first().attr("data-src-mp3");
							URI uri = new URI(url);
							String name = new File(uri.toURL().getFile())
									.getName();
							String word = name.substring(0, name.indexOf("_"));
							File f = new File(dir, word.concat(".mp3"));
							if (!f.exists()) {
								captureSoundFile(f, word, url);
							}
							String nextPhon = StringUtils.stripToEmpty(matcher
									.group(1));
							if (!captureWord.equals(word)) {
								Phons ph = createPhons(word);
								ph.get(phon.name()).add(nextPhon);
								phonProperties.put(word, ph.toJson());
							}
							setOfPhon.add(nextPhon);
						}
					}
				}
			}
			Phons ph = createPhons(captureWord);
			ph.get(phon.name()).addAll(setOfPhon);
			phonProperties.put(captureWord, ph.toJson());
		}
	}

	private boolean captureSoundFile(File file, String word, String request)
			throws IOException {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage());
		}
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(15000);
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		conn.setUseCaches(false);
		long size = 0;
		try (InputStream in = conn.getInputStream()) {
			size = Files.copy(in, Paths.get(file.toURI()),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception ex) {
			logger.error(ex.getMessage() + ". URL: ".concat(request));
		}
		return size > 0;
	}

	private Phons createPhons(String word) {
		String prevPhon = StringUtils.trimToEmpty(phonProperties
				.getProperty(word));
		Phons phones = new Phons();
		phones.load(prevPhon);
		return phones;
	}

	private boolean isLoaded(String word) {
		return phonProperties.containsKey(word)
				&& !phonProperties.getProperty(word).startsWith("error:");
	}

	private void receive(String word) {
		if (isLoaded(word)) {
			return;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage());
		}
		try {
			capture(word, String.format(REQUEST, word, word));
			save();
		} catch (org.jsoup.HttpStatusException ex) {
			phonProperties.put(word, ex.getStatusCode() == 404 ? "" : "error:"
					+ ex.getStatusCode());
			logger.error(ex);
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	private void save() {
		try {
			if (!phonFile.exists()) {
				phonFile.createNewFile();
			}
			try (FileOutputStream out = new FileOutputStream(phonFile)) {
				phonProperties.storeToXML(out, new Date().toString(), "UTF-8");
			}
		} catch (IOException ex) {
			logger.error(ex);
		}
	}

	private String toNormal(String word) {
		return StringUtils.trimToEmpty(word).replaceAll("[ ]+", " ")
				.toLowerCase();
	}

	private enum PHONETIC {
		AM(
				"div[class=\"top-container\"] div[resource=phonetics][class=\"pron-gs ei-g\"] span[geo=n_am].pron-g span.prefix:contains(NAmE) + span[class=phon]",
				Constants.AM_SOUND_DIR, "NAmE//(.*)//",
				"div[class=\"sound audio_play_button pron-us icon-audio\"][data-src-mp3]"), BR(
				"div[class=\"top-container\"] div[resource=phonetics][class=\"pron-gs ei-g\"] span[geo=br].pron-g span.prefix:contains(BrE) + span[class=phon]",
				Constants.BR_SOUND_DIR, "BrE//(.*)//",
				"div[class=\"sound audio_play_button pron-uk icon-audio\"][data-src-mp3]");
		private final String css;

		private final String pattern;
		private final String soundCss;
		private final String soundSubDir;

		PHONETIC(String request, String subDir, String pattern, String soundCss) {
			this.css = request;
			this.soundSubDir = subDir;
			this.pattern = pattern;
			this.soundCss = soundCss;
		}

	}

	private static class Phons {
		Map<String, Set<String>> phons = new HashMap<>();

		public void load(String in) {
			if (!StringUtils.isEmpty(in)) {
				try {
					JsonReader reader = Json.createReader(new StringReader(in));
					JsonObject jobj = reader.readObject();
					for (Map.Entry<String, JsonValue> ent : jobj.entrySet()) {
						Set<String> setOfPhone = get(ent.getKey());
						JsonArray array = (JsonArray) ent.getValue();
						for (JsonValue val : array) {
							JsonString s = (JsonString) val;
							setOfPhone.add(s.getString());
						}
					}
					reader.close();
				} catch (JsonParsingException ex) {
					logger.error(ex);
				}
			}
		}

		public String toJson() {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for (Map.Entry<String, Set<String>> ent : phons.entrySet()) {
				for (String val : ent.getValue()) {
					arrayBuilder.add(val);
				}
				builder.add(ent.getKey(), arrayBuilder);
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			JsonWriter writer = Json.createWriter(bos);
			writer.writeObject(builder.build());
			writer.close();
			return new String(bos.toByteArray());
		}

		Set<String> get(String phon) {
			Set<String> setOfPhon = phons.get(phon);
			if (setOfPhon == null) {
				setOfPhon = new LinkedHashSet<String>();
				phons.put(phon, setOfPhon);
			}
			return setOfPhon;
		}
	}

	public static void main(String... args) throws Exception,
			URISyntaxException, ParseException {
		AppProperties.getInstance().load(null);
		String w = "long";
		// OxfordReceiver.phonProperties.remove(w);
		System.out
				.println(OxfordReceiver.INSTANCE.getPhonetic(w, PHONETICS.AM));
		System.out.println();
	}
}
