package gtranslator;

import gtranslator.DictionaryService.DictionaryInput;
import gtranslator.persistences.WordDao;
import gtranslator.sound.SoundHelper;
import gtranslator.ui.Constants.PHONETICS;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Actions {
	private static final Logger logger = Logger.getLogger(Actions.class);
	private static Map<String, Action<?>> actions = new HashMap<>();

	public static abstract class Action<T> {
		public abstract void execute(T arg);
	}

	public static <N, T extends Action<N>> T findAction(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		T act = (T) actions.get(clazz.getName());
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

	public static class DictionaryAction extends Action<DictionaryInput> {
		@Override
		public void execute(DictionaryInput dic) {
			App.getUIOutput().showWaitCursor();
			try {
				if (dic.sourceType == DictionaryService.SOURCE_TYPE.HISTORY) {
					App.getDictionaryService().createDictionaryFromHistory(dic);
				} else if (dic.sourceType == DictionaryService.SOURCE_TYPE.DICTIONARY) {
					App.getDictionaryService().createDictionaryFromDict(dic);
				} else if (dic.sourceType == DictionaryService.SOURCE_TYPE.TEXT) {
					App.getDictionaryService().createDictionaryFromText(dic);
				} else if (dic.sourceType == DictionaryService.SOURCE_TYPE.IRREGULAR_VERB) {
					App.getDictionaryService()
							.createDictionaryFromIrregularVerb(dic);
				} else if (dic.sourceType == DictionaryService.SOURCE_TYPE.RUNTIME_WORDS) {
					App.getDictionaryService()
							.createDictionaryFromRuntimeWords(dic);
				} else if (dic.sourceType == DictionaryService.SOURCE_TYPE.WORDS_DIC 
						|| dic.sourceType == DictionaryService.SOURCE_TYPE.WORDS_IN
						|| dic.sourceType == DictionaryService.SOURCE_TYPE.WORDS_OUT) {
					App.getDictionaryService()
					.createDictionaryFromWords(dic);
		}
			} catch (Exception ex) {
				logger.error(ex);
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class ClearHistoryAction extends Action<String> {

		@Override
		public void execute(String engWord) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getHistoryService().delete(
						App.getTranslationService().toNormal(engWord));
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class PlayEngWordAction extends Action<String> {

		@Override
		public void execute(String engWord) {
			App.getUIOutput().showWaitCursor();
			try {
				SoundHelper.playEngWord(engWord);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class PlayEngWordWithLoadAction extends Action<String> {

		@Override
		public void execute(String engWord) {
			App.getUIOutput().showWaitCursor();
			try {
				SoundHelper.playEngWord(engWord);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class PauseTClipboardAction extends Action<Boolean> {

		@Override
		public void execute(Boolean b) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getClipboardObserver().setPause(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class StartStopTClipboardAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getClipboardObserver().setStart(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class ModeTClipboardAction extends
			Action<ClipboardObserver.MODE> {
		@Override
		public void execute(ClipboardObserver.MODE mode) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getClipboardObserver().setMode(mode);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class DetailTranslateAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getTranslationService().setAddition(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class RewriteHistoryAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getTranslationService().setRewrite(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class UseHistoryAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getTranslationService().setHistory(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class DisposeAppAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			App.getUIOutput().showWaitCursor();
			try {
				App.close();
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
				System.exit(0);
			}
		}
	}

	public static class WordPlayOfClipboardAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			App.getUIOutput().showWaitCursor();
			try {
				if (b) {
					App.getClipboardObserver().setActionListener(
							new ClipboardObserver.ActionListener() {
								@Override
								public void execute(String s) {
									App.getUIOutput().showWaitCursor();
									try {
										SoundHelper.playEngWord(s);
									} finally {
										App.getUIOutput().hideWaitCursor();
									}
								}
							});
				} else {
					App.getClipboardObserver().setActionListener(null);
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class SetDictionaryPhoneticAction extends Action<PHONETICS> {
		@Override
		public void execute(PHONETICS ph) {
			App.getUIOutput().showWaitCursor();
			try {
				AppProperties.getInstance().setDictionaryPhonetic(ph);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class TranslateWordAction extends Action<String[]> {
		@Override
		public void execute(String[] ss) {
			App.getUIOutput().showWaitCursor();
			try {
				ss[1] = App.getTranslationService().translateAndFormat(ss[0],
						false);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class CookieAction extends Action<String> {
		@Override
		public void execute(String s) {
			App.getUIOutput().showWaitCursor();
			try {
				App.getTranslationService().setCookie(s);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class DictionarySynthesizerAction extends Action<Boolean> {
		@Override
		public void execute(Boolean b) {
			try {
				AppProperties.getInstance().setDictionarySynthesizer(b);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}
		}
	}

	public static class RestoreHistoryAction extends Action<Void> {
		@Override
		public void execute(Void v) {
			try {
				App.getUIOutput().showWaitCursor();
				App.getHistoryService().restore();
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}

	public static class SnapshotAction extends Action<Object[]> {
		@Override
		public void execute(Object[] args) {
			execute(args[0].toString(), args.length == 2 ? (Boolean) args[1]
					: null);
		}

		public void execute(String eng, boolean isVisible) {
			if (!App.getHistoryService().isWord(eng)) {
				return;
			}
			try {
				App.getUIOutput().showWaitCursor();
				WordDao snapshotDao = new WordDao();
				snapshotDao.save(eng, isVisible);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				App.getUIOutput().hideWaitCursor();
			}
		}
	}
}
