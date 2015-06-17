package gtranslator.translate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class WordGoogleFormater extends DefaultGoogleFormater {
	
	@Override
	public String format(String jsonText, boolean isAddition) {
		Tree root = parse(jsonText);
		StringBuilder sb = new StringBuilder();
		Tree t = ((Tree) root.content.get(0));
		Tree ht = (Tree) t.content.get(0);
		ht = (Tree) ht.content.get(0);
		sb.append(ht.content.get(0));
		if (!isAddition) {
			return sb.toString();
		}		
		sb.append("\n----------\n");
		t = (Tree) t.content.get(1);
		for (Object l : t.content) {
			if (l instanceof Tree) {
				Tree t1 = (Tree) l;
				sb.append("-----");
				sb.append(t1.content.get(0));
				sb.append("-----\n");
				t1 = (Tree) t1.content.get(2);
				for (Object l1 : t1.content) {
					if (l1 instanceof Tree) {
						Tree t2 = (Tree) l1;
						sb.append("  ");
						sb.append(t2.content.get(0));
						if (t2.content.size() > 3) { 
							sb.append(" - ");
							BigDecimal bd = (BigDecimal) t2.content.get(t2.content.size() - 1);
							MathContext mc = new MathContext(2, RoundingMode.UP);
							sb.append(bd.round(mc).multiply(new BigDecimal(100), mc).intValue());
						}
						sb.append("\n");
					}	
				}			
			}
		}
		return sb.toString();
	}
}
