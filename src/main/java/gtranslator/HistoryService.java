package gtranslator;

import gtranslator.annotation.Singelton;
import gtranslator.sound.SoundHelper;
import gtranslator.translate.DefaultGoogleFormater;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryService implements Configurable {
    static final Logger logger = Logger.getLogger(HistoryService.class);

    private static File rawHisFile;
    private static LinkedBlockingQueue<String> soundQueue = new LinkedBlockingQueue<>();
    @Resource
    private H2Service h2Service;

    public interface StatisticListener {
        void execute(String message);
    }

    private Properties rawHis; //init only in loadHis()
    private StatisticListener statisticListener;
    private int wordCount;
    private Set<String> runtimeWords = new HashSet<>();

    @Singelton
    public static void createSingelton() {
        Registry.INSTANCE.add(new HistoryService());
    }

    private HistoryService() {
        Thread thread = new Thread(new SoundLoader());
        thread.setDaemon(true);
        thread.start();
    }

    public void restore() throws FileNotFoundException, IOException {
        toDb(rawHis);
    }

    public void toDb(Properties properties) {
        for (Entry<Object, Object> ent : properties.entrySet()) {
            h2Service
                    .addDic(ent.getKey().toString(), ent.getValue().toString());
        }
    }

    public void writeRaw(String key, String value) {
        String w = toNormal(key);
        h2Service.addDic(w, value);
        if (!rawHis.containsKey(w) && isWord(w)) {
            wordCount++;
        }
        rawHis.put(w, value);
        if (statisticListener != null) {
            statisticListener.execute(getStatistic());
        }
        try {
            soundQueue.put(w);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    public String readRaw(String key) {
        String w = toNormal(key);
        if (isWord(w)) {
            runtimeWords.add(w);
        }
        if (statisticListener != null) {
            statisticListener.execute(getStatistic());
        }
        return h2Service.getDic(toNormal(key));
    }

    public void delete(String key) {
        String w = toNormal(key);
        h2Service.deleteDic(w);
        if (rawHis.containsKey(w) && isWord(w)) {
            wordCount--;
        }
        rawHis.remove(w);
        if (statisticListener != null) {
            statisticListener.execute(getStatistic());
        }
    }

    public String getStatistic() {
        loadHis();
        if (wordCount == 0) {
            wordCount = getWords().size();
        }
        return String.format("%d/%d/%d - runtime/word/phrase",
                runtimeWords.size(), wordCount, rawHis.size());
    }

    public boolean isWord(String phrase) {
        return toNormal(phrase).matches("[a-zA-Z]+");
    }

    public Map<String, String> getWords() {
        loadHis();
        Map<String, String> words = new HashMap<>();
        DefaultGoogleFormater formater = new DefaultGoogleFormater();
        String key = "";
        String val = "";
        for (Entry<Object, Object> ent : rawHis.entrySet()) {
            try {
                key = toNormal(ent.getKey().toString());
                val = toNormal(ent.getValue().toString());
                if (key.matches("[a-zA-Z]+")) {
                    formater.format(val, true);
                    String s = formater.formatSimple(val);
                    words.put(key, s);
                }
            } catch (Exception ex) {
                logger.error(key, ex);
                throw ex;
            }
        }
        return Collections.unmodifiableMap(words);
    }

    public Collection getRuntimeWords() {
        return Collections.unmodifiableCollection(runtimeWords);
    }

    public void setStatisticListener(StatisticListener listener) {
        statisticListener = listener;
    }

    private String toNormal(String key) {
        return key.trim().replaceAll("[ ]+", " ").toLowerCase();
    }

    @Override
    public void init(AppProperties appProperties) {
        rawHisFile = Paths.get(
                AppProperties.getInstance().getDictionaryDirPath(), "db",
                "gtranslator-history-backup.xml").toFile();
    }

    @Override
    public void close() {
        try {
            if (!rawHisFile.exists()) {
                rawHisFile.createNewFile();
            }
            try (FileOutputStream out = new FileOutputStream(rawHisFile)) {
                rawHis.storeToXML(out, new Date().toString(), "UTF-8");
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void loadHis() {
        if (rawHis != null) {
            return;
        }
        rawHis = new Properties();
        rawHis.putAll(h2Service.getsDic());
        if (rawHisFile.exists()) {
            try (FileInputStream in = new FileInputStream(rawHisFile)) {
                rawHis.loadFromXML(in);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    static class SoundLoader implements Runnable {
        Pattern pattern = Pattern.compile("([a-zA-Z]+)");

        @Override
        public void run() {
            String eng;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    eng = soundQueue.take();
                    Matcher matcher = pattern.matcher(eng);
                    while (matcher.find()) {
                        String word = matcher.group();
                        if (word.length() > 2) {
                            try {
                                SoundHelper.captureEngWord(matcher.group());
                            } catch (Throwable ex) {
                                logger.error(ex);
                            }
                            Thread.sleep(2000);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.info(e);
                }
            }
        }
    }
}