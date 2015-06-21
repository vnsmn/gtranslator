package gtranslator.exception;

public class GTranslatorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GTranslatorException(String format, Object... message) {
		super(String.format(format, message));
	}

	public GTranslatorException(Throwable th, String format, Object... message) {
		super(String.format(format, message), th);
	}
	
	public GTranslatorException(String message) {
		super(message);
	}
	
	public GTranslatorException(String message, Throwable th) {
		super(message, th);
	}
}
