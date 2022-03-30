package com.cmsc.ml.dt.build;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cmsc.ml.dt.Config;
import com.cmsc.ml.dt.Constants;
import com.cmsc.ml.dt.domain.Attribute;
import com.cmsc.ml.dt.domain.InputData;
import com.cmsc.ml.dt.domain.Record;
import com.cmsc.ml.dt.domain.Value;

public class DataProcessor {
	public InputData readData(String fileName) throws FileNotFoundException, IOException {
		System.out.println("[DataProcessor] Reading data from: " + fileName);
		InputData data = new InputData();
		List<Record> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] valArr = line.trim().split(Config.COMMA_DELIMITER);
				List<String> values = cleanse(valArr);
				if (values != null) {
					// Label index after cleansing = 13
					Record rec = new Record(values, Config.POST_LABEL_INDEX, Config.ATTR_MAP);
					records.add(rec);
					updateMetaData(rec, data.getAttrDesc());
				}
			}
		}
		data.setRecords(records);
		System.out.println("[DataProcessor] Finished reading");
		return data;
	}

	@SuppressWarnings("unchecked")
	private void updateMetaData(Record rec, Map<String, Attribute> meta) {
		Map<String, Value<?>> attrs = rec.getAttrs();
		Set<String> attrNames = attrs.keySet();
		for (String attrName : attrNames) {
			Value<?> value = attrs.get(attrName);
			Attribute attr = meta.getOrDefault(attrName, new Attribute(attrName));

			if (value.getVal() instanceof Long) {
				attr.setType(Constants.TYPE.ORDINAL);
				Set<Long> distinctValues = (Set<Long>) attr.getDistinctValues();
				if (distinctValues == null) {
					distinctValues = new HashSet<>();
				}
				distinctValues.add((Long) value.getVal());
				attr.setDistinctValues(distinctValues);
			} else if (value.getVal() instanceof String) {
				attr.setType(Constants.TYPE.NOMINAL);
				Set<String> distinctValues = (Set<String>) attr.getDistinctValues();
				if (distinctValues == null) {
					distinctValues = new HashSet<>();
				}
				distinctValues.add((String) value.getVal());
				attr.setDistinctValues(distinctValues);
			}

			meta.put(attrName, attr);
		}
	}

	private List<String> cleanse(String[] attributes) {
		if (attributes.length != 15) {
			return null;
		}

		List<String> result = new ArrayList<>();
		for (int i = 0; i < attributes.length; i++) {
			if (i == 13) {
				// Remove attribute "native-country"
				continue;
			}
			String attr = attributes[i].trim();
			// Remove dot in label
			if (i == 14) {
				attr = attr.replace(".", "");
			}
			// Remove all records containing "?"
			if (!attr.equals("?")) {
				result.add(attr);
			} else {
				return null;
			}
		}
		return result;
	}
}
