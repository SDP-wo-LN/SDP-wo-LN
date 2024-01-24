package UDS;

import java.math.BigDecimal;

import util.Util;

public class UDSMainData {

	static int simNum = 100;

	public static void main(String args[]) {
		double epsilon_servers[] = { 0.1, 0.2, 0.5, 1, 2, 3, 4, 5 };
		double targetDeltas[] = { Math.pow(0.1, 6), Math.pow(0.1, 10), Math.pow(0.1, 8) };
		String dataNames[] = { "rfid" };

		for (String dataName : dataNames) {

			int orgData[] = Util.getOrgVals(dataName);
			int userNum = orgData.length;
			int categoryNum = Util.getCategoryNum(orgData);

			for (double target_epsilon : epsilon_servers) {
				for (double target_delta : targetDeltas) {

					double samplingRate = getSamplingRate(target_epsilon);
					int fakeNum = getOptimalFakeNum2(target_epsilon, target_delta);

					// Initialize users
					User users[] = new User[userNum];
					for (int i = 0; i < userNum; i++) {
						users[i] = new User(orgData[i]);
					}

					// For simulation
					double sumError = 0.0;
					double sumError_thresholding = 0.0;
					double sumErrorMAE = 0.0;
					double sumErrorMAE_thresholding = 0.0;

					for (int sim = 0; sim < simNum; sim++) {
						// Initialize the shuffler
						Shuffler shuffler = new Shuffler(userNum, categoryNum, fakeNum, samplingRate);
						// Initialize the data collector
						DataCollector dataCollector = new DataCollector(categoryNum, fakeNum, samplingRate, userNum);

						// Each user sends each value
						for (int i = 0; i < userNum; i++) {
							shuffler.receiveValue(i, users[i].getOriginalValue());
						}
						shuffler.addFakeValues();
						shuffler.sampleAndPermutation();

						// Data collector receives the sampled values
						dataCollector.receives(shuffler.getSampledValues());
						double frequency[] = dataCollector.getFrequency();
						double frequency_thresholding[] = util.Util.significance_threshold(frequency, userNum,
								calcExpectedError(userNum, categoryNum, fakeNum, samplingRate));

						// For evaluation
						double[] originalFrequency = new double[categoryNum];
						for (int i = 0; i < userNum; i++) {
							originalFrequency[users[i].getOriginalValue()]++;
						}
						for (int i = 0; i < categoryNum; i++) {
							originalFrequency[i] /= userNum;
						}
						double acctualError = calcError(originalFrequency, frequency);
						sumError += acctualError;
						double acctualError_thresholding = calcError(originalFrequency, frequency_thresholding);
						sumError_thresholding += acctualError_thresholding;
						double acctualErrorMAE = calcErrorMAE(originalFrequency, frequency);
						sumErrorMAE += acctualErrorMAE;
						double acctualErrorMAE_thresholding = calcErrorMAE(originalFrequency, frequency_thresholding);
						sumErrorMAE_thresholding += acctualErrorMAE_thresholding;
					}

					double expectedError = calcExpectedError(userNum, categoryNum, fakeNum, samplingRate);
					double expectedError2 = calcExpectedError2(target_epsilon, target_delta, userNum, categoryNum);
					System.out.println(dataName + "\t" + target_epsilon + "\t" + target_delta + "\t" + "\t" + fakeNum
							+ "\t" + (sumError / simNum) + "\t" + (sumError_thresholding / simNum) + "\t"
							+ (sumErrorMAE / simNum) + "\t" + (sumErrorMAE_thresholding / simNum) + "\t" + expectedError
							+ "\t" + expectedError2);
				}
			}
		}
	}

	public static double getSamplingRate(double epsilon_server) {
		double samplingRate = 1 - Math.exp(-epsilon_server / 2);
		return samplingRate;
	}

	public static int getOptimalFakeNum2(double epsilon, double delta) {
		double opt = Math.exp(epsilon) * Math.log(2 / delta)
				/ (Math.log(Math.exp(-epsilon / 2)) + (-1 + Math.exp(epsilon)) * Math.log(1 + Math.exp(-epsilon / 2)));
		int fakeNum = (int) Math.ceil(opt);
		return fakeNum;
	}

	public static double binomialProbability(int t, int x, double b) {
		double logBinomialCoeff = logBinomialCoefficient(t, x);
		double logProbability = logBinomialCoeff + x * Math.log(b) + (t - x) * Math.log(1 - b);

		return Math.exp(logProbability);
	}

	public static double logBinomialCoefficient(int n, int k) {
		double result = 0.0;
		for (int i = 1; i <= k; i++) {
			result += Math.log(n - i + 1) - Math.log(i);
		}
		return result;
	}

	public static double calcErrorMAE(double originalFrequency[], double expectedFrequency[]) {
		int categoryNum = originalFrequency.length;
		double error = 0.0;
		for (int i = 0; i < categoryNum; i++) {
			error += Math.abs(originalFrequency[i] - expectedFrequency[i]);
		}
		return error;
	}

	public static double calcError(double originalFrequency[], double expectedFrequency[]) {
		int categoryNum = originalFrequency.length;
		double error = 0.0;
		for (int i = 0; i < categoryNum; i++) {
			error += Math.pow(originalFrequency[i] - expectedFrequency[i], 2);
		}
		return error;
	}

	public static double calcExpectedError(int userNum, int categoryNum, int fakeNum, double samplingRate) {
		double error = (1.0 - samplingRate) * (userNum + fakeNum * categoryNum) / samplingRate / userNum / userNum;
		return error;
	}

	public static double calcExpectedError2(double epsilon, double delta, int userNum, int categoryNum) {
		double error = (4 * Math.log(2) - 2) * userNum * epsilon + 4 * categoryNum * Math.log(2 / delta);
		error /= (2 * Math.log(2) - 1) * userNum * userNum * epsilon * epsilon;
		return error;
	}

	public static String double2string(double d) {
		String s = BigDecimal.valueOf(d).toPlainString();
		return s;
	}

}
