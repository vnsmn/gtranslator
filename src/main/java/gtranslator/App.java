package gtranslator;

import gtranslator.translate.TranslationReceiver;
import gtranslator.ui.UIOutput;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class App {
	private static final Logger logger = Logger.getLogger(App.class);

	private static Thread clipboardThread;

	public static void main(String[] args) {
		logger.info("hi");

		try {
			loadProperties(args);
			applyProperties();
			loadHistory();
		} catch (IOException | ParseException | java.text.ParseException ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(-1);
		}

		clipboardThread = new Thread(ClipboardObserver.getInstance());
		clipboardThread.start();
	}

	public static void stopClipboardThread() throws InterruptedException {
		clipboardThread.interrupt();
		clipboardThread.join(10000);
	}

	private static void loadProperties(String... args) throws IOException,
			ParseException {
		// String[] args2 =
		// {"--prop-path=\"/home/vns/workspace/gtranslator/settings.xml\""};

		Options options = new Options();
		options.addOption("p", "prop-path", true, "");

		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);
		String path = line.getOptionValue("prop-path", "");
		logger.info("The path of properties file is " + path
				+ " by --prop-path");

		AppProperties.getInstance().load(path);
	}

	private static void loadHistory() throws IOException {
		HistoryHelper.INSTANCE.load();
		UIOutput.getInstance().setStatistic(
				HistoryHelper.INSTANCE.getStatistic());
		HistoryHelper.INSTANCE
				.setStatisticListener(new HistoryHelper.StatisticListener() {
					@Override
					public void execute(String message) {
						UIOutput.getInstance().setStatistic(
								HistoryHelper.INSTANCE.getStatistic());
					}
				});
	}

	private static void applyProperties() throws IOException,
			java.text.ParseException, ParseException {
		logger.info("********** properties **********");

		logger.info("----- cookie -----");
		String cookie = AppProperties.getInstance().getCookie();
		if (!StringUtils.isBlank(cookie)) {
			TranslationReceiver.INSTANCE.setCookie(cookie);
			UIOutput.getInstance().setCookie(cookie);
		}
		logger.info(cookie);

		logger.info("----- history -----");
		boolean isHistory = AppProperties.getInstance().isHistory();
		TranslationReceiver.INSTANCE.setHistory(isHistory);
		UIOutput.getInstance().setHistory(isHistory);
		logger.info(isHistory);

		logger.info("----- dictionary.target.dir -----");
		String dirPath = AppProperties.getInstance().getDictionaryDirPath();
		UIOutput.getInstance().setDictionaryDir(dirPath);
		logger.info(dirPath);

		logger.info("----- dictionary.pause.seconds -----");
		int pauseSeconds = AppProperties.getInstance()
				.getDictionaryPauseSeconds();
		DictionaryHelper.INSTANCE.setPauseSeconds(pauseSeconds);
		logger.info(pauseSeconds);

		logger.info("----- dictionary.defis.seconds -----");
		int defisSeconds = AppProperties.getInstance()
				.getDictionaryDefisSeconds();
		DictionaryHelper.INSTANCE.setDefisSeconds(defisSeconds);
		logger.info(defisSeconds);

		logger.info("----- dictionary.block.limit -----");
		int blockLimit = AppProperties.getInstance().getDictionaryBlockLimit();
		DictionaryHelper.INSTANCE.setBlockLimit(blockLimit);
		logger.info(blockLimit);
		
		UIOutput.getInstance().setActivityClipboard(true);
		UIOutput.getInstance().setModeClipboard(false);

		logger.info("********************");
	}
}