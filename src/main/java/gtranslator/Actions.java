package gtranslator;

import gtranslator.sound.SoundHelper;
import gtranslator.translate.TranslationReceiver;
import gtranslator.ui.UIOutput;

import org.apache.log4j.Logger;

public class Actions {
	private static final Logger logger = Logger.getLogger(Actions.class);

	interface Action<T> {
		void execute(T arg);
	}

	public static class HistoryDictionaryAction implements Action<String> {
		@Override
		public void execute(String arg) {
			UIOutput.getInstance().setWaitCursor();
			try {
				HistoryHelper.INSTANCE.save();
				DictionaryHelper.INSTANCE.createDictionary(
						HistoryHelper.INSTANCE.getWords(), AppProperties
								.getInstance().getDictionaryDirPath());
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class ClearHistoryAction implements Action<String> {
		@Override
		public void execute(String engWord) {
			UIOutput.getInstance().setWaitCursor();
			try {
				HistoryHelper.INSTANCE.delete(TranslationReceiver.INSTANCE
						.toNormal(engWord));
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class PlayEngWordAction implements Action<String> {
		@Override
		public void execute(String engWord) {
			UIOutput.getInstance().setWaitCursor();
			try {
				SoundHelper.playWord(engWord, false);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class PlayEngWordWithLoadAction implements Action<String> {
		@Override
		public void execute(String engWord) {
			UIOutput.getInstance().setWaitCursor();
			try {
				SoundHelper.playWord(engWord, true);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class StartStopTClipboardAction implements Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				ClipboardObserver.getInstance().setPause(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class ModeTClipboardAction implements Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				ClipboardObserver.getInstance().setSelected(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class DetailTranslateAction implements Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setAddition(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class RewriteHistoryAction implements Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setRewrite(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class UseHistoryAction implements Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setHistory(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class DisposeAppAction implements Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				App.stopClipboardThread();
				HistoryHelper.INSTANCE.save();
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class WordPlayOfClipboardAction implements Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				if (b) {
					ClipboardObserver.getInstance().setActionListener(
							new ClipboardObserver.ActionListener() {
								@Override
								public void execute(String s) {
									SoundHelper.playWord(s, true);
								}
							});
				} else {
					ClipboardObserver.getInstance().setActionListener(null);
				}
				HistoryHelper.INSTANCE.save();
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class TranslateWordAction implements Action<String[]> {
		@Override
		public void execute(String[] ss) {
			UIOutput.getInstance().setWaitCursor();
			try {
				ss[1] = TranslationReceiver.INSTANCE.translateAndFormat(ss[0],
						false);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class CookieAction implements Action<String> {
		@Override
		public void execute(String s) {
			UIOutput.getInstance().setWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setCookie(s);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}
}
