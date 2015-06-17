package gtranslator.translate;

import gtranslator.HistoryHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Formatter.BigDecimalLayoutForm;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.json.Json;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class TranslationReceiver {
	public enum METHOD {
		GET, POST
	}

	static final Logger logger = Logger.getLogger(TranslationReceiver.class);

	private AtomicReference<String> cookie = new AtomicReference<String>("");
	private AtomicBoolean isAddition = new AtomicBoolean(false);
	private AtomicBoolean isRewrite = new AtomicBoolean(false);
	private AtomicBoolean isHistory = new AtomicBoolean(false);
	private WordGoogleFormater wordGoogleFormater = new WordGoogleFormater();
	private PhraseGoogleFormater phraseGoogleFormater = new PhraseGoogleFormater();
	public final static TranslationReceiver INSTANCE = new TranslationReceiver();	

	private TranslationReceiver() {
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
		String normal = toNormal(sentense);
		if (isHistory.get()) {
			String rawTranslate = HistoryHelper.INSTANCE.readRaw(normal);
			if (isRewrite.get() || StringUtils.isBlank(rawTranslate)) {
				rawTranslate = isGetMethod ? executeGet(normal, cookie.get())
						: executePost(normal, cookie.get());
				HistoryHelper.INSTANCE.writeRaw(normal, rawTranslate);
			}			
			if (normal.matches("[a-zA-Z]+")) {
				String translateWords = wordGoogleFormater.format(rawTranslate, true);
				HistoryHelper.INSTANCE.writeWord(normal, translateWords);
				return translateWords;
			} else {
				return phraseGoogleFormater.format(rawTranslate, isAddition);
			}
		} else {
			String rawTranslate = isGetMethod ? executeGet(normal, cookie.get())
					: executePost(normal, cookie.get());
			return phraseGoogleFormater.format(rawTranslate, isAddition);
		}
	}

	public String toNormal(String s) {
		int i = 0;
		int j = s.length() - 1;
		for (; i < s.length(); i++) {
			if (Character.isLetter(s.charAt(i))) {
				break;
			}
		}
		for (; j > 0; j--) {
			if (Character.isLetter(s.charAt(j))) {
				break;
			}
		}
		try {
			return s.substring(i, j + 1).replaceAll("[ ]+", " ").toLowerCase();
		} catch (StringIndexOutOfBoundsException ex) {
			logger.error(ex.getMessage() + " : " + s);
			throw ex;
		}
	}

	private static class Result {
		Result parent;
		int index;
		List<Result> childs = new ArrayList<Result>();
		List<String> datas = new ArrayList<String>();

		String getComplexIndex() {
			if (parent != null)
				return parent.getComplexIndex() + "." + index;
			else
				return "" + index;
		}
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		String sentence = "This works well when the initialization value is available and the initialization can be put on one"
				+ " line. However, this form of initialization has limitations because of its simplicity. If initialization"
				+ " requires some logic (e.g., error handling or a for loop to fill a complex array), simple assignment is"
				+ " inadequate. Instance variables can be initialized in constructors, where error handling or other logic"
				+ " can be used. To provide the same capability for class variables, the Java programming language"
				+ " includes static initialization blocks.";
		String cookie = "PREF=ID=eb1b92938bb56d0a:U=fe67bf9c92332070:FF=0:LD=ru:NW=1:TM=1419582403:LM=1432111202:GM=1:SG=2:S=L0BJCcJgRJ-97cSl; NID=67=KotPOyK2nrutho0P-sHb-Ubbv5vam6QinJn4rCRQbJJNgsph9-z6vCTUvjzkmItrtCIw1cP9FvtdkurKyt-gVs8MExJSQiQe7bpro4xiAb8jDHnCP1HNBxv1hp2lZz-qIuI5Pk859QHlh_FwnWsytRRfP4cErl7g7ErcuIZBuvQdwyL-kq45dbrSWFnOQt4ciMh7ozu4HsCFqgmowhkQIsee3SPNQGzYUpcaqIZjThfrPntaH42tKQcbLMBkesdCW6t1; SID=DQAAAP0AAAC2ePkxVGlZmOxwv9WccRtJhzzmmuZ2v6satIx_qOHgaEqRn_lqMGQ-hrnlO-xzdR-zG5WvJN9YcYRk3ENogNhkmaUz3MnIal1LjE-1drJsTATuyfTMYl_fIBAuA14EW0pCG42Abt4479higkk83ICgb8FnQojIA6xM1g51WOKNohf9hLaskBcUCLfBzuxF2ZDN8-xrZxzmP75TDbob3WNRhwtMdMKLYp4LU--wFeZ3vFlox_b7Xs90X8x1RCPzpjoNTrr5e0Iug9B_hAA0jIRTZ6-7axoqCEGJ-lO0ZSufKqZr1t2vnBE_a701ac45aWsiCsN4y6yucaubb7nkiglU; HSID=AWeTGGuwEMKQgf-J7; APISID=dIRfBsR7yg9PF55k/AH_lGYc85_jVtnOHq; OGPC=4061155-4:; _ga=GA1.3.911712002.1432041668; GOOGLE_ABUSE_EXEMPTION=ID=264a1d4b203382c8:TM=1432115998:C=c:IP=176.104.37.229-:S=APGng0v4qyjwi1fshUcKAOt91zojWBCufA";
		String s = "[[[\"тест\",\"test\",\"test\",\"\"]],[[\"имя существительное\",[\"тест\",\"испытание\",\"проверка\",\"анализ\",\"проба\",\"мерило\",\"критерий\",\"контрольная работа\",\"проверочная работа\",\"исследование\",\"опыт\",\"реакция\",\"реактив\"],[[\"тест\",[\"test\",\"reaction\",\"test paper\"],,0.20961139],[\"испытание\",[\"test\",\"trial\",\"touch\",\"try\",\"assay\",\"checkout\"],,0.15335497],[\"проверка\",[\"check\",\"verification\",\"test\",\"examination\",\"review\",\"checkup\"],,0.015666196],[\"анализ\",[\"analysis\",\"assay\",\"test\",\"scan\",\"dissection\",\"anatomy\"],,0.015666196],[\"проба\",[\"try\",\"sample\",\"test\",\"trial\",\"probe\",\"assay\"]],[\"мерило\",[\"measure\",\"yardstick\",\"criterion\",\"standard\",\"test\",\"metewand\"]],[\"критерий\",[\"criterion\",\"test\",\"measure\",\"norm\",\"touchstone\",\"yardstick\"]],[\"контрольная работа\",[\"test\"]],[\"проверочная работа\",[\"test\"]],[\"исследование\",[\"study\",\"research\",\"investigation\",\"survey\",\"examination\",\"test\"]],[\"опыт\",[\"experience\",\"experiment\",\"practice\",\"attempt\",\"essay\",\"test\"]],[\"реакция\",[\"reaction\",\"response\",\"anticlimax\",\"answer\",\"test\"]],[\"реактив\",[\"reagent\",\"chemical agent\",\"test\"]]],\"test\",1],[\"глагол\",[\"тестировать\",\"проверять\",\"испытывать\",\"подвергать испытанию\",\"подвергать проверке\",\"производить опыты\"],[[\"тестировать\",[\"test\"],,0.029729217],[\"проверять\",[\"check\",\"verify\",\"check out\",\"test\",\"check up\",\"control\"],,0.017476905],[\"испытывать\",[\"test\",\"experience\",\"feel\",\"have\",\"tempt\",\"undergo\"],,0.011461634],[\"подвергать испытанию\",[\"put to test\",\"test\",\"try\",\"put to the proof\",\"essay\",\"tax\"]],[\"подвергать проверке\",[\"test\"]],[\"производить опыты\",[\"test\",\"experiment\",\"experimentalize\",\"experimentalise\"]]],\"test\",2],[\"имя прилагательное\",[\"испытательный\",\"контрольный\",\"проверочный\",\"пробный\"],[[\"испытательный\",[\"test\",\"trial\",\"probationary\",\"probatory\"],,0.016418032],[\"контрольный\",[\"controlling\",\"check\",\"test\",\"pilot\",\"checking\",\"master\"]],[\"проверочный\",[\"checking\",\"checkup\",\"test\"]],[\"пробный\",[\"trial\",\"test\",\"pilot\",\"tentative\",\"experimental\",\"specimen\"]]],\"test\",3]],\"en\",,[[\"тест\",[1],true,false,1000,0,1,0]],[[\"test\",1,[[\"тест\",1000,true,false],[\"испытание\",0,true,false],[\"испытания\",0,true,false],[\"проверка\",0,true,false],[\"тестирование\",0,true,false]],[[0,4]],\"test\"]],,,[],29]";
		TranslationReceiver.INSTANCE.setCookie(cookie);
		s = TranslationReceiver.INSTANCE.executePost(sentence, cookie);
		System.out.println(s);
		s = new PhraseGoogleFormater().format(s, true);
		System.out.println(s);	
	}	
}
