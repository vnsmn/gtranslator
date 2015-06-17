package gtranslator.translate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class DictGoogleFormater extends DefaultGoogleFormater {

	@Override
	public String format(String jsonText, boolean isAddition) {
		Tree root = parse(jsonText);
		StringBuilder sb = new StringBuilder();
		Tree t = ((Tree) root.content.get(0));
		t = (Tree) t.content.get(1);
		for (Object l : t.content) {
			if (sb.length() > 0) {
				sb.append(";");
			}
			if (l instanceof Tree) {
				Tree t1 = (Tree) l;
				t1 = (Tree) t1.content.get(2);
				int i = 0;
				for (Object l1 : t1.content) {
					if (l1 instanceof Tree) {
						Tree t2 = (Tree) l1;
						if (i++ > 0) {
							sb.append(",");
						}
						sb.append(t2.content.get(0));
						if (!isAddition) {
							break;
						}
					}
				}
			}
		}
		return sb.toString();
	}
}
