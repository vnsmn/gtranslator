package gtranslator;

import java.io.File;
import java.io.IOException;

public interface SoundReceiver {
	public String AM = "am";
	public String BR = "br";
	
	public class SoundReceiverException extends Exception {
		public SoundReceiverException(String message) {
			super(message);
		}
		public SoundReceiverException(String message, Throwable th) {
			super(message, th);
		}
	}
	boolean createSound(File dirFile, String word) throws SoundReceiverException;
}
