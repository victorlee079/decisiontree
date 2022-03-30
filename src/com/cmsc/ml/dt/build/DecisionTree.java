package com.cmsc.ml.dt.build;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import com.cmsc.ml.dt.Config;
import com.cmsc.ml.dt.Constants;
import com.cmsc.ml.dt.domain.Attribute;
import com.cmsc.ml.dt.domain.Record;
import com.cmsc.ml.dt.domain.SplitOption;
import com.cmsc.ml.dt.domain.TreeNode;
import com.cmsc.ml.dt.domain.Value;
import com.cmsc.ml.dt.utils.StringUtils;

public class DecisionTree {
	private TreeNode root;
	private ExecutorService executor;

	public DecisionTree() {

	}

	public void build(List<Record> trainingData, Map<String, Attribute> meta, int threshold)
			throws InterruptedException, ExecutionException {
		this.executor = Executors.newFixedThreadPool(Config.NUM_OF_THREADS);
		this.buildTree(trainingData, meta, threshold);
		this.executor.shutdown();
	}

	private TreeNode buildTree(List<Record> trainingData, Map<String, Attribute> meta, int threshold)
			throws InterruptedException, ExecutionException {
		boolean isSameAttribute = true;
		boolean isSameLabel = true;
		TreeNode node = null;

		Map<String, Long> labelCountMap = new HashMap<>();
		// Set the label of first record to +1
		labelCountMap.put(trainingData.get(0).getLabel(), 1L);
		int i;
		for (i = 1; i < trainingData.size(); i++) {
			Record prev = trainingData.get(i - 1);
			Record curr = trainingData.get(i);

			// Check if all records with same attributes
			if (isSameAttribute && !prev.isSameAttribute(curr, meta.keySet())) {
				isSameAttribute = false;
			}
			// Check if all records with same classes
			if (isSameLabel && !prev.getLabel().equals(curr.getLabel())) {
				isSameLabel = false;
			}
			String label = curr.getLabel();
			long count = labelCountMap.getOrDefault(label, 0L) + 1;
			labelCountMap.put(label, count);
		}

		// Return the label as node
		if (isSameLabel) {
			node = getSameLabelNode(trainingData.get(0));
		}

		// Return the label of majority as node
		// Same Attributes || No more attributes for comparing || Training Set is
		// smaller than threshold
		if (isSameAttribute || meta.isEmpty() || trainingData.size() < threshold) {
			node = getSameAttrRoot(labelCountMap);
		}

		if (node == null) {
			SplitOption option = chooseAttr(trainingData, meta);
			node = new TreeNode();
			node.setAttrName(option.getAttrName());
			node.setPredicate(PredicateFactory.getInstance().createPredicate(option.getVals()));
			node.setOption(option);
			meta.remove(option.getAttrName());
			Map<String, Attribute> leftMeta = new HashMap<>(meta);
			Map<String, Attribute> rightMeta = new HashMap<>(meta);
			updateMeta(leftMeta, option.getLeftList());
			updateMeta(rightMeta, option.getRightList());
			node.setLeft(this.buildTree(option.getLeftList(), leftMeta, threshold));
			node.setRight(this.buildTree(option.getRightList(), rightMeta, threshold));
		}

		this.root = node;
		return node;
	}

	private void updateMeta(Map<String, Attribute> meta, List<Record> records) {
		// Recreate
		Set<String> keys = meta.keySet();
		for (String key : keys) {
			Attribute newAttr = new Attribute(meta.get(key));
			meta.put(key, newAttr);
		}

		for (Record rec : records) {
			for (String key : keys) {
				Attribute attr = meta.get(key);
				Value<?> value = rec.get(key);
				if (value.getVal() instanceof Long) {
					@SuppressWarnings("unchecked")
					Set<Long> distinctValues = (Set<Long>) attr.getDistinctValues();
					distinctValues.add((Long) value.getVal());
				} else if (value.getVal() instanceof String) {
					@SuppressWarnings("unchecked")
					Set<String> distinctValues = (Set<String>) attr.getDistinctValues();
					distinctValues.add((String) value.getVal());
				}
			}
		}
	}

	private SplitOption chooseAttr(List<Record> records, Map<String, Attribute> meta)
			throws InterruptedException, ExecutionException {
		SplitOption result = null;

		List<Future<SplitOption>> options = new ArrayList<>();
		Set<String> attrNames = meta.keySet();

		for (String name : attrNames) {
			Attribute attr = meta.get(name);
			ComputationTask task = new ComputationTask(attr, records);
			options.add(executor.submit(task));
		}

		for (int i = 0; i < options.size(); i++) {
			SplitOption tmp = options.get(i).get();
			if (result == null || (tmp.getGini() < result.getGini())) {
				result = tmp;
			}
		}
		System.out.println("[DecisionTree] Attribute Choosen - " + result.getAttrName() + " | Split Option - "
				+ StringUtils.listToString(result.getVals()) + " | Gini - " + result.getGini() + " | Left - "
				+ result.getLeftList().size() + " | Right - " + result.getRightList().size());
		return result;
	}

