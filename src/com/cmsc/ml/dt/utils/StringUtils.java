package com.cmsc.ml.dt.utils;

import com.cmsc.ml.dt.domain.Value;

public class StringUtils {
	public static String listToString(Value<?>[] splitValues) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < splitValues.length; i++) {
			sb.append(splitValues[i].getVal().toString());
			if (i < splitValues.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
}
