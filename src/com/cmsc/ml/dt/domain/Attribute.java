package com.cmsc.ml.dt.domain;

import java.util.HashSet;
import java.util.Set;

public class Attribute {
	private String name;
	private String type;
	private Set<?> distinctValues;

	public Attribute(String attrName) {
		this.name = attrName;
	}

	public Attribute(Attribute old) {
		this.name = old.name;
		this.type = old.type;
		this.distinctValues = new HashSet<>();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<?> getDistinctValues() {
		return distinctValues;
	}

	public void setDistinctValues(Set<?> distinctValues) {
		this.distinctValues = distinctValues;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
