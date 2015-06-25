package gtranslator.sound;

import gtranslator.exception.SoundReceiverException;

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

public class GoogleSoundReceiver implements SoundReceiver {
	static final Logger logger = Logger.getLogger(GoogleSoundReceiver.class);
	public static final GoogleSoundReceiver INSTANCE = new GoogleSoundReceiver();
	private final static String RUS_REQUEST = "http://translate.google.com/translate_tts?tl=ru&q=%s";
	private final static String ENG_REQUEST = "http://translate.google.com/translate_tts?tl=en&q=%s";

	private GoogleSoundReceiver() {
	}

	public boolean createSoundFile(File dicDir, String phrase, LANG lang)
			throws SoundReceiverException {
		File soundDir = new File(dicDir, LANG.RUS == lang ? RU_SOUND_DIR
				: EN_SOUND_DIR);
		if (!soundDir.exists()) {
			soundDir.mkdirs();
		}
		File fw = new File(soundDir, phrase + ".mp3");
		boolean isloaded = fw.exists();
		if (!isloaded)
			try {
				isloaded = writeSound(fw, phrase, lang);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
				throw new SoundReceiverException(ex.getMessage(), ex);
			}
		return isloaded;
	}

	@Override
	public boolean createSoundFile(File dicDir, String phrase)
			throws SoundReceiverException {
		File soundDir = new File(dicDir, RU_SOUND_DIR);
		if (!soundDir.exists()) {
			soundDir.mkdirs();
		}
		File fw = new File(soundDir, phrase + ".mp3");
		boolean isloaded = fw.exists();
		if (!isloaded)
			try {
				isloaded = writeSound(fw, phrase, LANG.RUS);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
				throw new SoundReceiverException(ex.getMessage(), ex);
			}
		return isloaded;
	}

	public String getFilePath(String dir, String word, LANG lang) {
		return Paths.get(dir, LANG.RUS == lang ? RU_SOUND_DIR : EN_SOUND_DIR,
				word.concat(".mp3")).toString();
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
		new GoogleSoundReceiver().createSoundFile(new File("/tmp"),
				"тестирование");
	}
}