package com.cmsc.ml.dt.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Record implements Serializable {
	private static final long serialVersionUID = -3345370107408191271L;

	private String label;
	private Map<String, Value<?>> attrs = new HashMap<>();

	public Record(List<String> asList, int labelIdx, Map<String, Class<?>> meta) {
		List<String> keys = new ArrayList<>(meta.keySet());
		int j = 0;
		for (int i = 0; i < asList.size(); i++) {
			String value = asList.get(i);
			if (i == labelIdx) {
				this.label = value;
			} else {
				String attrName = keys.get(j);
				attrs.put(attrName, toValue(value, meta.get(attrName)));
				j++;
			}
		}
	}

	private static Value<?> toValue(String text, Class<?> clazz) {
		if (clazz.equals(Long.class)) {
			Value<Long> val = new Value<>();
			val.setVal(Long.valueOf(text));
			return val;
		} else if (clazz.equals(String.class)) {
			Value<String> val = new Value<>();
			val.setVal(text);
			return val;
		}
		return null;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, Value<?>> getAttrs() {
		return attrs;
	}

	public Value<?> get(String attrName) {
		return attrs.get(attrName);
	}

	public boolean isSameAttribute(Record other, Set<String> keys) {
		if (other == null) {
			return false;
		}

		for (String key : keys) {
			if (!other.get(key).getVal().equals(this.attrs.get(key).getVal())) {
				return false;
			}
		}
		return true;
	}
}
