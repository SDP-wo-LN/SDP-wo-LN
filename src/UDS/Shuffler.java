package UDS;

import java.util.ArrayList;
import java.util.Collections;

public class Shuffler {

	int userNum;// n
	int categoryNum;// d
	int fakeNum;// m
	double samplingRate;// beta
	private int values[];
	private int sampledValues[];

	public Shuffler(int userNum, int categoryNum, int fakeNum, double samplingRate) {
		this.userNum = userNum;
		this.categoryNum = categoryNum;
		this.fakeNum = fakeNum;
		this.samplingRate = samplingRate;
		values = new int[userNum + categoryNum * fakeNum];
	}

	public void receiveValue(int userId, int value) {
		values[userId] = value;
	}

	public void addFakeValues() {
		for (int i = 0; i < categoryNum; i++) {
			for (int j = 0; j < fakeNum; j++) {
				values[userNum + i * fakeNum + j] = i;
			}
		}
	}

	public void sampleAndPermutation() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < userNum + categoryNum * fakeNum; i++) {
			double rand = Math.random();
			if (rand < samplingRate) {
				list.add(values[i]);
			}
		}

		Collections.shuffle(list);

		sampledValues = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			sampledValues[i] = list.get(i);
		}
	}

	public int[] getSampledValues() {
		return sampledValues;
	}

}
