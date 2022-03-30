package com.cmsc.ml.dt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Config {
	public static final String COMMA_DELIMITER = ",";
	public static final int POST_LABEL_INDEX = 13;
	public static final Map<String, Class<?>> ATTR_MAP = new LinkedHashMap<>();
	public static final List<String> LABELS = new ArrayList<>();
	public static final int THRESHOLD = 400;

	static {
		ATTR_MAP.put("age", Long.class);
		ATTR_MAP.put("workclass", String.class);
		ATTR_MAP.put("fnlwgt", Long.class);
		ATTR_MAP.put("education", String.class);
		ATTR_MAP.put("education-num", Long.class);
		ATTR_MAP.put("marital-status", String.class);
		ATTR_MAP.put("occupation", String.class);
		ATTR_MAP.put("relationship", String.class);
		ATTR_MAP.put("race", String.class);
		ATTR_MAP.put("sex", String.class);
		ATTR_MAP.put("capital-gain", Long.class);
		ATTR_MAP.put("capital-loss", Long.class);
		ATTR_MAP.put("hours-per-week", Long.class);

		LABELS.add(">50K");
		LABELS.add("<=50K");
	}

	public static final int NUM_OF_THREADS = 4;
	public static final boolean IS_BUILD = true;

	private static final String TREE_FILE_NAME_PREFIX = "./build/dt";
	private static final String TREE_FILE_NAME_EXT = ".bin";

	private static final String LOG_DIR = "./log/";
	private static final String LOG_FILE_EXT = ".log";

	public static final String TRAIN_DATA_FILE = "./data/train/adult.data";
	public static final String TEST_DATA_FILE = "./data/test/adult.test";

	public static String getLogFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date timeNow = Calendar.getInstance().getTime();
		return LOG_DIR + sdf.format(timeNow) + "_" + THRESHOLD + LOG_FILE_EXT;
	}

	public static String getTreeBinFileName() {
		return TREE_FILE_NAME_PREFIX + THRESHOLD + TREE_FILE_NAME_EXT;
	}
}
