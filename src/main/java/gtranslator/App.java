package gtranslator;

import gtranslator.ClipboardObserver;
import gtranslator.GuiOutput;
import gtranslator.GuiOutput.ACTION_TYPE;
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
				GuiOutput.createAndShowGUI().init(ACTION_TYPE.COOKIE, cookie);
			}			
			logger.info("********************");
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(-1);
		}
		
		readHistory();

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
									String translate = TranslationReceiver.INSTANCE
											.translateAndFormat(source.getSourceText(), false);
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
								TranslationReceiver.INSTANCE.setCookie(s);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.REWRITE_HISTORY,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(boolean b) {
								TranslationReceiver.INSTANCE.setRewrite(b);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.CLEAN_HISTORY,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(GuiOutput source) {
								HistoryHelper.INSTANCE.delete(source.getSourceText());
								try {
									String translate = TranslationReceiver.INSTANCE
											.translateAndFormat(source.getSourceText(), false);
									source.setTargetText(translate);
								} catch (IOException e) {
									logger.error(e.getMessage(), e);
								}
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
									saveHistory();									
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
	
	private static File rawHisFile;
	private static File wordHisFile;
	static {
		String dir = System.getProperty("user.home");
		rawHisFile = new File(dir, "gtranslator-raw-his.xml");
		wordHisFile = new File(dir, "gtranslator-word-his.xml");
	}
	
	private static void saveHistory() {
		try {
			HistoryHelper.INSTANCE.save(rawHisFile, wordHisFile);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
	
	private static void readHistory() {
		try {
			HistoryHelper.INSTANCE.load(rawHisFile, wordHisFile);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
}
