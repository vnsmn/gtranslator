package gtranslator;

import gtranslator.sound.SoundHelper;
import gtranslator.translate.TranslationReceiver;
import gtranslator.ui.UIOutput;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Actions {
	private static final Logger logger = Logger.getLogger(Actions.class);
	private static Map<String, Action<?>> actions = new HashMap<>();

	public static abstract class Action<T> {
		public abstract void execute(T arg);
	}

	public static <T> Action<T> findAction(Class<? extends Action<T>> clazz) {
		@SuppressWarnings("unchecked")
		Action<T> act = (Action<T>) actions.get(clazz.getName());
		synchronized (Actions.class) {
			if (act == null) {
				try {
					act = clazz.newInstance();
					actions.put(clazz.getName(), act);
				} catch (Exception ex) {
					logger.error(ex);
					System.exit(-1);
				}
			}
		}
		return act;
	}

	public static class DictionaryInput {
		public String path;
		public String resultDir;
		public DictionaryHelper.SOURCE_TYPE sourceType;
		public boolean isAmPronunciation;
		public boolean isBrPronunciation;
		public boolean isRusTransled;
		public boolean isMultiRusTransled;
	}

	public static class DictionaryAction extends Action<DictionaryInput> {
		@Override
		public void execute(DictionaryInput dic) {
			UIOutput.getInstance().showWaitCursor();
			try {
				HistoryHelper.INSTANCE.save();
				if (dic.sourceType == DictionaryHelper.SOURCE_TYPE.HISTORY) {
					DictionaryHelper.INSTANCE.createDictionaryFromHistory(
							dic.resultDir, dic.isAmPronunciation,
							dic.isBrPronunciation, dic.isRusTransled,
							dic.isMultiRusTransled);
				} else if (dic.sourceType == DictionaryHelper.SOURCE_TYPE.DICTIONARY) {
					DictionaryHelper.INSTANCE.createDictionaryFromDict(
							dic.path, dic.resultDir, dic.isAmPronunciation,
							dic.isBrPronunciation, dic.isRusTransled,
							dic.isMultiRusTransled);
				} else if (dic.sourceType == DictionaryHelper.SOURCE_TYPE.TEXT) {
					DictionaryHelper.INSTANCE.createDictionaryFromText(
							dic.path, dic.resultDir, dic.isAmPronunciation,
							dic.isBrPronunciation, dic.isRusTransled,
							dic.isMultiRusTransled);
				}
			} catch (Exception ex) {
				logger.error(ex);
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class ClearHistoryAction extends Action<String> {

		@Override
		public void execute(String engWord) {
			UIOutput.getInstance().showWaitCursor();
			try {
				HistoryHelper.INSTANCE.delete(TranslationReceiver.INSTANCE
						.toNormal(engWord));
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class PlayEngWordAction extends Action<String> {

		@Override
		public void execute(String engWord) {
			UIOutput.getInstance().showWaitCursor();
			try {
				SoundHelper.playEngWord(engWord, false);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class PlayEngWordWithLoadAction extends Action<String> {

		@Override
		public void execute(String engWord) {
			UIOutput.getInstance().showWaitCursor();
			try {
				SoundHelper.playEngWord(engWord, true);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class StartStopTClipboardAction extends Action<Boolean> {

		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().showWaitCursor();
			try {
				ClipboardObserver.getInstance().setPause(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class ModeTClipboardAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().showWaitCursor();
			try {
				ClipboardObserver.getInstance().setSelected(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class DetailTranslateAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().showWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setAddition(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class RewriteHistoryAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().showWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setRewrite(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class UseHistoryAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().showWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setHistory(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class DisposeAppAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().showWaitCursor();
			try {
				App.stopClipboardThread();
				HistoryHelper.INSTANCE.save();
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class WordPlayOfClipboardAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			UIOutput.getInstance().showWaitCursor();
			try {
				if (b) {
					ClipboardObserver.getInstance().setActionListener(
							new ClipboardObserver.ActionListener() {
								@Override
								public void execute(String s) {
									UIOutput.getInstance().showWaitCursor();
									try {
										SoundHelper.playEngWord(s, true);
									} finally {
										UIOutput.getInstance().hideWaitCursor();
									}
								}
							});
				} else {
					ClipboardObserver.getInstance().setActionListener(null);
				}
				HistoryHelper.INSTANCE.save();
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}
	
	public static class SetDictionaryPronunciationAction extends Action<String> {
		@Override
		public void execute(String s) {
			UIOutput.getInstance().showWaitCursor();
			try {
				AppProperties.getInstance().setDictionaryPronunciation(s);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class TranslateWordAction extends Action<String[]> {
		@Override
		public void execute(String[] ss) {
			UIOutput.getInstance().showWaitCursor();
			try {
				ss[1] = TranslationReceiver.INSTANCE.translateAndFormat(ss[0],
						false);
				if (ClipboardObserver.getInstance().isSupportSoundWord()) {					
					SoundHelper.playEngWord(TranslationReceiver.INSTANCE.toNormal(ss[0]), true);
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}

	public static class CookieAction extends Action<String> {
		@Override
		public void execute(String s) {
			UIOutput.getInstance().showWaitCursor();
			try {
				TranslationReceiver.INSTANCE.setCookie(s);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				UIOutput.getInstance().hideWaitCursor();
			}
		}
	}
}
