package com.cmsc.ml.dt;

public interface Constants {
	public static interface TYPE {
		public static final String ORDINAL = "O";
		public static final String NOMINAL = "N";
		public static final String LABEL = "L";
	}
	
	public static interface ARGUMENTS {
		public static final String TRAIN = "-t";
		public static final String EVAL = "-e";
		public static final String THRESHOLD = "-n";
		public static final String USE_MODEL = "-m";
	}
}
