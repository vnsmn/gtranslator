package gtranslator.sound;

import gtranslator.DictionaryHelper;
import gtranslator.exception.SoundReceiverException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OxfordSoundReceiver implements SoundReceiver {
	static final Logger logger = Logger.getLogger(DictionaryHelper.class);
	private static final String REQUEST = "http://www.oxfordlearnersdictionaries.com/definition/english/%s_1?q=%s";

	private static Properties missedProperies = new Properties();
	private static File missedFiles;
	static {
		String dir = System.getProperty("user.home");
		missedFiles = new File(dir, "gtranslator-oxford-sound-missed.xml");
		if (missedFiles.exists())
			try (FileInputStream in = new FileInputStream(missedFiles)) {
				missedProperies.loadFromXML(in);
			} catch (Exception ex) {
				logger.error(ex);
			}
	}

	public boolean createSoundFile(File dicDir, String word)
			throws SoundReceiverException {
		File dirBr = new File(dicDir, BR_SOUND_DIR);
		File dirAm = new File(dicDir, AM_SOUND_DIR);
		if (!dirBr.exists()) {
			dirBr.mkdirs();
		}
		if (!dirAm.exists()) {
			dirAm.mkdirs();
		}
		File fBr = new File(dirBr, word.concat(".mp3"));
		File fAm = new File(dirAm, word.concat(".mp3"));
		boolean isloaded;
		if (!fBr.exists() || !fAm.exists()) {
			if (missedProperies.contains(word)) {
				return false;
			}
			try {
				Map<String, String> refs = getDataSrcList(String.format(
						REQUEST, word, word));
				String refBr = refs.get(BR_SOUND_DIR);
				String refAm = refs.get(AM_SOUND_DIR);
				logger.info(refBr);
				logger.info(refAm);
				isloaded = !fBr.exists() ? writeSound(fBr, word, refBr) : true;
				isloaded |= !fAm.exists() ? writeSound(fAm, word, refAm) : true;
			} catch (IOException ex) {
				missedProperies.put(word, word);
				saveMissedFiles();
				throw new SoundReceiverException(ex.getMessage() + ". url: "
						+ String.format(REQUEST, word, word), ex);
			}			
		} else {
			isloaded = true;
		}

		if (!isloaded) {
			missedProperies.put(word, word);
			saveMissedFiles();
		}

		return isloaded;
	}

	private boolean writeSound(File file, String word, String request)
			throws IOException {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage());
		}
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

	private Map<String, String> getDataSrcList(String request)
			throws IOException {
		Map<String, String> refs = new HashMap<>();
		Document doc = Jsoup.connect(request).timeout(3000).get();
		Elements elements = doc
				.select("div#entryContent span[geo=br].pron-g div[data-src-mp3].audio_play_button");
		if (elements.size() > 0) {
			refs.put(BR_SOUND_DIR, elements.first().attr("data-src-mp3"));
		}
		elements = doc
				.select("div#entryContent span[geo=n_am].pron-g div[data-src-mp3].audio_play_button");
		if (elements.size() > 0) {
			refs.put(AM_SOUND_DIR, elements.first().attr("data-src-mp3"));
		}
		return refs;
	}

	public void saveMissedFiles() {
		try {
			if (!missedFiles.exists()) {
				missedFiles.createNewFile();
			}
			try (FileOutputStream out = new FileOutputStream(missedFiles)) {
				missedProperies.storeToXML(out, new Date().toString(), "UTF-8");
			}
		} catch (IOException ex) {
			logger.error(ex);
		}
	}
}
