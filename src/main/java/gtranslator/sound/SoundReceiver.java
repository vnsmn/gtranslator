package gtranslator.sound;

import gtranslator.exception.SoundReceiverException;

import java.io.File;
import java.nio.file.Paths;

public interface SoundReceiver {
	String AM = "am";
	String BR = "br";
	String RU = "ru";
	
	String AM_SOUND_DIR = Paths.get("sounds", AM).toString();
	String BR_SOUND_DIR = Paths.get("sounds", BR).toString();
	String RU_SOUND_DIR = Paths.get("sounds", RU).toString();
	
	boolean createSoundFile(File dirFile, String word)
			throws SoundReceiverException;
}
