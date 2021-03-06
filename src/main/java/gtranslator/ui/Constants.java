package gtranslator.ui;

import java.nio.file.Paths;

public class Constants {
	public final static String PROPERTY_CHANGE_ACTIVITY_CLIPBOARD = "PROPERTY_CHANGE_ACTIVITY_CLIPBOARD";
	public final static String PROPERTY_CHANGE_MODE_CLIPBOARD = "PROPERTY_CHANGE_MODE_CLIPBOARD";
	public final static String PROPERTY_CHANGE_DETAIL_CLIPBOARD = "PROPERTY_CHANGE_DETAIL_CLIPBOARD";
	public final static String PROPERTY_CHANGE_COOKIE = "PROPERTY_CHANGE_COOKIE";
	public final static String PROPERTY_CHANGE_DICTIONARY_DIR = "PROPERTY_CHANGE_DICTIONARY_DIR";
	public final static String PROPERTY_CHANGE_HISTORY = "PROPERTY_CHANGE_HISTORY";
	public final static String PROPERTY_CHANGE_SOUND = "PROPERTY_CHANGE_SOUND";
	public final static String PROPERTY_CHANGE_FIXED_LOCATION_FRAME = "PROPERTY_CHANGE_FIXED_LOCATION_FRAME";
	public final static String PROPERTY_CHANGE_STATISTIC = "PROPERTY_CHANGE_STATISTIC";
	public final static String PROPERTY_CHANGE_DICTIONARY_PRONUNCIATION = "PROPERTY_CHANGE_DICTIONARY_PRONUNCIATION";
	public final static String PROPERTY_CHANGE_DICTIONARY_RESULT_DIR = "PROPERTY_CHANGE_DICTIONARY_RESULT_DIR";
	public final static String PROPERTY_CHANGE_DICTIONARY_BLOCK_LIMIT = "PROPERTY_CHANGE_DICTIONARY_BLOCK_LIMIT";
	public final static String PROPERTY_CHANGE_DICTIONARY_PAUSE_SECONDS = "PROPERTY_CHANGE_DICTIONARY_PAUSE_SECONDS";
	public final static String PROPERTY_CHANGE_DICTIONARY_DEFIS_SECONDS = "PROPERTY_CHANGE_DICTIONARY_DEFIS_SECONDS";
	public final static String PROPERTY_CHANGE_DICTIONARY_SYNTHESIZER = "PROPERTY_CHANGE_DICTIONARY_SYNTHESIZER";

	public final static String AM = "am";
	public final static String BR = "br";
	public final static String RU = "ru";
	public final static String EN = "en";

	public final static String AM_SOUND_DIR = Paths.get("sounds", AM)
			.toString();
	public final static String BR_SOUND_DIR = Paths.get("sounds", BR)
			.toString();
	public final static String RU_SOUND_DIR = Paths.get("sounds", RU)
			.toString();
	public final static String EN_SOUND_DIR = Paths.get("sounds", EN)
			.toString();

	public enum PHONETICS {
		AM, BR
	}

	public enum LANG {
		RUS, ENG
	}
}
