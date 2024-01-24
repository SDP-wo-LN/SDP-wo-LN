package BD;

import UDS.User;
import util.Util;

public class BDMainData {

	static int simNum = 1;

	public static void main(String args[]) {

		double epsilon_servers[] = { 5 };
		double targetDeltas[] = { Math.pow(0.1, 8) };
		String dataNames[] = { "rfid", "localization", "man", "census" };

		for (String dataName : dataNames) {

			int orgData[] = Util.getOrgVals(dataName);
			int userNum = orgData.length;
			int categoryNum = Util.getCategoryNum(orgData);
			for (double target_epsilon : epsilon_servers) {
				for (double target_delta : targetDeltas) {

					int M = getBdM(target_epsilon, target_delta);

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
						Shuffler shuffler = new Shuffler(userNum, categoryNum, M);
						// Initialize the data collector
						DataCollector dataCollector = new DataCollector(categoryNum, M, userNum);

						// Each user sends each value
						for (int i = 0; i < userNum; i++) {
							shuffler.receiveValue(i, users[i].getOriginalValue());
						}
						shuffler.addFakeValues();
						shuffler.permutation();

						// shuffler.makeHistogram();
						// dataCollector.setHistogram(shuffler.getHistogram());

						// Data collector receives the sampled values
						dataCollector.receives(shuffler.getPermutatedValues());
						double frequency[] = dataCollector.getFrequency();

						double frequency_thresholding[] = util.Util.significance_threshold(frequency, userNum,
								calcExpectedError(userNum, categoryNum, M));

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

					double expectedError = calcExpectedError(userNum, categoryNum, M);
					double expectedError2 = calcExpectedError2(userNum, categoryNum, target_epsilon, target_delta);

					System.out.println(dataName + "\t" + target_epsilon + "\t" + target_delta + "\t" + "\t" + "\t"
							+ (sumError / simNum) + "\t" + (sumError_thresholding / simNum) + "\t"
							+ (sumErrorMAE / simNum) + "\t" + (sumErrorMAE_thresholding / simNum) + "\t" + expectedError
							+ "\t" + expectedError2);
				}
			}
		}
	}

	public static double getEpsilon(int M) {
		double epsilon = 2 * Math.log(2.0 / M + 1);
		return epsilon;
	}

	public static double getDelta(int M, double epsilon) {
		double eta = (Math.exp(epsilon / 2) - 1) / (Math.exp(epsilon / 2) + 1)
				- 2.0 / (M * (Math.exp(epsilon / 2) + 1));
		double delta = 4 * Math.exp(-eta * eta * M / 2.0);
		return delta;
	}

	public static double calcError(double originalFrequency[], double expectedFrequency[]) {
		int categoryNum = originalFrequency.length;
		double error = 0.0;
		for (int i = 0; i < categoryNum; i++) {
			error += Math.pow(originalFrequency[i] - expectedFrequency[i], 2);
		}
		return error;
	}

	public static double calcErrorMAE(double originalFrequency[], double expectedFrequency[]) {
		int categoryNum = originalFrequency.length;
		double error = 0.0;
		for (int i = 0; i < categoryNum; i++) {
			error += Math.abs(originalFrequency[i] - expectedFrequency[i]);
		}
		return error;
	}

	public static double calcExpectedError(int userNum, int categoryNum, int M) {
		double error = (double) M * categoryNum / (4.0 * userNum * userNum);
		return error;
	}

	public static double calcExpectedError2(int userNum, int categoryNum, double epsilon, double delta) {
		double error = 8 * categoryNum * Math.log(4 / delta) / ((double) userNum * userNum * epsilon * epsilon);
		return error;
	}

	public static int getBdM(double epsilon, double delta) {
		int M1 = (int) Math.ceil(2 / (-1 + Math.exp(epsilon / 2)));

		double expEpsilonOver2 = Math.exp(epsilon / 2.0);
		double log4OverDelta = Math.log(4.0 / delta);

		double numerator = 2.0 * (-1.0 + expEpsilonOver2) + Math.pow(1.0 + expEpsilonOver2, 2.0) * log4OverDelta
				+ (1.0 + expEpsilonOver2) * Math.sqrt(log4OverDelta
						* (4.0 * (-1.0 + expEpsilonOver2) + Math.pow(1.0 + expEpsilonOver2, 2.0) * log4OverDelta));

		double denominator = Math.pow(-1.0 + expEpsilonOver2, 2.0);

		int M2 = (int) (numerator / denominator);

		int M = Math.max(M1, M2);

		return M;
	}

}
