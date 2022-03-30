package com.cmsc.ml.dt.domain;

import java.io.Serializable;

import com.cmsc.ml.dt.utils.StringUtils;

public class TreeNode implements Serializable {
	private static final long serialVersionUID = 7679013338284339653L;
	
	// Yes
	private TreeNode left;
	// No
	private TreeNode right;

	private String attrName;

	private SplitOption option;

	// Null -> leaf node
	private Predicate predicate;

	public TreeNode() {

	}

	public TreeNode(String attrName, Predicate predicate) {
		this.attrName = attrName;
		this.predicate = predicate;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	public void setLeft(TreeNode left) {
		this.left = left;
	}

	public void setRight(TreeNode right) {
		this.right = right;
	}

	public SplitOption getOption() {
		return option;
	}

	public void setOption(SplitOption option) {
		this.option = option;
	}

	public String predict(Record rec) {
		if (predicate != null) {
			boolean result = predicate.predict(rec.get(attrName));
			if (result) {
				return left.predict(rec);
			} else {
				return right.predict(rec);
			}
		} else {
			return this.attrName;
		}
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(50);
		print(buffer, "", "");
		return buffer.toString();
	}

	private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
		buffer.append(prefix);
		buffer.append(attrName);
		if (option != null) {
			buffer.append(" : ");
			buffer.append(StringUtils.listToString(option.getVals()));
		}
		buffer.append('\n');

		if (this.left != null) {
			this.left.print(buffer, childrenPrefix + "|-- ", childrenPrefix + "|   ");
		}
		if (this.right != null) {
			this.right.print(buffer, childrenPrefix + "|-- ", childrenPrefix + "    ");
		}

	}
}