	private static class ComputationTask implements Callable<SplitOption> {
		private Attribute attr;
		private List<Record> records;

		public ComputationTask(Attribute attr, List<Record> records) {
			this.attr = attr;
			this.records = records;
		}

		@Override
		public SplitOption call() throws Exception {
			return computeOption(attr, records);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private SplitOption computeOption(Attribute attr, List<Record> records) {
			SplitOption option = null;
			String attrName = attr.getName();

			switch (attr.getType()) {
			case Constants.TYPE.NOMINAL:
				List<List<String>> combinations = computeCombination(new ArrayList(attr.getDistinctValues()));
				for (List<String> combination : combinations) {
					SplitOption tmp = computeNominalOption(attrName, combination, records);
					if (option == null || tmp.getGini() < option.getGini()) {
						option = tmp;
					}
				}
				break;
			case Constants.TYPE.ORDINAL:
				List<Record> copiedList = new ArrayList<>(records);
				copiedList.sort(new Comparator<Record>() {
					@Override
					public int compare(Record o1, Record o2) {
						Long v1 = (Long) o1.get(attrName).getVal();
						Long v2 = (Long) o2.get(attrName).getVal();
						return v1.compareTo(v2);
					}
				});
				List<Long> ordinalValues = new ArrayList(attr.getDistinctValues());
				Collections.sort(ordinalValues);

				int len = ordinalValues.size();

				long[] mts = new long[len];
				long[] chkCnts = new long[len];

				long mt = 0L;
				long chkCnt = 0L;

				int valIdx = 0;

				for (int i = 0; i < copiedList.size(); i++) {
					Record rec = copiedList.get(i);
					long curVal = (Long) rec.get(attrName).getVal();
					if (curVal > ordinalValues.get(valIdx)) {
						valIdx++;
					}

					if ((Long) rec.get(attrName).getVal() <= ordinalValues.get(valIdx)) {
						if (rec.getLabel().equals(Config.LABELS.get(0))) {
							mt++;
						}
					}
					if ((Long) rec.get(attrName).getVal() == ordinalValues.get(valIdx)) {
						mts[valIdx] = mt;
					}
					chkCnt++;
					chkCnts[valIdx] = chkCnt;
				}

				option = computeOrdinalOption(attrName, ordinalValues, mts, chkCnts, copiedList);
				break;
			default:
				throw new IllegalArgumentException("Type should not be null");
			}

			return option;
		}

		private SplitOption computeOrdinalOption(String attrName, List<Long> ordinalValues, long[] mts, long[] chkCnts,
				List<Record> copiedList) {
			SplitOption option = new SplitOption();

			Double[] ginis = new Double[ordinalValues.size()];
			long totalTrue = mts[mts.length - 1];
			long totalFalse = chkCnts[chkCnts.length - 1] - totalTrue;

			for (int i = 0; i < ordinalValues.size(); i++) {
				long mt = mts[i];
				long mf = chkCnts[i] - mt;
				long nt = totalTrue - mt;
				long nf = totalFalse - mf;

				double lGini = (mf + mt) > 0
						? 1 - Math.pow((double) mt / (mf + mt), 2) - Math.pow((double) mf / (mf + mt), 2)
						: 0;
				double rGini = (nf + nt) > 0
						? 1 - Math.pow((double) nt / (nf + nt), 2) - Math.pow((double) nf / (nf + nt), 2)
						: 0;
				ginis[i] = lGini * (double) (mt + mf) / (mt + mf + nt + nf)
						+ rGini * (double) (nt + nf) / (mt + mf + nt + nf);
			}

			List<Double> giniList = Arrays.asList(ginis);
			int smallestGiniIndex = IntStream.range(0, giniList.size())
					.reduce((i, j) -> giniList.get(i) > giniList.get(j) ? j : i).getAsInt();

			double gini = ginis[smallestGiniIndex];
			long splitVal = ordinalValues.get(smallestGiniIndex);
			List<Long> longArr = new ArrayList<>(1);
			longArr.add(splitVal);

			option.setAttrName(attrName);

			option.setGini(gini);
			option.setVals(convert(longArr));

			int splitIndex = -1;
			for (int i = copiedList.size() - 1; i > -1; i--) {
				if ((Long) copiedList.get(i).get(attrName).getVal() == splitVal) {
					splitIndex = i;
					break;
				}
			}

			option.setLeftList(new ArrayList<>(copiedList.subList(0, splitIndex + 1)));
			option.setRightList(new ArrayList<>(copiedList.subList(splitIndex + 1, copiedList.size())));

			return option;
		}

		private SplitOption computeNominalOption(String attrName, List<String> splitSet, List<Record> records) {
			SplitOption option = new SplitOption();
			option.setAttrName(attrName);
			option.setVals(convert(splitSet));

			Map<Boolean, Map<String, List<Record>>> matchingTable = new HashMap<>();
			for (Record rec : records) {
				String label = rec.getLabel();
				String attrVal = (String) rec.get(attrName).getVal();
				boolean match = false;
				if (splitSet.contains(attrVal)) {
					match = true;
				}
				Map<String, List<Record>> map = matchingTable.getOrDefault(match, new HashMap<>());
				List<Record> matchedRecords = map.getOrDefault(label, new ArrayList<>());
				matchedRecords.add(rec);
				map.put(label, matchedRecords);
				matchingTable.put(match, map);
			}

			computeGini(option, matchingTable, records);

			return option;
		}

		private void computeGini(SplitOption option, Map<Boolean, Map<String, List<Record>>> matchingTable,
				List<Record> records) {
			long n = records.size();

			List<Record> leftList = new ArrayList<>();
			List<Record> rightList = new ArrayList<>();

			Map<String, List<Record>> tMap = matchingTable.getOrDefault(true, new HashMap<>());

			List<List<Record>> tLists = new ArrayList<>(tMap.values());
			double tTotCount = tLists.stream().mapToInt(List::size).sum();

			double leftGini = 1;
			if (tTotCount != 0) {
				for (String label : Config.LABELS) {
					List<Record> lRecs = tMap.getOrDefault(label, new ArrayList<>());
					leftList.addAll(lRecs);
					leftGini = leftGini - Math.pow(lRecs.size() / tTotCount, 2);
				}
			}

			Map<String, List<Record>> fMap = matchingTable.getOrDefault(false, new HashMap<>());

			List<List<Record>> fLists = new ArrayList<>(fMap.values());
			double fTotCount = fLists.stream().mapToInt(List::size).sum();

			double rightGini = 1;
			if (fTotCount != 0) {
				for (String label : Config.LABELS) {
					List<Record> rRecs = fMap.getOrDefault(label, new ArrayList<>());
					rightList.addAll(rRecs);
					rightGini = rightGini - Math.pow(rRecs.size() / fTotCount, 2);
				}
			}

			double gini = tTotCount / n * leftGini + fTotCount / n * rightGini;

			option.setLeftList(leftList);
			option.setRightList(rightList);
			option.setGini(gini);
		}

		private <T> Value<T>[] convert(List<T> splitSet) {
			@SuppressWarnings("unchecked")
			Value<T>[] vals = (Value<T>[]) Array.newInstance(Value.class, splitSet.size());
			for (int i = 0; i < splitSet.size(); i++) {
				vals[i] = new Value<T>();
				vals[i].setVal(splitSet.get(i));
			}
			return vals;
		}

		private static List<List<String>> computeCombination(List<String> values) {
			List<List<String>> result = new ArrayList<>();

			for (int i = 0; i < values.size(); i++) {
				String currVal = values.get(i);

				List<String> firstComb = new ArrayList<>();
				firstComb.add(currVal);
				result.add(firstComb);

				List<String> subList = new ArrayList<>(values.subList(i + 1, values.size()));

				List<List<String>> tmpResult = computeCombination(subList);
				for (List<String> tmpComb : tmpResult) {
					tmpComb.add(0, currVal);
					result.add(tmpComb);
				}
			}

			return result;
		}
	}

