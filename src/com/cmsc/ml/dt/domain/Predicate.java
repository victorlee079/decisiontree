package com.cmsc.ml.dt.domain;

import java.io.Serializable;

@FunctionalInterface
public interface Predicate extends Serializable {
	public boolean predict(Value<?> val);
}
