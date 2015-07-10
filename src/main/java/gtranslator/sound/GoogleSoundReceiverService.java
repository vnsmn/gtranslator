package gtranslator.sound;

import gtranslator.AppProperties;
import gtranslator.Configurable;
import gtranslator.Registry;
import gtranslator.annotation.Singelton;
import gtranslator.exception.SoundReceiverException;
import gtranslator.ui.Constants;
import gtranslator.ui.Constants.LANG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

public class GoogleSoundReceiverService implements Configurable {
	static final Logger logger = Logger
			.getLogger(GoogleSoundReceiverService.class);
	private final static String RUS_REQUEST = "http://translate.google.com/translate_tts?tl=ru&q=%s";
	private final static String ENG_REQUEST = "http://translate.google.com/translate_tts?tl=en&q=%s";
	private File soundEnDir;
	private File soundRuDir;

	private GoogleSoundReceiverService() {
	}

	@Singelton
	public static void createSingelton() {
		Registry.INSTANCE.add(new GoogleSoundReceiverService());
	}

	public File getSound(String phrase, LANG lang)
			throws SoundReceiverException {
		File soundDir = LANG.RUS == lang ? soundRuDir : soundEnDir;
		File fw = new File(soundDir, phrase + ".mp3");
		boolean isloaded = fw.exists();
		if (!isloaded)
			try {
				isloaded = writeSound(fw, phrase, lang);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
				throw new SoundReceiverException(ex.getMessage(), ex);
			}
		return isloaded ? fw : null;
	}

	private boolean writeSound(File file, String phrase, LANG lang)
			throws IOException {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
			return false;
		}
		URL url = new URL(String.format(LANG.RUS == lang ? RUS_REQUEST
				: ENG_REQUEST, URLEncoder.encode(phrase, "UTF-8")));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setUseCaches(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		long size = 0;
		try (InputStream in = conn.getInputStream()) {
			size = Files.copy(in, Paths.get(file.toURI()),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage());
		}
		return size > 0;
	}

	public static void main(String... args) throws Exception {
		new GoogleSoundReceiverService().getSound("тестирование", LANG.RUS);
	}

	@Override
	public void init(AppProperties appProperties) {
		File dicDir = new File(appProperties.getDictionaryDirPath());
		soundEnDir = new File(dicDir, Constants.EN_SOUND_DIR);
		soundRuDir = new File(dicDir, Constants.RU_SOUND_DIR);
		if (!soundEnDir.exists()) {
			soundEnDir.mkdirs();
		}
		if (!soundRuDir.exists()) {
			soundRuDir.mkdirs();
		}
	}

	@Override
	public void close() {
	}
}