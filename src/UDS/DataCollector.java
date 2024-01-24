package UDS;

public class DataCollector {

	int categoryNum;
	int fakeNum;
	double samplingRate;
	private int[] shuffledValues;
	private double[] frequency;
	int userNum;

	public DataCollector(int categoryNum, int fakeNum, double samplingRate, int userNum) {
		this.categoryNum = categoryNum;
		this.fakeNum = fakeNum;
		this.samplingRate = samplingRate;
		this.frequency = new double[categoryNum];
		this.userNum = userNum;
	}

	public void receives(int[] shuffledValues) {
		this.shuffledValues = shuffledValues;
		calcFreqDist();
		adjustFreqDist();
	}

	private void calcFreqDist() {
		for (int i = 0; i < shuffledValues.length; i++) {
			frequency[shuffledValues[i]]++;
		}
	}

	private void adjustFreqDist() {
		for (int i = 0; i < categoryNum; i++) {
			frequency[i] = (frequency[i] / samplingRate - fakeNum) / userNum;
		}
	}

	public double[] getFrequency() {
		return frequency;
	}

}
