package gtranslator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.commons.lang3.StringUtils;

public class TranslationReceiver {
	public enum METHOD {
		GET, POST
	}

	private String cookie;
	private boolean isAddition;
	public final static TranslationReceiver INSTANCE = new TranslationReceiver();

	private TranslationReceiver() {
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public void setAddition(boolean isAddition) {
		this.isAddition = isAddition;
	}

	public String execute(String sentense, String cookie, boolean isGetMethod)
			throws IOException {
		if (isGetMethod) {
			return getExecute(sentense, cookie);
		} else {
			return postExecute(sentense, cookie);
		}
	}

	public synchronized String execute(String sentense, boolean isGetMethod)
			throws IOException {
		if (isGetMethod) {
			return getExecute(sentense, cookie);
		} else {
			return postExecute(sentense, cookie);
		}
	}

	private String getExecute(String sentense, String cookie)
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

	private String postExecute(String sentense, String cookie)
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

	public String format(String translate) {
		return format(translate, isAddition);
	}

	public String format(String translate, boolean isAll) {
		Map<String, Result> results = parseTranslate(translate);
		StringBuilder sb = new StringBuilder();
		Result res = results.get("0.1.1");
		for (Result ch : res.childs) {
			sb.append(ch.datas.get(0));
			sb.append("\n");
		}

		String ts = sb.toString().replaceAll(
				new String(Character.toChars(160)), "");
		String[] tss = ts.split("\n");
		sb.delete(0, sb.length());
		for (String s : tss) {
			if (StringUtils.isBlank(s)) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(s.startsWith(" ") ? s.substring(1) : s);
		}
		ts = sb.toString();

		if (!isAll) {
			return ts;
		}

		sb.delete(0, sb.length());
		sb.append(ts);

		sb.append("\n**********\n");

		res = results.get("0.1.5");
		int index = 1;
		boolean isDrawLine = false;
		for (Result ch : res.childs) {
			if (isDrawLine) {
				sb.append("----------\n");
			} else {
				isDrawLine = true;
			}
			Set<String> uniqWords = new HashSet<>();
			for (String s : ch.datas) {
				String st = s.trim();
				if (st.isEmpty() || uniqWords.contains(st))
					continue;
				sb.append(st);
				sb.append("\n");
				uniqWords.add(st);
			}
			Result rus = results.get("0.1.5." + index + ".1");
			for (Result ch2 : rus.childs) {
				for (String s : ch2.datas) {
					if (s.trim().isEmpty())
						continue;
					sb.append("  ");
					sb.append(s);
					sb.append("\n");
				}
			}
			index++;
		}

		return sb.toString();
	}

	private static Map<String, Result> parseTranslate(String translate) {
		while (translate.indexOf(",,") != -1) {
			translate = translate.replaceAll(",,", ",[],");
		}
		Map<String, Result> resMap = new HashMap<>();
		Reader reader = new StringReader(translate);
		JsonParser parser = Json.createParser(reader);
		Result current = new Result();
		while (parser.hasNext()) {
			JsonParser.Event e;
			e = parser.next();
			if (e == Event.START_ARRAY) {
				Result newData = new Result();
				newData.parent = current;
				current.childs.add(newData);
				newData.index = current.childs.size();
				current = newData;
				resMap.put(current.getComplexIndex(), current);
			} else if (e == Event.END_ARRAY && current.parent != null) {
				current = current.parent;
			} else if (e == Event.VALUE_STRING) {
				current.datas.add(parser.getString());
			}
		}

		return resMap;
	}

	public static void main1(String[] args) throws IOException {
		String sentence = s2;
		String cookie = "PREF=ID=eb1b92938bb56d0a:U=fe67bf9c92332070:FF=0:LD=ru:NW=1:TM=1419582403:LM=1432111202:GM=1:SG=2:S=L0BJCcJgRJ-97cSl; NID=67=KotPOyK2nrutho0P-sHb-Ubbv5vam6QinJn4rCRQbJJNgsph9-z6vCTUvjzkmItrtCIw1cP9FvtdkurKyt-gVs8MExJSQiQe7bpro4xiAb8jDHnCP1HNBxv1hp2lZz-qIuI5Pk859QHlh_FwnWsytRRfP4cErl7g7ErcuIZBuvQdwyL-kq45dbrSWFnOQt4ciMh7ozu4HsCFqgmowhkQIsee3SPNQGzYUpcaqIZjThfrPntaH42tKQcbLMBkesdCW6t1; SID=DQAAAP0AAAC2ePkxVGlZmOxwv9WccRtJhzzmmuZ2v6satIx_qOHgaEqRn_lqMGQ-hrnlO-xzdR-zG5WvJN9YcYRk3ENogNhkmaUz3MnIal1LjE-1drJsTATuyfTMYl_fIBAuA14EW0pCG42Abt4479higkk83ICgb8FnQojIA6xM1g51WOKNohf9hLaskBcUCLfBzuxF2ZDN8-xrZxzmP75TDbob3WNRhwtMdMKLYp4LU--wFeZ3vFlox_b7Xs90X8x1RCPzpjoNTrr5e0Iug9B_hAA0jIRTZ6-7axoqCEGJ-lO0ZSufKqZr1t2vnBE_a701ac45aWsiCsN4y6yucaubb7nkiglU; HSID=AWeTGGuwEMKQgf-J7; APISID=dIRfBsR7yg9PF55k/AH_lGYc85_jVtnOHq; OGPC=4061155-4:; _ga=GA1.3.911712002.1432041668; GOOGLE_ABUSE_EXEMPTION=ID=264a1d4b203382c8:TM=1432115998:C=c:IP=176.104.37.229-:S=APGng0v4qyjwi1fshUcKAOt91zojWBCufA";
		String translate = new TranslationReceiver().execute(sentence, cookie,
				false);
		// System.out.println(format(translate, true));
	}

	static String s = "This works well when the initialization value is available and the initialization can be put on one"
			+ " line. However, this form of initialization has limitations because of its simplicity. If initialization"
			+ " requires some logic (e.g., error handling or a for loop to fill a complex array), simple assignment is"
			+ " inadequate. Instance variables can be initialized in constructors, where error handling or other logic"
			+ " can be used. To provide the same capability for class variables, the Java programming language"
			+ " includes static initialization blocks.";

	static String s2 = "This works well when the initialization value is available and the initialization can be put on one line.";

	/*
	 * private static void recursion(Result content, int indent) { char[] spaces
	 * = new char[indent]; Arrays.fill(spaces, ' '); if
	 * (!content.datas.isEmpty() && indent > 1) { int order = 0; for (String s :
	 * content.datas) { if (s.isEmpty()) continue; System.out.println();
	 * System.out.print(content.getComplexIndex() + ":" + order + " ");
	 * System.out.print(indent); System.out.print(spaces); System.out.print(s);
	 * order++; } }
	 * 
	 * for (Result ch : content.childs) { recursion(ch, indent + 1); } }
	 */

}
