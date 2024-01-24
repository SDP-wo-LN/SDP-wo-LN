package BD;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class Shuffler {

	int userNum;// n
	int categoryNum;// d
	double samplingRate;// beta
	private ArrayList<Integer> values;
	private int M;
	private double p = 0.5;
	int permutatedValues[];

	private double histogram[];

	BinomialDistribution distribution = null;

	public Shuffler(int userNum, int categoryNum, int M) {
		this.userNum = userNum;
		this.categoryNum = categoryNum;
		this.M = M;
		values = new ArrayList<Integer>();
		distribution = new BinomialDistribution(M, p);
	}

	public void receiveValue(int userId, int value) {
		values.add(value);
	}

	// Histogram ver.
	public void makeHistogram() {
		histogram = new double[categoryNum];
		for (int value : values) {
			histogram[value]++;
		}

		for (int i = 0; i < categoryNum; i++) {
			int zi = distribution.sample();
			for (int j = 0; j < zi; j++) {
				histogram[i]++;
			}
		}
	}

	// Histogram ver.
	public double[] getHistogram() {
		return histogram;
	}

	public void addFakeValues() {
		for (int i = 0; i < categoryNum; i++) {
			int zi = distribution.sample();
			for (int j = 0; j < zi; j++) {
				values.add(i);
			}
		}
	}

	public void permutation() {
		Collections.shuffle(values);
		permutatedValues = new int[values.size()];
		int count = 0;
		for (int value : values) {
			permutatedValues[count] = value;
			count++;
		}
	}

	public int[] getPermutatedValues() {
		return permutatedValues;
	}

}
