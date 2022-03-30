package com.cmsc.ml.dt.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputData {
	private List<Record> records;
	private Map<String, Attribute> attrDesc = new HashMap<>();

	public List<Record> getRecords() {
		return records;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}

	public Map<String, Attribute> getAttrDesc() {
		return attrDesc;
	}

	public void setAttrDesc(Map<String, Attribute> attrDesc) {
		this.attrDesc = attrDesc;
	}
}
