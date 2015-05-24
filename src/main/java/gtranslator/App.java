package gtranslator;

import gtranslator.ClipboardObserver;
import gtranslator.GuiOutput;
import gtranslator.TranslationReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class App {
	static final Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("hi");

		final Properties props;
		try {
			//String[] args2 = {"--prop-path=\"/home/vns/workspace/gtranslator/settings.xml\""};
			props = loadProperties(args);			
			logger.info("********** properties **********");
			String cookie = props.getProperty("cookie", "").replaceAll("\n", "");			
			if (!StringUtils.isBlank(cookie)) {
				logger.info("----- cookie -----");
				logger.info(cookie);
				TranslationReceiver.INSTANCE.setCookie(cookie);
			}			
			logger.info("********************");
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(-1);
		}
		
		//if (1==1) return ;

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

	private static Properties loadProperties(String... args)
			throws FileNotFoundException, IOException, ParseException {
		Options options = new Options();
		options.addOption("p", "prop-path", true, "");

		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);
		String path = line.getOptionValue("prop-path", "");
		if (path.trim().startsWith("\"")) {
			path = path.substring(1, path.length() - 1);
		}
		Properties props = new Properties();
		if (StringUtils.isBlank(path)) {
			path = System.getProperty("user.dir");			
		}
		if (!StringUtils.isBlank(path)) {
			File f = new File(path);
			if (f.isDirectory()) {
				f = new File(f, "settings.xml");
			}
			try (FileInputStream fis = new FileInputStream(f)) {
				props.loadFromXML(fis);
			}
		}
		
		return props;
	}
}
