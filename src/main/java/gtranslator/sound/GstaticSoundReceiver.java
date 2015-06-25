package gtranslator.sound;

import gtranslator.exception.SoundReceiverException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

public class GstaticSoundReceiver implements SoundReceiver {
	static final Logger logger = Logger.getLogger(GstaticSoundReceiver.class);

	private final static String REQUEST = "http://ssl.gstatic.com/dictionary/static/sounds/de/0/";

	@Override
	public boolean createSoundFile(File dicDir, String word, LANG lang)
			throws SoundReceiverException {
		File soundDir = new File(dicDir, AM_SOUND_DIR);
		if (!soundDir.exists()) {
			soundDir.mkdirs();
		}
		File fw = new File(soundDir, word + ".mp3");
		boolean isloaded = fw.exists();
		if (!isloaded)
			try {
				isloaded = writeSound(fw, word);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
				throw new SoundReceiverException(ex.getMessage(), ex);
			}
		return isloaded;
	}

	private boolean writeSound(File dirFile, String word) throws IOException {
		URL url = new URL(REQUEST.concat(word).concat(".mp3"));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setUseCaches(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		File fw = new File(dirFile, word + ".mp3");
		long size = 0;
		try (InputStream in = conn.getInputStream()) {
			size = Files.copy(in, Paths.get(fw.toURI()),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage());
		}
		return size > 0;
	}

	@Override
	public boolean createSoundFile(File dicDir, String phrase)
			throws SoundReceiverException {
		return createSoundFile(dicDir, phrase, LANG.ENG);
	}
}