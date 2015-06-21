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

import org.apache.log4j.Logger;

/*
 * grant { permission java.awt.AWTPermission "accessClipboard" };
 */
public class ClipboardObserver implements Runnable, ClipboardOwner {
	private boolean isLostData = true;
	private AtomicBoolean isPause = new AtomicBoolean(false);
	private AtomicBoolean isSelected = new AtomicBoolean(false);
	private AtomicBoolean isUsingHistory = new AtomicBoolean(false);
	private static ClipboardObserver instance;

	private ActionListener actionListener;
	
	static final Logger logger = Logger.getLogger(ClipboardObserver.class);

	public interface ActionListener {
		void execute(String text);
	}
	
	private ClipboardObserver() {
	}
	
	public static ClipboardObserver getInstance() {
		if (instance == null) {
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
		while (!Thread.interrupted()) {
			try {
				if (isLostData && !isPause.get()) {
					synchronized (this) {
						Clipboard clipboard = isSelected.get() ? selClipboard
								: copyClipboard;
						Transferable clipData = clipboard.getContents(null);
						if (clipData
								.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							Object text = clipData
									.getTransferData(DataFlavor.stringFlavor);
							StringSelection st = new StringSelection(
									text.toString());
							UIOutput.getInstance().setSourceText(
									text.toString());
							String translate = TranslationReceiver.INSTANCE
									.translateAndFormat(text.toString(), false);
							UIOutput.getInstance().setTargetText(
									translate);
							UIOutput.getInstance().selectTranslatePanel();
							if (actionListener != null) {
								try {
									actionListener.execute(text.toString());
								} catch (Exception ex) {
									logger.error(ex.getMessage());
								}
							}
							clipboard.setContents(st, this);
							isLostData = false;
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

	public synchronized void setSelected(boolean b) {
		isSelected.set(b);
		isLostData = true;
	}

	public synchronized void setActionListener(ActionListener l) {
		actionListener = l;
	}
	
	public synchronized boolean isSupportSoundWord() {
		return actionListener != null;
	}
}
