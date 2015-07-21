package gtranslator;

import gtranslator.Actions.ModeTClipboardAction;
import gtranslator.Actions.StartStopTClipboardAction;
import gtranslator.annotation.Singelton;
import gtranslator.sound.GoogleSoundReceiverService;
import gtranslator.sound.OxfordReceiverService;
import gtranslator.translate.TranslationService;
import gtranslator.ui.UIOutput;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;

import com.google.common.base.Predicates;

public class App {
	private static final Logger logger = Logger.getLogger(App.class);

	private static Thread clipboardThread;

	public static void main(String[] args) throws InvocationTargetException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		logger.info("hi");

		try {
			loadProperties(args);
			File f = new File(AppProperties.getInstance()
					.getDictionaryDirPath());
			if (!f.exists()) {
				f.mkdirs();
			}
			instantiate();
			HistoryService historyService = Registry.INSTANCE
					.get(HistoryService.class);
			UIOutput uiOutput = Registry.INSTANCE.get(UIOutput.class);
			uiOutput.setStatistic(historyService.getStatistic());
			boolean isStart = AppProperties.getInstance().getClipboardActive();
			Actions.findAction(StartStopTClipboardAction.class)
					.execute(isStart);
			String mode = AppProperties.getInstance().getClipboardMode();
			Actions.findAction(ModeTClipboardAction.class).execute(
					ClipboardObserver.MODE.valueOf(mode));
		} catch (IOException | ParseException ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(-1);
		}

		clipboardThread = new Thread(App.getClipboardObserver());
		clipboardThread.start();
	}

	public static void close() throws InterruptedException {
		clipboardThread.interrupt();
		clipboardThread.join(10000);
		
		for (Object obj : Registry.INSTANCE.gets()) {
			if (obj instanceof Configurable) {
				((Configurable) obj).close();
			}
		}
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

	@SuppressWarnings("unchecked")
	private static void instantiate() throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Reflections reflections = new Reflections(App.class.getPackage()
				.getName(), new MethodAnnotationsScanner(),
				new FieldAnnotationsScanner());
		Object[] args = new Object[0];

		Set<Method> singeltons = reflections
				.getMethodsAnnotatedWith(Singelton.class);
		for (Method m : singeltons) {
			m.invoke(m.getClass(), args);
		}

		Set<Class<? extends Configurable>> configurations = reflections
				.getSubTypesOf(Configurable.class);
		for (Class<? extends Configurable> tp : configurations) {
			Configurable cnf = (Configurable) Registry.INSTANCE.get(tp);
			if (cnf == null) {
				Set<Constructor> allConstructors = ReflectionUtils
						.getConstructors(tp, Predicates.and(
								ReflectionUtils.withModifier(Modifier.PUBLIC),
								ReflectionUtils.withParametersCount(0)));
				if (allConstructors.isEmpty()) {
					throw new RuntimeException("The class " + tp
							+ " has not public constructor without parameters.");
				}
				for (Constructor<?> c : allConstructors) {
					cnf = (Configurable) c.newInstance();
					cnf.init(AppProperties.getInstance());
					Registry.INSTANCE.add(cnf);
				}
			}
		}
		Set<Field> allFields = reflections
				.getFieldsAnnotatedWith(Resource.class);
		for (Field f : allFields) {
			Object value = Registry.INSTANCE.get(f.getType());
			Object obj = Registry.INSTANCE.get(f.getDeclaringClass());
			if (obj == null) {
				throw new RuntimeException("The file " + f.getName()
						+ " of class " + f.getDeclaringClass()
						+ " has not registry object of "
						+ f.getDeclaringClass());
			}
			if (value == null) {
				throw new RuntimeException("The class " + f.getName()
						+ " of class " + f.getDeclaringClass()
						+ " has not registry object of " + f.getType());
			}
			f.setAccessible(true);
			f.set(obj, value);
		}

		for (Object obj : Registry.INSTANCE.gets()) {
			if (obj instanceof Configurable) {
				((Configurable) obj).init(AppProperties.getInstance());
			}
		}
	}

	public static UIOutput getUIOutput() {
		return Registry.INSTANCE.get(UIOutput.class);
	}

	public static HistoryService getHistoryService() {
		return Registry.INSTANCE.get(HistoryService.class);
	}

	public static ClipboardObserver getClipboardObserver() {
		return Registry.INSTANCE.get(ClipboardObserver.class);
	}

	public static TranslationService getTranslationService() {
		return Registry.INSTANCE.get(TranslationService.class);
	}
	
	public static DictionaryService getDictionaryService() {
		return Registry.INSTANCE.get(DictionaryService.class);
	}
	
	public static OxfordReceiverService getOxfordReceiverService() {
		return Registry.INSTANCE.get(OxfordReceiverService.class);
	}
	
	public static GoogleSoundReceiverService getGoogleSoundReceiverService() {
		return Registry.INSTANCE.get(GoogleSoundReceiverService.class);
	}
	
	public static H2Service getH2Service() {
		return Registry.INSTANCE.get(H2Service.class);
	}
}