	private TreeNode getSameLabelNode(Record anyRec) {
		TreeNode root = new TreeNode();
		root.setAttrName(anyRec.getLabel());
		return root;
	}

	private TreeNode getSameAttrRoot(Map<String, Long> labelCountMap) {
		TreeNode root = new TreeNode();
		Set<Entry<String, Long>> entries = labelCountMap.entrySet();
		Entry<String, Long> highest = null;
		for (Entry<String, Long> entry : entries) {
			if (highest == null) {
				highest = entry;
				continue;
			}
			if (entry.getValue() > highest.getValue()) {
				highest = entry;
			}
		}
		root.setAttrName(highest.getKey());
		return root;
	}

	public double evaluate(List<Record> testingData) {
		return evaluate(testingData, false);
	}

	public double evaluate(List<Record> testingData, boolean printPrediction) {
		if (root == null) {
			throw new IllegalStateException("Please build the model first.");
		}

		double tcount = 0;
		double fcount = 0;

		for (Record rec : testingData) {
			String result = predict(rec);
			if (printPrediction) {
				List<String> strVals = new ArrayList<>();
				Config.ATTR_MAP.keySet().forEach(key -> {
					strVals.add(rec.get(key).getVal().toString());
				});
				strVals.add(rec.getLabel());
				strVals.add(result);
				System.out.println(String.join(",", strVals));
			}
			if (rec.getLabel().equals(result)) {
				tcount++;
			} else {
				fcount++;
			}
		}

		return fcount / (tcount + fcount) * 100;
	}

	public String predict(Record rec) {
		return root.predict(rec);
	}

	public void print() {
		if (root == null) {
			throw new IllegalStateException("Please build the model first.");
		}

		System.out.println(root);

		try (FileOutputStream fileOut = new FileOutputStream(Config.getTreeBinFileName());
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeObject(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}
}
