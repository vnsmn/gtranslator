package gtranslator.sound;

import gtranslator.exception.SoundReceiverException;

import java.io.File;
import java.nio.file.Paths;

public interface SoundReceiver {
	String AM = "am";
	String BR = "br";
	String RU = "ru";
	String EN = "en";

	String AM_SOUND_DIR = Paths.get("sounds", AM).toString();
	String BR_SOUND_DIR = Paths.get("sounds", BR).toString();
	String RU_SOUND_DIR = Paths.get("sounds", RU).toString();
	String EN_SOUND_DIR = Paths.get("sounds", EN).toString();

	public enum LANG {
		RUS, ENG
	}

	boolean createSoundFile(File dirFile, String word)
			throws SoundReceiverException;

	public boolean createSoundFile(File dicDir, String phrase, LANG lang)
			throws SoundReceiverException;
}
