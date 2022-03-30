package com.cmsc.ml.dt.domain;

import java.io.Serializable;

public class Value<T> implements Serializable {
	private static final long serialVersionUID = 264101984683761931L;

	private T val;

	public T getVal() {
		return val;
	}

	public void setVal(T val) {
		this.val = val;
	}

	@Override
	public String toString() {
		return val.toString();
	}
}
