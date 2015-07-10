package gtranslator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Registry {
	private Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
	public static Registry INSTANCE = new Registry();

	private Registry() {
	}

	public void add(Object inst) {
		if (!instances.containsKey(inst.getClass())) {
			instances.put(inst.getClass(), inst);
		}
	}

	public Collection<Object> gets() {
		return Collections.unmodifiableCollection(instances.values());
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		return (T) instances.get(clazz);
	}
}
