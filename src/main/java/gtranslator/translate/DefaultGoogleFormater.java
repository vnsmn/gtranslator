package gtranslator.translate;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class DefaultGoogleFormater {
	private List<List<String>> lastVariantWords = new ArrayList<>();

	protected Tree parse(String s) {
		lastVariantWords.clear();
		while (s.indexOf(",,") != -1) {
			s = s.replaceAll(",,", ",[],");
		}
		JsonParser jsp = JsonProvider.provider().createParser(
				new StringReader(s));
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

	public String format(String jsonText, boolean isAddition) {
		Tree root = parse(jsonText);
		StringBuilder sb = new StringBuilder();
		Iterator<Tree> itHt = root.first(Tree.class).first(Tree.class)
				.iterator(Tree.class);
		while (itHt.hasNext()) {
			String s = itHt.next().first();
			sb.append(s);
			lastVariantWords.add(Arrays.asList(s));
		}
		if (!isAddition) {
			return sb.toString();
		}
		sb.append("\n");
		Tree t = root.first(Tree.class);
		boolean isWordType = Tree.class == t.get(1).getClass();
		if (isWordType) {
			t = t.get(1);
			Iterator<Tree> it = t.iterator(Tree.class);
			while (it.hasNext()) {
				Tree t1 = it.next();
				sb.append("-----");
				sb.append(t1.first().toString());
				sb.append("-----\n");
				Iterator<String> it2 = t1.first(Tree.class).iterator(
						String.class);
				List<String> wds = new ArrayList<String>();
				while (it2.hasNext()) {
					sb.append("  ");
					String wd = it2.next();
					sb.append(wd);
					wds.add(wd);
					sb.append("\n");
				}
				lastVariantWords.add(wds);
			}
		}
		t = root.first(Tree.class);
		boolean isWordVariants = t.get(5) != null
				&& Tree.class == t.get(5).getClass();
		if (isWordVariants) {
			t = t.get(5);
			Set<String> dublicates = new HashSet<String>();
			Iterator<Tree> it = t.iterator(Tree.class);
			while (it.hasNext()) {
				Tree t1 = it.next();
				String s = t1.first();
				if (!s.matches(".*[a-zA-Z]+.*") || dublicates.contains(s)) {
					continue;
				}
				sb.append("-----");
				sb.append(s);
				dublicates.add(s);
				sb.append("-----\n");
				List<String> wds = new ArrayList<String>();
				Iterator<Tree> it2 = t1.first(Tree.class).iterator(Tree.class);
				while (it2.hasNext()) {
					Iterator<String> it3 = it2.next().iterator(String.class);					
					while (it3.hasNext()) {
						sb.append("  ");
						String wd = it3.next();
						sb.append(wd);
						wds.add(wd);
						sb.append("\n");
					}
				}
				lastVariantWords.add(wds);
			}
		}
		return sb.toString();
	}

	public List<List<String>> getLastVariantWords() {
		return Collections.unmodifiableList(lastVariantWords);
	}

	public String formatSimple(List<List<String>> words) {
		Set<String> dublicates = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		for (List<String> l : words) {
			for (String s : l) {
				if (dublicates.contains(s)) {
					break;
				}
				if (sb.length() > 0) {
					sb.append(";");
				}
				sb.append(s);
				dublicates.add(s);				
				break;
			}
		}
		return sb.toString();
	}

	public static class Tree {
		private Tree parent;
		private List<Object> content = new ArrayList<>();

		public Tree add() {
			Tree tree = new Tree();
			tree.parent = this;
			content.add(tree);
			return tree;
		}

		public Tree getParent() {
			return parent;
		}

		@SuppressWarnings("unchecked")
		public <T> T get(int index) {
			return content.isEmpty() || content.size() <= index ? null
					: (T) content.get(index);
		}

		@SuppressWarnings("unchecked")
		public <T> T get(int index, Class<T> clazz) {
			int i = index;
			for (Object obj : content) {
				if (clazz.equals(obj.getClass()) && i-- == 0) {
					return (T) obj;
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public <T> T first() {
			return content.isEmpty() ? null : (T) content.get(0);
		}

		public <T> T first(Class<T> clazz) {
			return get(0, clazz);
		}

		@SuppressWarnings("unchecked")
		public <T> Iterator<T> iterator(Class<T> clazz) {
			final Iterator<T> iterator;
			List<T> lst = new ArrayList<>();
			for (Object t : content) {
				if (clazz == t.getClass()) {
					lst.add((T) t);
				}
			}
			iterator = lst.iterator();
			return new Iterator<T>() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public T next() {
					return iterator.next();
				}
			};
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
}
