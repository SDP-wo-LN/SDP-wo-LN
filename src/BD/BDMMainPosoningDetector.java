package BD;

import java.util.HashSet;

import UDS.User;
import util.Util;

public class BDMMainPosoningDetector {

	static int simNum = 100;

	public static void main(String args[]) {

		double attacking_ratios[] = { 0.1 };
		double epsilon_servers[] = { 5 };
		double targetDeltas[] = { Math.pow(0.1, 8) };
		String dataNames[] = { "rfid", "localization", "man", "census" };
		int poisonigTargetNum = 1;
		boolean isThresholdings[] = { false };

		for (String dataName : dataNames) {

			int orgData[] = Util.getOrgVals(dataName);
			int orgUserNum = orgData.length;
			int categoryNum = Util.getCategoryNum(orgData);

			for (double attacking_ratio : attacking_ratios) {
				for (double target_epsilon : epsilon_servers) {
					for (double target_delta : targetDeltas) {
						for (boolean isThresholding : isThresholdings) {

							int M = BDMainData.getBdM(target_epsilon, target_delta);
							int poisoningUserNum = (int) (orgUserNum * attacking_ratio / (1 - attacking_ratio));

							// Initialize users
							User users[] = new User[orgUserNum + poisoningUserNum];

							// Set user values
							for (int i = 0; i < orgUserNum; i++) {
								users[i] = new User(orgData[i]);
							}

							// For simulation
							double sumError = 0.0;
							double sumMga = 0.0;
							double sumMga2 = 0.0;
							double sumExpectedMga = 0.0;

							for (int sim = 0; sim < simNum; sim++) {

								HashSet<Integer> poisoningTargetAttributes = Util.getRandomElements(categoryNum,
										poisonigTargetNum);

								// fake users
								for (int i = 0; i < poisoningUserNum; i++) {
									users[orgUserNum + i] = new User();
									users[orgUserNum + i].setFakeValue(poisoningTargetAttributes);
								}

								// Initialize the shuffler
								Shuffler shuffler1 = new Shuffler(orgUserNum, categoryNum, M);
								Shuffler shuffler2 = new Shuffler(orgUserNum + poisoningUserNum, categoryNum, M);
								// Initialize the data collector
								DataCollector dataCollector1 = new DataCollector(categoryNum, M, orgUserNum);
								DataCollector dataCollector2 = new DataCollector(categoryNum, M,
										orgUserNum + poisoningUserNum);

								// Each user sends each value
								for (int i = 0; i < orgUserNum; i++) {
									shuffler1.receiveValue(i, users[i].getOriginalValue());
									shuffler2.receiveValue(i, users[i].getOriginalValue());
								}

								for (int i = 0; i < poisoningUserNum; i++) {
									shuffler2.receiveValue(orgUserNum + i, users[orgUserNum + i].getOriginalValue());
								}

								shuffler1.addFakeValues();
								shuffler1.permutation();
								shuffler2.addFakeValues();
								shuffler2.permutation();

								// shuffler1.makeHistogram();
								// shuffler2.makeHistogram();
								// dataCollector1.setHistogram(shuffler1.getHistogram());
								// dataCollector2.setHistogram(shuffler2.getHistogram());

								// // Data collector receives the sampled values
								dataCollector1.receives(shuffler1.getPermutatedValues());
								double frequency1[] = dataCollector1.getFrequency();
								dataCollector2.receives(shuffler2.getPermutatedValues());
								double frequency2[] = dataCollector2.getFrequency();

								double frequency_counter[] = Util.normalization(frequency2);

								if (isThresholding) {
									frequency1 = util.Util.significance_threshold(frequency1,
											orgUserNum + poisoningUserNum,
											calcExpectedError(orgUserNum, categoryNum, M));
									frequency2 = util.Util.significance_threshold(frequency2,
											orgUserNum + poisoningUserNum,
											calcExpectedError(orgUserNum + poisoningUserNum, categoryNum, M));
									frequency_counter = Util.normalization(frequency2);
								}

								// For evaluation
								double[] originalFrequency = new double[categoryNum];
								for (int i = 0; i < orgUserNum; i++) {
									originalFrequency[users[i].getOriginalValue()]++;
								}
								for (int i = 0; i < categoryNum; i++) {
									originalFrequency[i] /= orgUserNum;
								}

								double acctualError = calcError(originalFrequency, frequency2);
								sumError += acctualError;

								double mga = Util.getMga(frequency1, frequency2, poisoningTargetAttributes);
								sumMga += mga;
								double mga2 = Util.getMga(frequency1, frequency_counter, poisoningTargetAttributes);
								sumMga2 += mga2;

								double lambda = (double) poisoningUserNum / (orgUserNum + poisoningUserNum);
								double ft = 0;
								for (int targetAtt : poisoningTargetAttributes) {
									ft += originalFrequency[targetAtt];
								}

								double estimatedMga = lambda * (1 - ft);
								sumExpectedMga += estimatedMga;

							}

							System.out.println(dataName + "\t" + "BD-Shuffle\t" + target_epsilon + "\t" + target_delta
									+ "\t\t" + isThresholding + "\t" + poisoningUserNum + "\t" + attacking_ratio + "\t"
									+ "\t" + (sumError / simNum) + "\t" + (sumMga / simNum) + "\t" + (sumMga2 / simNum)
									+ "\t" + (sumExpectedMga / simNum));
						}
					}
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
