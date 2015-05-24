package gtranslator;

import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class App {
	static final Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("hi");

		final ClipboardObserver clipboardObserver = new ClipboardObserver();
		final Thread th = new Thread(clipboardObserver);
		th.start();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.FIXED,
						new GuiOutput.ActionListener() {
							public void execute(GuiOutput source) {
								try {
									String dirtyTranslate = TranslationReceiver.INSTANCE
											.execute(source.getSourceText(),
													false);
									String translate = TranslationReceiver.INSTANCE
											.format(dirtyTranslate);
									source.setTargetText(translate);
								} catch (IOException e) {
									logger.error(e.getMessage(), e);
								}
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.START_STOP,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(boolean b) {
								clipboardObserver.setPause(b);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.ADDITION_INFO,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(boolean b) {
								TranslationReceiver.INSTANCE.setAddition(b);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.MODE_SELECT,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(boolean b) {
								clipboardObserver.setSelected(b);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.COOKIE,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(String s) {
								// TranslationReceiver.INSTANCE.setCookie(s);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.DISPOSE,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(GuiOutput source) {
								try {
									th.interrupt();
									th.join(10000);
									logger.info("goodbay");
								} catch (InterruptedException e) {
									logger.error(e.getMessage(), e);
								}
							}
						});
			}
		});
	}
}
