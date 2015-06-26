package gtranslator.translate;

import gtranslator.DictionaryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class OxfordPhonReceiver {
	static final Logger logger = Logger.getLogger(DictionaryHelper.class);
	private static final String REQUEST = "http://www.oxfordlearnersdictionaries.com/definition/english/%s_1?q=%s";

	private static Properties phonProperies = new Properties();
	private static File phonFile;
	static {
		String dir = System.getProperty("user.home");
		phonFile = new File(dir, "gtranslator-oxford-phon.xml");
		if (phonFile.exists())
			try (FileInputStream in = new FileInputStream(phonFile)) {
				phonProperies.loadFromXML(in);
			} catch (Exception ex) {
				logger.error(ex);
			}
	}

	public enum PHONE {
		AM, BR
	}

	private static Map<PHONE, String> getPhons(String request)
			throws IOException {
		Map<PHONE, String> refs = new HashMap<>();
		Document doc = Jsoup.connect(request).timeout(3000).get();
		Elements elements = doc
				.select("div[class=\"top-container\"] div[resource=phonetics][class=\"pron-gs ei-g\"] span[geo=br].pron-g span.prefix:contains(BrE) + span[class=phon]");
		if (elements.size() > 0) {
			NEXT:
			for (Element el : elements) {
				for (Node n : el.childNodes()) {
					if (n instanceof TextNode) {
						refs.put(PHONE.BR, ((TextNode) n).getWholeText());
						break NEXT;
					}
				}
			}
		}
		elements = doc
				.select("div[class=\"top-container\"] div[resource=phonetics] span[geo=n_am].pron-g span.prefix:contains(NAmE) + span[class=phon]");
		if (elements.size() > 0) {
			NEXT:
			for (Element el : elements) {
				for (Node n : el.childNodes()) {
					if (n instanceof TextNode) {
						refs.put(PHONE.AM, ((TextNode) n).getWholeText());
						break NEXT;
					}
				}
			}
		}
		return refs;
	}

	public synchronized static String get(String word, PHONE phone) {
		String normal = TranslationReceiver.INSTANCE.toNormal(word);
		if (!phonProperies.containsKey(normal)) {
			receive(normal);
		}
		String s = phonProperies.getProperty(normal);
		if (StringUtils.isBlank(s)) {
			return null;
		}
		String[] ss = s.split("[=]");
		if (ss.length == 0) {
			return null;
		}
		return phone == PHONE.AM ? ss[0] : ss[1];
	}

	public synchronized static void receive(String word) {
		try {
			Map<PHONE, String> phons = getPhons(String.format(REQUEST, word,
					word));
			String am = phons.get(PHONE.AM);
			String br = phons.get(PHONE.BR);
			phonProperies.put(word, StringUtils.trimToEmpty(am) + "="
					+ StringUtils.trimToEmpty(br));
			save();

		} catch (org.jsoup.HttpStatusException ex) {
			phonProperies.put(word, "");
			logger.error(ex);
		} catch (IOException ex) {
			logger.error(ex);
		}
	}

	private static void save() {
		try {
			if (!phonFile.exists()) {
				phonFile.createNewFile();
			}
			try (FileOutputStream out = new FileOutputStream(phonFile)) {
				phonProperies.storeToXML(out, new Date().toString(), "UTF-8");
			}
		} catch (IOException ex) {
			logger.error(ex);
		}
	}

	public static void main(String... args) throws IOException {
		String s = "am";
		System.out.println(OxfordPhonReceiver.get(s, PHONE.AM));
		System.out.println(OxfordPhonReceiver.get(s, PHONE.BR));
	}
}
