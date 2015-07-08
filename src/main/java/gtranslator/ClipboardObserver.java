package gtranslator;

import gtranslator.translate.TranslationReceiver;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

/*
 * grant { permission java.awt.AWTPermission "accessClipboard" };
 */
public class ClipboardObserver implements Runnable, ClipboardOwner {
	private boolean isLostData = true;
	private AtomicBoolean isPause = new AtomicBoolean(false);
	private AtomicBoolean isStart = new AtomicBoolean(false);
	private AtomicReference<MODE> mode = new AtomicReference<>(MODE.COPY);
	private static ClipboardObserver instance;

	public enum MODE {
		SELECT, COPY, TEXT
	}

	private ActionListener actionListener;

	static final Logger logger = Logger.getLogger(ClipboardObserver.class);

	public interface ActionListener {
		void execute(String text);
	}

	private ClipboardObserver() {
	}

	public static ClipboardObserver getInstance() {
		if (instance == null) {
			try {
				GlobalScreen.registerNativeHook();
			} catch (NativeHookException ex) {
				logger.error(ex.getMessage(), ex);
				System.exit(1);
			}
			GlobalScreen.getInstance().addNativeMouseListener(
					new NativeMouseListenerExt());
			instance = new ClipboardObserver();
		}
		return instance;
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
		Object clipText = null;
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
							UIOutput.getInstance().setSourceText(
									text.toString());
							String translate = TranslationReceiver.INSTANCE
									.translateAndFormat(text.toString(), false);
							UIOutput.getInstance().setTargetText(translate);
							if (mode.get() == MODE.COPY) {
								UIOutput.getInstance().selectTranslatePanel();
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
							UIOutput.getInstance().restore();
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

	private static class NativeMouseListenerExt implements NativeMouseListener {
		@Override
		public void nativeMouseClicked(NativeMouseEvent e) {
		}

		@Override
		public void nativeMousePressed(NativeMouseEvent e) {
		}

		@Override
		public void nativeMouseReleased(NativeMouseEvent e) {
			try {
				if (e.getButton() == 1 && e.getClickCount() <= 1) {
					if (ClipboardObserver.getInstance().mode.get() == MODE.TEXT
							&& !ClipboardObserver.getInstance().isPause.get()
							&& ClipboardObserver.getInstance().isStart.get()) {
						Clipboard clipboard = Toolkit.getDefaultToolkit()
								.getSystemSelection();
						Transferable clipData = clipboard.getContents(null);
						if (clipData
								.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							Object text = clipData
									.getTransferData(DataFlavor.stringFlavor);
							if (StringUtils.isBlank("" + text)) {
								return;
							}
							UIOutput.getInstance().setSourceText(
									text.toString());
							String translate = TranslationReceiver.INSTANCE
									.translateAndFormat(text.toString(), false);
							UIOutput.getInstance().setTargetText(translate);
							if (ClipboardObserver.getInstance().actionListener != null) {
								try {
									ClipboardObserver.getInstance().actionListener
											.execute(text.toString());
								} catch (Exception ex) {
									logger.error(ex.getMessage());
								}
							}
							UIOutput.getInstance().restore();
						}
					}
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}
}
