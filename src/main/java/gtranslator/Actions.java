package gtranslator;

import gtranslator.sound.SoundHelper;
import gtranslator.translate.TranslationReceiver;
import gtranslator.ui.UIOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;

public class Actions {
	private static final Logger logger = Logger.getLogger(Actions.class);
	private static Map<String, Action<?>> actions = new HashMap<>();

	public static abstract class Action<T> {
		private Observable observable = new Observable() {
			public void notifyObservers(Object arg) {
		        super.setChanged();
		        super.notifyObservers(arg);
		    }
		};

		public abstract void execute(T arg);

		public Observable getObservable() {
			return observable;
		}		
	}

	public static <T> Action<T> findAction(Class<? extends Action<T>> clazz) {
		@SuppressWarnings("unchecked")
		Action<T> act = (Action<T>) actions.get(clazz.getName());
		synchronized (Actions.class) {
			if (act == null) {
				try {
					act = clazz.newInstance();
					actions.put(clazz.getName(),  act);
				} catch (Exception ex) {
					logger.error(ex);
					System.exit(-1);
				}
			}
		}
		return act;
	}

	public static class HistoryDictionaryAction extends Action<String> {
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

	public static class ClearHistoryAction extends Action<String> {

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

	public static class PlayEngWordAction extends Action<String> {

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

	public static class PlayEngWordWithLoadAction extends Action<String> {

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

	public static class StartStopTClipboardAction extends Action<Boolean> {

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

	public static class ModeTClipboardAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().setWaitCursor();
			try {
				ClipboardObserver.getInstance().setSelected(b);
				getObservable().notifyObservers(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().setDefCursor();
			}
		}
	}

	public static class DetailTranslateAction extends Action<Boolean> {
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

	public static class RewriteHistoryAction extends Action<Boolean> {
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

	public static class UseHistoryAction extends Action<Boolean> {
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

	public static class DisposeAppAction extends Action<Boolean> {
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

	public static class WordPlayOfClipboardAction extends Action<Boolean> {
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

	public static class TranslateWordAction extends Action<String[]> {
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

	public static class CookieAction extends Action<String> {
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
