package gtranslator.sound;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;

public class LameSoundHelper {
	public final static LameSoundHelper INSTANCE = new LameSoundHelper();

	public enum PROP {
		QUALITY
	}
	
	private LameSoundHelper() {		
	}

	public void convert(String waveFile, String mp3File) throws IOException {
		convert(waveFile, mp3File, Collections.emptyMap());
	}
	
	public void convert(String waveFile, String mp3File, Map<PROP, String> settings) throws IOException {
		String quality = StringUtils.defaultIfBlank(settings.get(PROP.QUALITY), "128");
		Map<String, String> map = new HashMap<String, String>();
		map.put("in_file", waveFile);
		map.put("out_file", mp3File);
		CommandLine cmdLine = new CommandLine("lame");
		cmdLine.addArgument("-b");
		cmdLine.addArgument(quality);
		cmdLine.addArgument("-h");
		cmdLine.addArgument("${in_file}", false);
		cmdLine.addArgument("${out_file}", false);
		cmdLine.setSubstitutionMap(map);
		DefaultExecutor executor = new DefaultExecutor();
		PumpStreamHandler pumpStreamHandler = new PumpStreamHandler();
		executor.setExitValue(0);
		executor.setStreamHandler(pumpStreamHandler);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		System.out.println(cmdLine.toString());
		int exitValue = executor.execute(cmdLine);
		if (executor.isFailure(exitValue)) {
			System.err.println("error: " + exitValue);
		}
	}
	
	public static void main(String... args) throws Exception {
		INSTANCE.convert(
				"/ext/english/learningenglish.voanews.com/LinkedIn EF Offer Test Scores for English Learners/words-sound-ru-1-1-1.wave",
				"/ext/english/learningenglish.voanews.com/LinkedIn EF Offer Test Scores for English Learners/words-sound-ru-1-1-1.mp3");
	}
}
