package gtranslator.sound;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.ivona.services.tts.IvonaSpeechCloudClient;
import com.ivona.services.tts.model.CreateSpeechRequest;
import com.ivona.services.tts.model.CreateSpeechResult;
import com.ivona.services.tts.model.Input;
import com.ivona.services.tts.model.Voice;
import gtranslator.AppProperties;
import gtranslator.Configurable;
import gtranslator.Registry;
import gtranslator.annotation.Singelton;
import gtranslator.ui.Constants;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class IvonaReceiverService implements Configurable {
    static final Logger logger = Logger.getLogger(IvonaReceiverService.class);
    private IvonaSpeechCloudClient speechCloud;
    File dicDir;
    File soundBrEnDir;
    File soundAmEnDir;
    File soundRuDir;

    public enum VOICE {
        RU("Tatyana"), BREN("Amy"), AMEN("Joey");

        VOICE(String speaker) {
            this.SPEAKER = speaker;
        }

        final public String SPEAKER;
    }

    private File getSoundDir(VOICE voice) {
        switch (voice) {
            case RU:
                return soundRuDir;
            case BREN:
                return soundBrEnDir;
            case AMEN:
                return soundAmEnDir;
            default:
                throw new IllegalArgumentException(voice.name());
        }
    }

    @Singelton
    public static void createSingelton() {
        Registry.INSTANCE.add(new IvonaReceiverService());
    }

    @Override
    public void init(AppProperties appProperties) {
        speechCloud = new IvonaSpeechCloudClient(
                new PropertiesFileCredentialsProvider(AppProperties.getInstance().getIvonaCredentialsPath()));
        speechCloud.setEndpoint("https://tts.eu-west-1.ivonacloud.com");
        dicDir = new File(appProperties.getDictionaryDirPath());
        soundBrEnDir = new File(dicDir, Constants.BR_SOUND_DIR);
        soundAmEnDir = new File(dicDir, Constants.AM_SOUND_DIR);
        soundRuDir = new File(dicDir, Constants.RU_SOUND_DIR);
        if (!soundBrEnDir.exists()) {
            soundBrEnDir.mkdirs();
        }
        if (!soundAmEnDir.exists()) {
            soundAmEnDir.mkdirs();
        }
        if (!soundRuDir.exists()) {
            soundRuDir.mkdirs();
        }
    }

    @Override
    public void close() {

    }

    public static void main(String[] args) throws Exception {
        String dictionaryDir = "/home/vns/gtranslator-dictionary/";
        AppProperties.getInstance().load(null);
        AppProperties.getInstance().setDictionaryDirPath(dictionaryDir);
        IvonaReceiverService.createSingelton();

        IvonaReceiverService service = Registry.INSTANCE.get(IvonaReceiverService.class);
        service.init(AppProperties.getInstance());
        File f = service.getSound("1", VOICE.RU);
        if (f.exists()) {
            Files.copy(f.toPath(), Paths.get("/tmp/test.mp3"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public File getSound(String text, VOICE speaker) throws Exception {
        File soundFile = new File(getSoundDir(speaker), text + ".mp3");
        if (soundFile.exists()) {
            return soundFile;
        }

        CreateSpeechRequest createSpeechRequest = new CreateSpeechRequest();
        Input input = new Input();
        Voice voice = new Voice();

        voice.setName(speaker.SPEAKER);
        input.setData(text);

        createSpeechRequest.setInput(input);
        createSpeechRequest.setVoice(voice);
        InputStream in = null;
        FileOutputStream outputStream = null;

        try {

            CreateSpeechResult createSpeechResult = speechCloud.createSpeech(createSpeechRequest);

            logger.info("\nSuccess sending request:");
            logger.info(" content type:\t" + createSpeechResult.getContentType());
            logger.info(" request id:\t" + createSpeechResult.getTtsRequestId());
            logger.info(" request chars:\t" + createSpeechResult.getTtsRequestCharacters());
            logger.info(" request units:\t" + createSpeechResult.getTtsRequestUnits());

            logger.info("\nStarting to retrieve audio stream:");

            in = createSpeechResult.getBody();
            outputStream = new FileOutputStream(soundFile);

            byte[] buffer = new byte[2 * 1024];
            int readBytes;

            while ((readBytes = in.read(buffer)) > 0) {
                logger.info(" received bytes: " + readBytes);
                outputStream.write(buffer, 0, readBytes);
            }

            logger.info("\nFile saved: " + soundFile.toString());

        } finally {
            if (in != null) {
                in.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return soundFile;
    }
}
