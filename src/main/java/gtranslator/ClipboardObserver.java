package gtranslator;

import gtranslator.annotation.Singelton;
import gtranslator.persistences.WordDao;
import gtranslator.translate.TranslationService;
import gtranslator.ui.UIOutput;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

/*
 * grant { permission java.awt.AWTPermission "accessClipboard" };
 */
public class ClipboardObserver implements Runnable, ClipboardOwner,
		Configurable {
	private boolean isLostData = true;
	private AtomicBoolean isPause = new AtomicBoolean(false);
	private AtomicBoolean isStart = new AtomicBoolean(false);
	private AtomicReference<MODE> mode = new AtomicReference<>(MODE.COPY);
	@Resource
	private UIOutput uiOutput;
	@Resource
	private TranslationService translationReceiver;

	private WordDao wordDao = new WordDao();

	public enum MODE {
		SELECT, COPY, TEXT
	}

	private ActionListener actionListener;

	static final Logger logger = Logger.getLogger(ClipboardObserver.class);

	public interface ActionListener {
		void execute(String text);
	}

	@Singelton
	public static void createSingelton() {
		Registry.INSTANCE.add(new ClipboardObserver());
	}

	private ClipboardObserver() {
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(1);
		}
		GlobalScreen.getInstance().addNativeMouseListener(
				new NativeMouseListenerExt());
		GlobalScreen.getInstance().addNativeKeyListener(
				new NativeKeyListenerExt());
	}

	@Override
	public void run() {

		/*
		 * SecurityManager sm = new SecurityManager(); try {
		 * sm.checkPermission(new AWTPermission("accessClipboard")); } catch
		 * (SecurityException ex) { ex.printStackTrace(); System.exit(-1); }
		 */

		Clipboard copyClipboard = Toolkit.getDefaultToolkit()
				.getSystemClipboard();
		Clipboard selClipboard = Toolkit.getDefaultToolkit()
				.getSystemSelection();
		while (!Thread.interrupted()) {
			try {
				// if ((isLostData || mode.get() == MODE.TEXT) && !isPause.get()
				// && isStart.get()) {
				if ((isLostData && mode.get() != MODE.TEXT) && !isPause.get()
						&& isStart.get()) {
					synchronized (this) {
						Clipboard clipboard = mode.get() == MODE.SELECT ? selClipboard
								: copyClipboard;
						Transferable clipData = clipboard.getContents(null);
						if (clipData
								.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							Object text = clipData
									.getTransferData(DataFlavor.stringFlavor);
							uiOutput.setSourceText(text.toString());
							String translate = translationReceiver
									.translateAndFormat(text.toString(), false);							
							uiOutput.setTargetText(translate);
							if (mode.get() == MODE.COPY) {
								uiOutput.selectTranslatePanel();
							}
							if (actionListener != null) {
								try {
									actionListener.execute(text.toString());
								} catch (Exception ex) {
									logger.error(ex.getMessage());
								}
							}
							StringSelection st = new StringSelection(
									text.toString());
							clipboard.setContents(st, this);
							isLostData = false;
							uiOutput.restore();
							wordDao.save(text.toString());
							/*
							 * if (mode.get() == MODE.TEXT) { if
							 * (!text.equals(clipText)) { clipText = text;
							 * UIOutput.getInstance().restore(true); } } else {
							 * StringSelection st = new StringSelection(
							 * text.toString()); clipboard.setContents(st,
							 * this); isLostData = false;
							 * UIOutput.getInstance().restore(); }
							 */
						}
					}
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.info("The clipboardObserver has stoped");
				Thread.interrupted();
				break;
			} catch (UnsupportedFlavorException e) {
				isLostData = true;
				logger.error(e.getMessage(), e);
			} catch (Exception e) {
				isLostData = true;
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public synchronized void lostOwnership(Clipboard clipboard,
			Transferable contents) {
		isLostData = true;
	}

	public synchronized void setPause(boolean b) {
		isPause.set(b);
		isLostData = true;
	}

	public synchronized void setStart(boolean b) {
		isStart.set(b);
		isLostData = true;
	}

	public synchronized void setMode(MODE mode) {
		this.mode.set(mode);
		isLostData = true;
	}

	public synchronized void setActionListener(ActionListener l) {
		actionListener = l;
	}

	public synchronized boolean isSupportSoundWord() {
		return actionListener != null;
	}

	private static class NativeKeyListenerExt implements NativeKeyListener {

		@Override
		public void nativeKeyPressed(NativeKeyEvent e) {
			if (e.getModifiers() == 2
					&& e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
				App.getUIOutput().hide();
				App.getUIOutput().selectSetupPanel();
			} else if (e.getKeyCode() == NativeKeyEvent.VK_WINDOWS) {
				App.getUIOutput().selectTranslatePanel();
				App.getUIOutput().restore();
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent e) {
		}

		@Override
		public void nativeKeyTyped(NativeKeyEvent e) {
		}

	}

	private static class NativeMouseListenerExt implements NativeMouseListener {
		String seltext = "";
		WordDao wordDao = new WordDao();

		@Override
		public void nativeMouseClicked(NativeMouseEvent e) {
		}

		@Override
		public void nativeMousePressed(NativeMouseEvent e) {
		}

		@Override
		public void nativeMouseReleased(NativeMouseEvent e) {
			try {
				if (e.getButton() == 1 && e.getClickCount() <= 2) {
					if (App.getClipboardObserver().mode.get() == MODE.TEXT
							&& !App.getClipboardObserver().isPause.get()
							&& App.getClipboardObserver().isStart.get()) {
						Clipboard clipboard = Toolkit.getDefaultToolkit()
								.getSystemSelection();
						Transferable clipData = null;
						try {
							clipData = clipboard.getContents(null);
						} catch (Exception ex) {
							logger.error(ex.getMessage(), ex);
							return;
						}
						if (clipData
								.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							Object text = clipData
									.getTransferData(DataFlavor.stringFlavor);
							if (StringUtils.isBlank("" + text)) {
								return;
							}
							if (seltext != null
									&& seltext.equals(text.toString())) {
								return;
							}
							seltext = text.toString();
							App.getUIOutput().setSourceText(text.toString());
							String translate = App.getTranslationService()
									.translateAndFormat(text.toString(), false);
							App.getUIOutput().setTargetText(translate);
							if (App.getClipboardObserver().actionListener != null) {
								try {
									App.getClipboardObserver().actionListener
											.execute(text.toString());
								} catch (Exception ex) {
									logger.error(ex.getMessage());
								}
							}
							App.getUIOutput().restore();
							wordDao.save(text.toString());
							Thread.sleep(1000);
						}
					}
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	@Override
	public void init(AppProperties appProperties) {
	}

	@Override
	public void close() {
	}
}
