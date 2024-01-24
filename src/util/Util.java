package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.NormalDistribution;

public class Util {

	public static void main(String args[]) {
		double est[] = new double[] { 0.1, 0.9, -0.1 };
		double est2[] = significance_threshold(est, 10, 1);
		System.out.println(Arrays.toString(est2));
	}

	public static String double2string(double d) {
		String s = BigDecimal.valueOf(d).toPlainString();
		return s;
	}

	public static int getCategoryNum(int data[]) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int d : data) {
			set.add(d);
		}
		int categoryNum = set.size();
		return categoryNum;
	}

	public static double[] significance_threshold(double est_org[], int n, double estimated_l2_loss) {

		NormalDistribution nd = new NormalDistribution();
		double alpha = 0.05;
		int domain_size = est_org.length;

		double estn[] = new double[domain_size];
		for (int i = 0; i < domain_size; i++) {
			estn[i] = est_org[i] * n;
		}

		double variance_i = estimated_l2_loss * n * n / domain_size;

		int zeroCount = 0;
		double std = Math.sqrt(variance_i);
		double threshold = nd.inverseCumulativeProbability(1 - alpha / domain_size) * std;
		for (int k = 0; k < domain_size; k++) {
			if (estn[k] < threshold) {
				zeroCount++;
				estn[k] = 0;
			}
		}

		double sum = 0;
		for (int k = 0; k < domain_size; k++) {
			sum += estn[k];
		}

		if (sum < n) {
			for (int k = 0; k < domain_size; k++) {
				if (estn[k] == 0) {
					estn[k] = (n - sum) / zeroCount;
				}
			}
		}

		sum = 0;
		for (double es : estn) {
			sum += es;
		}

		if (sum != 0) {
			for (int k = 0; k < domain_size; k++) {
				estn[k] = (estn[k] / sum) * n;
			}
		} else {
			for (int k = 0; k < estn.length; k++) {
				estn[k] = n / domain_size;
			}
		}

		double result[] = new double[domain_size];
		for (int i = 0; i < domain_size; i++) {
			result[i] = estn[i] / n;
		}

		return result;

	}

	public static double getMga(double estimatedDistributionWithoutFakes[], double estimatedDistributionWithFakes[],
			Set<Integer> poisoningTargetAttributes) {
		double mga = 0.0;
		for (int targetAtt : poisoningTargetAttributes) {
			mga += (estimatedDistributionWithFakes[targetAtt] - estimatedDistributionWithoutFakes[targetAtt]);

		}
		return mga;
	}

	public static double[] normalization(double histogram[]) {
		int categoryNum = histogram.length;
		double min = Double.MAX_VALUE;
		for (double v : histogram) {
			if (v < min) {
				min = v;
			}
		}

		double sum = 0.0;
		for (int i = 0; i < categoryNum; i++) {
			sum += histogram[i] - min;
		}

		double histogram2[] = new double[categoryNum];
		for (int i = 0; i < categoryNum; i++) {
			if (sum != 0) {
				histogram2[i] = (histogram[i] - min) / sum;
			} else {
				histogram2[i] = histogram[i];
			}
		}
		return histogram2;
	}

	public static int[] getOrgVals(String dataName) {
		String fileName = "dataset/" + dataName + ".txt";
		int vals[] = null;
		try {
			TreeSet<String> set = new TreeSet<String>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			int count = 0;
			String line = "";
			while ((line = br.readLine()) != null) {
				count++;
				set.add(line);
			}
			br.close();

			int newId = 0;
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (String val : set) {
				map.put(val, newId++);
			}

			vals = new int[count];
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			count = 0;
			while ((line = br.readLine()) != null) {
				vals[count++] = map.get(line);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vals;
	}

	public static HashSet<Integer> getRandomElements(int size, int targetNum) {
		List<Integer> numbers = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			numbers.add(i);
		}

		Collections.shuffle(numbers);
		HashSet<Integer> set = new HashSet<>(numbers.subList(0, targetNum));

		return set;
	}
}
