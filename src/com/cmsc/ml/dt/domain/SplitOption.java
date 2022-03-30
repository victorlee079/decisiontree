package com.cmsc.ml.dt.domain;

import java.io.Serializable;
import java.util.List;

public class SplitOption implements Serializable {
	private static final long serialVersionUID = -8684068936103437466L;
	
	private String attrName;
	private Value<?>[] vals;
	private double gini;
	private List<Record> leftList;
	private List<Record> rightList;

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public Value<?>[] getVals() {
		return vals;
	}

	public void setVals(Value<?>[] vals) {
		this.vals = vals;
	}

	public double getGini() {
		return gini;
	}

	public void setGini(double gini) {
		this.gini = gini;
	}

	public List<Record> getLeftList() {
		return leftList;
	}

	public void setLeftList(List<Record> leftList) {
		this.leftList = leftList;
	}

	public List<Record> getRightList() {
		return rightList;
	}

	public void setRightList(List<Record> rightList) {
		this.rightList = rightList;
	}
}
