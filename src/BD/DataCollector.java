package BD;

public class DataCollector {

	int categoryNum;
	int M;
	private int[] permutatedValues;
	private double[] frequency;
	int userNum;

	public DataCollector(int categoryNum, int M, int userNum) {
		this.categoryNum = categoryNum;
		this.M = M;
		this.frequency = new double[categoryNum];
		this.userNum = userNum;
	}

	// Histogram ver.
	public void setHistogram(double histogram[]) {
		frequency = histogram;
		adjustFreqDist();
	}

	public void receives(int[] permutatedValues) {
		this.permutatedValues = permutatedValues;
		calcFreqDist();
		adjustFreqDist();
	}

	private void calcFreqDist() {
		for (int i = 0; i < permutatedValues.length; i++) {
			frequency[permutatedValues[i]]++;
		}
	}

	private void adjustFreqDist() {
		for (int i = 0; i < categoryNum; i++) {
			frequency[i] = (frequency[i] - M / 2.0) / userNum;
		}
	}

	public double[] getFrequency() {
		return frequency;
	}

}
