package gtranslator.exception;

public class SoundReceiverException extends Exception {
	private static final long serialVersionUID = 1L;

	public SoundReceiverException(String message) {
		super(message);
	}

	public SoundReceiverException(String message, Throwable th) {
		super(message, th);
	}		
}
