package gtranslator.sound;

import gtranslator.exception.SoundReceiverException;

import java.io.File;
import java.nio.file.Paths;

public interface SoundReceiver {
	String AM_SOUND_DIR = Paths.get("sounds", "am").toString();
	String BR_SOUND_DIR = Paths.get("sounds", "br").toString();
	
	boolean createSoundFile(File dirFile, String word)
			throws SoundReceiverException;
}
