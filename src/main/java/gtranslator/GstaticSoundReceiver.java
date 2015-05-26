package gtranslator;

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

	@Override
	public boolean createSound(File dirFile, String word)
			throws SoundReceiverException {
		File f = new File(dirFile, word + ".mp3");
		boolean isloaded = f.exists();
		if (!isloaded) {
			try {
				isloaded = writeSound(f, word);
			} catch (IOException ex) {
				throw new SoundReceiverException(ex.getMessage(), ex);
			}
		}
		return isloaded;
	}

	private boolean writeSound(File dirFile, String word) throws IOException {
		String request = "http://ssl.gstatic.com/dictionary/static/sounds/de/0/"
				+ word + ".mp3";
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		conn.setUseCaches(false);
		File f = new File(dirFile, word + ".mp3");
		long size = 0;
		try {
			InputStream in = conn.getInputStream();
			size = Files.copy(in, Paths.get(f.toURI()),
					StandardCopyOption.REPLACE_EXISTING);
			in.close();
		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage());
		}
		return size > 0;
	}

	@Override
	public File findFile(boolean isBr, File soundDir, String word) {
		return null;
	}
}
