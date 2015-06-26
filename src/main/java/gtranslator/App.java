package gtranslator;

import gtranslator.Actions.ModeTClipboardAction;
import gtranslator.Actions.StartStopTClipboardAction;
import gtranslator.translate.TranslationReceiver;
import gtranslator.ui.UIOutput;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		logger.info("hi");

//		javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					loadProperties(args);
//					applyProperties();
//					loadHistory();
//				} catch (IOException | ParseException | java.text.ParseException ex) {
//					logger.error(ex.getMessage(), ex);
//					System.exit(-1);
//				}				
//			}		
//		});
		
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

		logger.info("----- dictionary.block.limit -----");
		int blockLimit = AppProperties.getInstance().getDictionaryBlockLimit();
		UIOutput.getInstance().setDictionaryBlockLimit(blockLimit);
		logger.info(blockLimit);

		logger.info("----- dictionary.result.dir -----");
		String resultDir = AppProperties.getInstance().getDictionaryResultDir();
		UIOutput.getInstance().setDictionaryResultDir(resultDir);
		logger.info(resultDir);

		logger.info("----- dictionary.pronunciation -----");
		String pronunciation = AppProperties.getInstance()
				.getDictionaryPronunciation();
		UIOutput.getInstance().setDictionaryPronunciation(pronunciation);
		logger.info(resultDir);
		
		logger.info("----- dictionary.pause.seconds -----");
		Integer pauseSeconds = AppProperties.getInstance()
				.getDictionaryPauseSeconds();
		UIOutput.getInstance().setDictionaryPauseSeconds(pauseSeconds);
		logger.info(pauseSeconds);
		
		logger.info("----- dictionary.defis.seconds -----");
		Integer defisSeconds = AppProperties.getInstance()
				.getDictionaryDefisSeconds();
		UIOutput.getInstance().setDictionaryDefisSeconds(defisSeconds);
		logger.info(defisSeconds);
		
		logger.info("----- dictionary.synthesizer -----");
		boolean synthesizer = AppProperties.getInstance()
				.isDictionarySynthesizer();
		UIOutput.getInstance().setDictionarySynthesizer(synthesizer);
		logger.info(synthesizer);

		logger.info("----- clipboard.active -----");
		boolean isStart = AppProperties.getInstance().getClipboardActive();
		Actions.findAction(StartStopTClipboardAction.class).execute(isStart);
		UIOutput.getInstance().setActivityClipboard(isStart);
		logger.info(isStart);

		logger.info("----- clipboard.mode -----");
		String mode = AppProperties.getInstance().getClipboardMode();
		UIOutput.getInstance().setModeClipboard(ClipboardObserver.MODE.valueOf(mode));
		Actions.findAction(ModeTClipboardAction.class).execute(ClipboardObserver.MODE.valueOf(mode));
		logger.info(mode);

		logger.info("********************");
	}
}