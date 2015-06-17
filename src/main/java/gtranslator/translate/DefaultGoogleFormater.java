package gtranslator.translate;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class DefaultGoogleFormater {
	protected Tree parse(String s) {
		while (s.indexOf(",,") != -1) {
			s = s.replaceAll(",,", ",[],");
		}
		JsonParser jsp = JsonProvider.provider().createParser(new StringReader(s));
		Tree current = new Tree();
		while (jsp.hasNext()) {
			try {
				Event evt = jsp.next();
				if (evt.compareTo(Event.START_ARRAY) == 0) {
					current = current.add();
				}
				if (evt.compareTo(Event.END_ARRAY) == 0) {
					current = current.parent;
				}
				if (evt.compareTo(Event.VALUE_STRING) == 0) {
					current.content.add(jsp.getString());
				}
				if (evt.compareTo(Event.VALUE_NUMBER) == 0) {
					current.content.add(jsp.getBigDecimal());
				}
			} catch (javax.json.stream.JsonParsingException ex) {
				ex.printStackTrace();
			}
		}		
		return current;
	}

	public static class Tree {
		public Tree parent;
		public List<Object> content = new ArrayList<>();

		public Tree add() {
			Tree tree = new Tree();
			tree.parent = this;
			content.add(tree);
			return tree;
		}

		public void print() {
			if (content.isEmpty()) {
				return;
			}
			System.out.println("[");
			for (Object val : content) {
				if (Tree.class.equals(val.getClass())) {
					((Tree) val).print();
				} else {
					System.out.print(val);
					System.out.print(";");
				}
			}
			System.out.println("]");
		}
	}
	
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
		sb.append("\n");
		t = (Tree) t.content.get(1);		
		for (Object l : t.content) {			
			if (l instanceof Tree) {
				Tree t1 = (Tree) l;
				sb.append("-----");
				sb.append(t1.content.get(0));
				sb.append("-----\n");
				Tree t2 = (Tree) t1.content.get(1);
				for (Object txt : t2.content) {
					sb.append("\t");
					sb.append(txt);
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}			
}
