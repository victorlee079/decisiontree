package com.cmsc.ml.dt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import com.cmsc.ml.dt.build.DataProcessor;
import com.cmsc.ml.dt.build.DecisionTree;
import com.cmsc.ml.dt.domain.InputData;
import com.cmsc.ml.dt.domain.TreeNode;

public class Main {
	public static void main(String[] args)
			throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		boolean isBuild = Config.IS_BUILD;
		String trainingFilePath = Config.TRAIN_DATA_FILE;
		String testingFilePath = Config.TEST_DATA_FILE;
		String treeBinFileName = Config.getTreeBinFileName();
		int threshold = Config.THRESHOLD;

		if (args.length > 1) {
			int i = 0;
			while (i < args.length) {
				switch (args[i].trim()) {
				case Constants.ARGUMENTS.EVAL:
					i++;
					testingFilePath = args[i].trim();
					i++;
					break;
				case Constants.ARGUMENTS.THRESHOLD:
					i++;
					threshold = Integer.valueOf(args[i]);
					i++;
					break;
				case Constants.ARGUMENTS.TRAIN:
					i++;
					trainingFilePath = args[i].trim();
					i++;
					break;
				case Constants.ARGUMENTS.USE_MODEL:
					i++;
					isBuild = false;
					break;
				default:
					System.out.println("Invalid usage.");
					return;
				}
			}
		}

		try (PrintStream o = new PrintStream(new File(Config.getLogFileName()))) {
			System.setOut(o);

			DataProcessor dp = new DataProcessor();
			InputData trainData = dp.readData(trainingFilePath);
			InputData testData = dp.readData(testingFilePath);

			System.out.println("Start Building...");
			DecisionTree tree = new DecisionTree();

			File file = new File(treeBinFileName);
			if (!isBuild && file.exists()) {
				try (FileInputStream fileIn = new FileInputStream(treeBinFileName);
						ObjectInputStream in = new ObjectInputStream(fileIn)) {
					TreeNode root = (TreeNode) in.readObject();
					tree.setRoot(root);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				tree.build(trainData.getRecords(), trainData.getAttrDesc(), threshold);
			}
			tree.print();

			double errorRate = tree.evaluate(trainData.getRecords());
			System.out.println("Error Rate for Training Set: " + errorRate);
			errorRate = tree.evaluate(testData.getRecords(), true);
			System.out.println("Error Rate for Testing Set: " + errorRate);
		}
	}
}
