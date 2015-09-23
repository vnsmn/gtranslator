package gtranslator.sound;

import gtranslator.AppProperties;
import gtranslator.Configurable;
import gtranslator.Registry;
import gtranslator.annotation.Singelton;
import gtranslator.exception.SoundReceiverException;
import gtranslator.ui.Constants;
import gtranslator.ui.Constants.LANG;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class GoogleSoundReceiverService implements Configurable {
    static final Logger logger = Logger
            .getLogger(GoogleSoundReceiverService.class);
    private final static String RUS_REQUEST = "http://translate.google.com/translate_tts?tl=ru&q=%s";
    private final static String ENG_REQUEST = "http://translate.google.com/translate_tts?tl=en&q=%s";
    private File soundEnDir;
    private File soundRuDir;
    private AtomicReference<String> cookie = new AtomicReference<String>("");

    private GoogleSoundReceiverService() {
    }

    @Singelton
    public static void createSingelton() {
        Registry.INSTANCE.add(new GoogleSoundReceiverService());
    }

    public File getSound(String phrase, LANG lang)
            throws SoundReceiverException {
        File soundDir = LANG.RUS == lang ? soundRuDir : soundEnDir;
        File fw = new File(soundDir, phrase + ".mp3");
        boolean isloaded = fw.exists();
        if (!isloaded)
            try {
                isloaded = writeSound(fw, phrase, lang);
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
                throw new SoundReceiverException(ex.getMessage(), ex);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
                throw new SoundReceiverException(ex.getMessage(), ex);
            }
        return isloaded ? fw : null;
    }

    private boolean writeSound(File file, String phrase, LANG lang)
            throws IOException, InterruptedException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
        URL url = new URL(String.format(LANG.RUS == lang ? RUS_REQUEST
                : ENG_REQUEST, URLEncoder.encode(phrase, "UTF-8")));
        HttpURLConnection conn = connect(url, true, false);
        conn.setRequestProperty("Cookie", cookie.get());
        long size = 0;
        try (InputStream in = conn.getInputStream()) {
            size = Files.copy(in, Paths.get(file.toURI()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (FileNotFoundException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            int code = conn.getResponseCode();
            if (code == 503) {
                if (ex.getMessage().startsWith("Server returned HTTP response code: 503 for URL: ")) {
                    String captchaUrl = ex.getMessage().replace("Server returned HTTP response code: 503 for URL: ", "");
                    if (resolve503(captchaUrl)) {
                        return writeSound(file, phrase, lang);
                    }
                    return false;
                }
            }
            logger.error(ex.getMessage());
            throw ex;
        }
        return size > 0;
    }

    public static void main(String... args) throws Exception {
        GoogleSoundReceiverService service = new GoogleSoundReceiverService();
        service.getSound("страна", LANG.RUS);
        service.getSound("страна", LANG.RUS);
    }

    @Override
    public void init(AppProperties appProperties) {
        File dicDir = new File(appProperties.getDictionaryDirPath());
        soundEnDir = new File(dicDir, Constants.EN_SOUND_DIR);
        soundRuDir = new File(dicDir, Constants.RU_SOUND_DIR);
        if (!soundEnDir.exists()) {
            soundEnDir.mkdirs();
        }
        if (!soundRuDir.exists()) {
            soundRuDir.mkdirs();
        }
        if (!StringUtils.isBlank(appProperties.getCookie())) {
            cookie.set(appProperties.getCookie());
        }
    }

    @Override
    public void close() {
    }

    boolean isSendClosedResolve503 = false;
    boolean isClosedResolve503 = false;
    String captchaText = "";

    public boolean resolve503(String request) throws IOException, InterruptedException {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HttpURLConnection indConn = connect(new URL(request));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (InputStream in = indConn.getInputStream()) {
            IOUtils.copy(in, bos);
        } catch (Exception ex) {
            bos.reset();
            IOUtils.copy(indConn.getErrorStream(), bos);
            logger.error(ex.getMessage() + ". URL: ".concat(request));
        }
        Document doc = Jsoup.parse(new String(bos.toByteArray()), "utf-8");
        Elements elements = doc.select("form input[name=id]");
        String postID = elements.size() > 0 ? elements.get(0).attr("value") : "";
        elements = doc.select("form input[name=continue]");
        String postContinue = elements.size() > 0 ? elements.get(0).attr("value") : "";
        elements = doc.select("div img[border=1]");
        String imgUrl = elements.size() > 0 ? "http://ipv4.google.com" + elements.get(0).attr("src") : "";
        if (!StringUtils.isBlank(postID) && !StringUtils.isBlank(postContinue) && !StringUtils.isBlank(imgUrl)) {
            HttpURLConnection imgConn = connect(new URL(imgUrl));
            bos.reset();
            JFrame frame = createFrame(imgConn.getInputStream());
            while (!isClosedResolve503 && !isSendClosedResolve503) {
                Thread.sleep(1000);
            }
            frame.dispose();
            if (isSendClosedResolve503) {
                Map<String, String> post = new HashMap<>();
                post.put("captcha", captchaText);
                post.put("submit", "Submit");
                post.put("id", postID);
                post.put("continue", postContinue);
                String postData = createParameters(post);
                URL captchaUrl = new URL("http://ipv4.google.com/sorry/CaptchaRedirect?" + postData);
                HttpURLConnection captchaConn = connect(captchaUrl);
                String scookie = imgConn.getHeaderFields().get("Set-Cookie").get(0).split(";")[0];
                captchaConn.setRequestProperty("Cookie", scookie);
                int i = captchaConn.getResponseCode();
                if (i == 503) {
                    logger.error("error 503: " + captchaUrl.toString());
                    resolve503(request);
                }
                cookie.set(scookie);
                return true;
            }
        }
        return false;
    }

    private JFrame createFrame(InputStream imageStream) throws IOException {
        BufferedImage bi = ImageIO.read(imageStream);
        JFrame frame = new JFrame();
        ImageIcon imageIcon = new ImageIcon(bi.getScaledInstance(
                bi.getWidth(), bi.getHeight(), 1
        ));
        isClosedResolve503 = false;
        isSendClosedResolve503 = false;
        Box box = Box.createVerticalBox();
        frame.add(box, BorderLayout.CENTER);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel picLabel = new JLabel(imageIcon);
        panel.add(picLabel);
        box.add(panel);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JTextField field = new JTextField();
        panel.add(field);
        box.add(panel);
        panel = new JPanel();

        JButton button = new JButton();
        button.setText("send");
        panel.add(button);
        box.add(panel);
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captchaText = field.getText();
                isSendClosedResolve503 = true;
            }
        });

        frame.setSize(bi.getWidth(), bi.getHeight() + 100);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                isClosedResolve503 = true;
            }
        });
        return frame;
    }

    private HttpURLConnection connect(URL url) throws IOException {
        return connect(url, true, true);
    }
    private HttpURLConnection connect(URL url, boolean doIn, boolean  doOut) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setDoInput(doIn);
        conn.setDoOutput(doOut);
        conn.setRequestMethod("GET");
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setUseCaches(false);
        return conn;
    }

    private String createParameters(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> ent : params.entrySet()) {
            sb.append(URLEncoder.encode(ent.getKey(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(ent.getValue(), "UTF-8"));
            sb.append("&");
        }
        String s = sb.toString();
        return s.substring(0, s.length() - 1);
    }
}