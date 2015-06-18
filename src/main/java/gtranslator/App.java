package gtranslator;

import gtranslator.ClipboardObserver.ActionListener;
import gtranslator.GuiOutput.ACTION_TYPE;
import gtranslator.sound.SoundHelper;
import gtranslator.translate.TranslationReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class App {
	static final Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) {
		logger.info("hi");

		try {
			applyProperties(args);
		} catch (IOException | ParseException | java.text.ParseException ex) {
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
											.translateAndFormat(
													source.getSourceText(),
													false);
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
								HistoryHelper.INSTANCE
										.delete(TranslationReceiver.INSTANCE
												.toNormal(source
														.getSourceText()));
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.USE_HISTORY,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(boolean b) {
								TranslationReceiver.INSTANCE.setHistory(b);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.DICTIONARY,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(String s) {
								GuiOutput.createAndShowGUI().setWaitCursor();
								try {
									saveHistory();
									DictionaryHelper.INSTANCE.createDictionary(
											HistoryHelper.INSTANCE.getWords(),
											GuiOutput.createAndShowGUI()
													.getDictionaryDirPath());
								} catch (Exception ex) {
									logger.error(ex.getMessage());
								} finally {
									GuiOutput.createAndShowGUI().setDefCursor();
								}
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.SOUND,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(String text) {
								playWord(text, false);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.LOAD_SOUND,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(String text) {
								playWord(text, true);
							}
						});
				GuiOutput.createAndShowGUI().putActionListener(
						GuiOutput.ACTION_TYPE.IS_SOUND,
						new GuiOutput.ActionListener() {
							@Override
							public void execute(boolean b) {
								if (b) {
									clipboardObserver
											.setActionListener(new ActionListener() {
												@Override
												public void execute(String text) {
													playWord(text, false);
												}
											});
								} else {
									clipboardObserver.setActionListener(null);
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
			throws IOException, ParseException {
		Options options = new Options();
		options.addOption("p", "prop-path", true, "");

		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);
		String path = line.getOptionValue("prop-path", "");
		logger.info("The path of properties file is " + path
				+ " by --prop-path");
		if (path.trim().startsWith("\"")) {
			path = path.substring(1, path.length() - 1);
		}
		Properties props = new Properties();
		if (StringUtils.isBlank(path)) {
			path = System.getProperty("user.dir");
		}
		Properties defProps = new Properties();
		try (InputStream in = App.class.getClassLoader().getResourceAsStream(
				"settings.xml")) {
			defProps.loadFromXML(in);
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
		for (Entry<Object, Object> ent : defProps.entrySet()) {
			props.putIfAbsent(ent.getKey(), ent.getValue());
		}
		return props;
	}

	// private static File rawHisFile;
	// //private static File wordHisFile;
	// static {
	// String dir = System.getProperty("user.home");
	// rawHisFile = new File(dir, "gtranslator-raw-his.xml");
	// //wordHisFile = new File(dir, "gtranslator-word-his.xml");
	// }

	private static void saveHistory() {
		try {
			HistoryHelper.INSTANCE.save();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private static void readHistory() {
		try {
			HistoryHelper.INSTANCE.load();
			GuiOutput.createAndShowGUI().init(ACTION_TYPE.STATISTIC,
					HistoryHelper.INSTANCE.getStatistic());
			HistoryHelper.INSTANCE
					.setStatisticListener(new HistoryHelper.StatisticListener() {
						@Override
						public void execute(String message) {
							GuiOutput.createAndShowGUI().init(
									ACTION_TYPE.STATISTIC,
									HistoryHelper.INSTANCE.getStatistic());
						}
					});
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private static Properties applyProperties(String... args)
			throws IOException, java.text.ParseException, ParseException {
		final Properties props;
		// String[] args2 =
		// {"--prop-path=\"/home/vns/workspace/gtranslator/settings.xml\""};
		props = loadProperties(args);
		logger.info("********** properties **********");

		logger.info("----- cookie -----");
		String cookie = props.getProperty("cookie", "").replaceAll("\n", "");
		if (!StringUtils.isBlank(cookie)) {
			TranslationReceiver.INSTANCE.setCookie(cookie);
			GuiOutput.createAndShowGUI().init(ACTION_TYPE.COOKIE, cookie);
		}
		logger.info(cookie);

		logger.info("----- history -----");
		boolean isHistory = parseBoolean(props.getProperty("history", "")
				.replaceAll("\n", ""));
		TranslationReceiver.INSTANCE.setHistory(isHistory);
		GuiOutput.createAndShowGUI().init(ACTION_TYPE.USE_HISTORY, isHistory);
		logger.info(isHistory);

		logger.info("----- dictionary.target.dir -----");
		String dirPath = props.getProperty("dictionary.target.dir", "")
				.replaceAll(
						"\n",
						System.getProperty("user.home")
								+ "/gtranslator-dictionary");
		dirPath = System.getProperty("user.home") + "/gtranslator-dictionary";
		GuiOutput.createAndShowGUI().init(ACTION_TYPE.DICTIONARY, dirPath);
		logger.info(dirPath);

		logger.info("----- dictionary.pause.seconds -----");
		String pauseSeconds = props
				.getProperty("dictionary.pause.seconds", "2").replaceAll("\n",
						"");
		DictionaryHelper.INSTANCE
				.setPauseSeconds(Integer.valueOf(pauseSeconds));
		logger.info(pauseSeconds);

		logger.info("----- dictionary.block.limit -----");
		String blockLimit = props.getProperty("dictionary.block.limit", "10")
				.replaceAll("\n", "");
		DictionaryHelper.INSTANCE.setBlockLimit(Integer.valueOf(blockLimit));
		logger.info(blockLimit);

		logger.info("********************");
		return props;
	}

	private static boolean parseBoolean(String s)
			throws java.text.ParseException {
		if (!StringUtils.isBlank(s)
				&& !s.toLowerCase().matches("(y|yes|true|on|n|no|false|off)")) {
			throw new java.text.ParseException(s, 0);
		}
		return s.toLowerCase().matches("(y|yes|true|on)");
	}

	private static void playWord(String text, boolean doSoundLoad) {
		String normal = TranslationReceiver.INSTANCE.toNormal(text);
		if (normal.matches("[a-zA-Z]+")) {
			String dicDirPath = GuiOutput.createAndShowGUI()
					.getDictionaryDirPath();
			File f = DictionaryHelper.INSTANCE.findFile(true, dicDirPath,
					normal);
			try {
				if (doSoundLoad && !f.exists()) {
					Map<String, String> words = new HashMap<String, String>();
					words.put(normal, normal);
					Set<String> loaded = DictionaryHelper.INSTANCE.loadSound(
							words, new File(dicDirPath));
					if (loaded.isEmpty()) {
						logger.error("the file " + f.getAbsolutePath()
								+ " not found.");
						return;
					}
				}
				SoundHelper.play(f);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}
		}
	}
}