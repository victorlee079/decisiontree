package com.cmsc.ml.dt.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.cmsc.ml.dt.domain.Predicate;
import com.cmsc.ml.dt.domain.Value;

public class PredicateFactory {
	private final static PredicateFactory factory = new PredicateFactory();

	private PredicateFactory() {

	}

	public static PredicateFactory getInstance() {
		return factory;
	}

	public <T> Predicate createPredicate(@SuppressWarnings("unchecked") Value<T>... vals) {
		Value<T> firstVal = vals[0];
		T t = firstVal.getVal();
		if (t instanceof Long) {
			long v = (long) t;
			return (a) -> ((long) a.getVal() <= v);
		} else if (t instanceof String) {
			List<String> valList = new ArrayList<>();
			valList.addAll(Arrays.asList(vals).stream().map(v -> (String) v.getVal()).collect(Collectors.toList()));
			return (a) -> (valList.contains((String) a.getVal()));
		}
		return (a) -> false;
	}
}
