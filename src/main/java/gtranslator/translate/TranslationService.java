package gtranslator.translate;

import gtranslator.AppProperties;
import gtranslator.Configurable;
import gtranslator.HistoryService;
import gtranslator.Registry;
import gtranslator.annotation.Singelton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class TranslationService implements Configurable {
	public enum METHOD {
		GET, POST
	}

	static final Logger logger = Logger.getLogger(TranslationService.class);

	private AtomicReference<String> cookie = new AtomicReference<String>("");
	private AtomicBoolean isAddition = new AtomicBoolean(false);
	private AtomicBoolean isRewrite = new AtomicBoolean(false);
	private AtomicBoolean isHistory = new AtomicBoolean(false);
	private DefaultGoogleFormater defaultGoogleFormater = new DefaultGoogleFormater();
	@Resource
	private HistoryService historyService;

	@Singelton
	public static void createSingelton() {
		Registry.INSTANCE.add(new TranslationService());
	}

	private TranslationService() {
	}

	public void setCookie(String cookie) {
		this.cookie.set(StringUtils.isBlank(cookie) ? "" : cookie);
	}

	public void setAddition(boolean isAddition) {
		this.isAddition.set(isAddition);
	}

	public void setRewrite(boolean isRewrite) {
		this.isRewrite.set(isRewrite);
	}

	public void setHistory(boolean isHistory) {
		this.isHistory.set(isHistory);
	}

	private String executeGet(String sentense, String cookie)
			throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "t");
		params.put("text", sentense);
		params.put("sl", "auto");
		params.put("tl", "ru");

		String request = "http://translate.google.com/translate_a/t?"
				+ createParameters(params);
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(15000);
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		conn.setRequestProperty("Cookie", cookie);
		conn.setUseCaches(false);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String decodedString;
		StringBuilder sb = new StringBuilder();
		while ((decodedString = in.readLine()) != null) {
			// System.out.println(decodedString);
			sb.append(decodedString);
		}
		in.close();
		return sb.toString();
	}

	private String executePost(String sentense, String cookie)
			throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "t");
		params.put("text", sentense);
		params.put("sl", "auto");
		params.put("tl", "ru");

		byte[] postData = createParameters(params).getBytes(
				StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		String request = "http://translate.google.com/translate_a/t";
		URL url = new URL(request);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(15000);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length",
				Integer.toString(postDataLength));
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
		conn.setRequestProperty("Cookie", cookie);
		// conn.setRequestProperty( "Connection", "keep-alive");
		conn.setUseCaches(false);
		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String decodedString;
		StringBuilder sb = new StringBuilder();
		while ((decodedString = in.readLine()) != null) {
			// System.out.println(decodedString);
			sb.append(decodedString);
			sb.append("\n");
		}
		in.close();
		return sb.toString();
	}

	private String createParameters(Map<String, String> params)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> ent : params.entrySet()) {
			sb.append(URLEncoder.encode(ent.getKey(), "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(ent.getValue(), "UTF-8"));
			sb.append("&");
		}
		return sb.toString();
	}

	public synchronized String translateAndFormat(String sentense,
			boolean isGetMethod) throws IOException {
		return translateAndFormat(sentense, isGetMethod, isAddition.get());
	}

	public synchronized String translateAndFormat(String sentense,
			boolean isGetMethod, boolean isAddition) throws IOException {
		return defaultGoogleFormater.format(translate(sentense, isGetMethod),
				isAddition);
	}

	public synchronized String translateAndSimpleFormat(String sentense,
			boolean isGetMethod) throws IOException {
		return defaultGoogleFormater.formatSimple(translate(sentense,
				isGetMethod));
	}

	protected synchronized String translate(String sentense, boolean isGetMethod)
			throws IOException {
		String normal = toNormal(sentense);
		if (isHistory.get()) {
			String rawTranslate = historyService.readRaw(normal);
			if (isRewrite.get() || StringUtils.isBlank(rawTranslate)) {
				rawTranslate = isGetMethod ? executeGet(normal, cookie.get())
						: executePost(normal, cookie.get());
				historyService.writeRaw(normal, rawTranslate);
			}
			return rawTranslate;
		} else {
			return isGetMethod ? executeGet(normal, cookie.get())
					: executePost(normal, cookie.get());
		}
	}

	public String toNormal(String s) {
		String s1 = s;
		int i = 0;
		int j = s1.length() - 1;
		for (; i < s1.length(); i++) {
			if (Character.isLetter(s1.charAt(i))) {
				break;
			}
		}
		for (; j > 0; j--) {
			if (Character.isLetter(s1.charAt(j))) {
				break;
			}
		}
		try {
			return s1.substring(i, j + 1).replaceAll("[ ]+", " ").toLowerCase();
		} catch (StringIndexOutOfBoundsException ex) {
			logger.error(ex.getMessage() + " : " + s);
			return "";
		}
	}

	public static void main(String[] args) throws IOException,
			URISyntaxException {
		// String sentence =
		// "This works well when the initialization value is available and the initialization can be put on one"
		// +
		// " line. However, this form of initialization has limitations because of its simplicity. If initialization"
		// +
		// " requires some logic (e.g., error handling or a for loop to fill a complex array), simple assignment is"
		// +
		// " inadequate. Instance variables can be initialized in constructors, where error handling or other logic"
		// +
		// " can be used. To provide the same capability for class variables, the Java programming language"
		// + " includes static initialization blocks.";
		// String cookie =
		// "PREF=ID=eb1b92938bb56d0a:U=fe67bf9c92332070:FF=0:LD=ru:NW=1:TM=1419582403:LM=1432111202:GM=1:SG=2:S=L0BJCcJgRJ-97cSl; NID=67=KotPOyK2nrutho0P-sHb-Ubbv5vam6QinJn4rCRQbJJNgsph9-z6vCTUvjzkmItrtCIw1cP9FvtdkurKyt-gVs8MExJSQiQe7bpro4xiAb8jDHnCP1HNBxv1hp2lZz-qIuI5Pk859QHlh_FwnWsytRRfP4cErl7g7ErcuIZBuvQdwyL-kq45dbrSWFnOQt4ciMh7ozu4HsCFqgmowhkQIsee3SPNQGzYUpcaqIZjThfrPntaH42tKQcbLMBkesdCW6t1; SID=DQAAAP0AAAC2ePkxVGlZmOxwv9WccRtJhzzmmuZ2v6satIx_qOHgaEqRn_lqMGQ-hrnlO-xzdR-zG5WvJN9YcYRk3ENogNhkmaUz3MnIal1LjE-1drJsTATuyfTMYl_fIBAuA14EW0pCG42Abt4479higkk83ICgb8FnQojIA6xM1g51WOKNohf9hLaskBcUCLfBzuxF2ZDN8-xrZxzmP75TDbob3WNRhwtMdMKLYp4LU--wFeZ3vFlox_b7Xs90X8x1RCPzpjoNTrr5e0Iug9B_hAA0jIRTZ6-7axoqCEGJ-lO0ZSufKqZr1t2vnBE_a701ac45aWsiCsN4y6yucaubb7nkiglU; HSID=AWeTGGuwEMKQgf-J7; APISID=dIRfBsR7yg9PF55k/AH_lGYc85_jVtnOHq; OGPC=4061155-4:; _ga=GA1.3.911712002.1432041668; GOOGLE_ABUSE_EXEMPTION=ID=264a1d4b203382c8:TM=1432115998:C=c:IP=176.104.37.229-:S=APGng0v4qyjwi1fshUcKAOt91zojWBCufA";
		// String s =
		// "[[[\"тест\",\"test\",\"test\",\"\"]],[[\"имя существительное\",[\"тест\",\"испытание\",\"проверка\",\"анализ\",\"проба\",\"мерило\",\"критерий\",\"контрольная работа\",\"проверочная работа\",\"исследование\",\"опыт\",\"реакция\",\"реактив\"],[[\"тест\",[\"test\",\"reaction\",\"test paper\"],,0.20961139],[\"испытание\",[\"test\",\"trial\",\"touch\",\"try\",\"assay\",\"checkout\"],,0.15335497],[\"проверка\",[\"check\",\"verification\",\"test\",\"examination\",\"review\",\"checkup\"],,0.015666196],[\"анализ\",[\"analysis\",\"assay\",\"test\",\"scan\",\"dissection\",\"anatomy\"],,0.015666196],[\"проба\",[\"try\",\"sample\",\"test\",\"trial\",\"probe\",\"assay\"]],[\"мерило\",[\"measure\",\"yardstick\",\"criterion\",\"standard\",\"test\",\"metewand\"]],[\"критерий\",[\"criterion\",\"test\",\"measure\",\"norm\",\"touchstone\",\"yardstick\"]],[\"контрольная работа\",[\"test\"]],[\"проверочная работа\",[\"test\"]],[\"исследование\",[\"study\",\"research\",\"investigation\",\"survey\",\"examination\",\"test\"]],[\"опыт\",[\"experience\",\"experiment\",\"practice\",\"attempt\",\"essay\",\"test\"]],[\"реакция\",[\"reaction\",\"response\",\"anticlimax\",\"answer\",\"test\"]],[\"реактив\",[\"reagent\",\"chemical agent\",\"test\"]]],\"test\",1],[\"глагол\",[\"тестировать\",\"проверять\",\"испытывать\",\"подвергать испытанию\",\"подвергать проверке\",\"производить опыты\"],[[\"тестировать\",[\"test\"],,0.029729217],[\"проверять\",[\"check\",\"verify\",\"check out\",\"test\",\"check up\",\"control\"],,0.017476905],[\"испытывать\",[\"test\",\"experience\",\"feel\",\"have\",\"tempt\",\"undergo\"],,0.011461634],[\"подвергать испытанию\",[\"put to test\",\"test\",\"try\",\"put to the proof\",\"essay\",\"tax\"]],[\"подвергать проверке\",[\"test\"]],[\"производить опыты\",[\"test\",\"experiment\",\"experimentalize\",\"experimentalise\"]]],\"test\",2],[\"имя прилагательное\",[\"испытательный\",\"контрольный\",\"проверочный\",\"пробный\"],[[\"испытательный\",[\"test\",\"trial\",\"probationary\",\"probatory\"],,0.016418032],[\"контрольный\",[\"controlling\",\"check\",\"test\",\"pilot\",\"checking\",\"master\"]],[\"проверочный\",[\"checking\",\"checkup\",\"test\"]],[\"пробный\",[\"trial\",\"test\",\"pilot\",\"tentative\",\"experimental\",\"specimen\"]]],\"test\",3]],\"en\",,[[\"тест\",[1],true,false,1000,0,1,0]],[[\"test\",1,[[\"тест\",1000,true,false],[\"испытание\",0,true,false],[\"испытания\",0,true,false],[\"проверка\",0,true,false],[\"тестирование\",0,true,false]],[[0,4]],\"test\"]],,,[],29]";
		// TranslationReceiver.INSTANCE.setCookie(cookie);
		// s = TranslationReceiver.INSTANCE.executePost("when", cookie);
		// System.out.println(s);
		// DefaultGoogleFormater f = new DefaultGoogleFormater();
		// s = f.format(s, true);
		// System.out.println(s);
		// // s = f.formatSimple(f.getLastVariantWords());
		// // s = new DictGoogleFormater().format(s, true);
		// // s = new WordGoogleFormater().format(s, true);
		// // s = new PhraseGoogleFormater().format(s, true);
		// System.out.println(s);
	}

	@Override
	public void init(AppProperties appProperties) {
		if (!StringUtils.isBlank(appProperties.getCookie())) {
			setCookie(appProperties.getCookie());
		}
		boolean isHistory = appProperties.isHistory();
		setHistory(isHistory);
	}

	@Override
	public void close() {
	}
}
