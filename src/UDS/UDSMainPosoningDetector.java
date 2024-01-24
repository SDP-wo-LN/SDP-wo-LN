package UDS;

import java.util.HashSet;

import util.Util;

public class UDSMainPosoningDetector {

	static int simNum = 100;

	public static void main(String args[]) {

		double attacking_ratios[] = { 0.1 };
		double epsilon_servers[] = { 10 };
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

							int poisoningUserNum = (int) (orgUserNum * attacking_ratio / (1 - attacking_ratio));

							double samplingRate = getSamplingRate(target_epsilon);

							int fakeNum = getOptimalFakeNum2(target_epsilon, target_delta);

							// For simulation
							double sumError = 0.0;
							double sumMga = 0.0;
							double sumMga2 = 0.0;
							double sumExpectedMga = 0.0;

							for (int sim = 0; sim < simNum; sim++) {
								// Initialize users
								User users[] = new User[orgUserNum + poisoningUserNum];

								// Set user values
								for (int i = 0; i < orgUserNum; i++) {
									users[i] = new User(orgData[i]);
								}

								HashSet<Integer> poisoningTargetAttributes = Util.getRandomElements(categoryNum,
										poisonigTargetNum);

								// fake users
								for (int i = 0; i < poisoningUserNum; i++) {
									users[orgUserNum + i] = new User();
									users[orgUserNum + i].setFakeValue(poisoningTargetAttributes);
								}

								// Initialize the shuffler
								Shuffler shuffler1 = new Shuffler(orgUserNum, categoryNum, fakeNum, samplingRate);
								Shuffler shuffler2 = new Shuffler(orgUserNum + poisoningUserNum, categoryNum, fakeNum,
										samplingRate);
								// Initialize the data collector
								DataCollector dataCollector1 = new DataCollector(categoryNum, fakeNum, samplingRate,
										orgUserNum);
								DataCollector dataCollector2 = new DataCollector(categoryNum, fakeNum, samplingRate,
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
								shuffler1.sampleAndPermutation();
								shuffler2.addFakeValues();
								shuffler2.sampleAndPermutation();

								// Data collector receives the sampled values
								dataCollector1.receives(shuffler1.getSampledValues());
								double frequency1[] = dataCollector1.getFrequency();
								dataCollector2.receives(shuffler2.getSampledValues());
								double frequency2[] = dataCollector2.getFrequency();
								double frequency_counter[] = Util.normalization(frequency2);

								if (isThresholding) {
									frequency1 = util.Util.significance_threshold(frequency1, orgUserNum,
											calcExpectedError(orgUserNum, categoryNum, fakeNum, samplingRate));
									frequency2 = util.Util.significance_threshold(frequency2,
											orgUserNum + poisoningUserNum, calcExpectedError(
													orgUserNum + poisoningUserNum, categoryNum, fakeNum, samplingRate));
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

							System.out.println(dataName + "\t" + "UDS-Shuffle\t" + target_epsilon + "\t" + target_delta
									+ "\t\t" + isThresholding + "\t" + poisoningUserNum + "\t" + attacking_ratio + "\t"
									+ "\t" + (sumError / simNum) + "\t" + (sumMga / simNum) + "\t" + (sumMga2 / simNum)
									+ "\t" + (sumExpectedMga / simNum));
						}
					}
				}
			}
		}
	}

	public static double getSamplingRate(double epsilon_server) {
		double samplingRate = 1 - Math.exp(-epsilon_server / 2);
		return samplingRate;
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

	public static double calcExpectedError2(double epsilon, int userNum, int categoryNum, int fakeNum) {
		double error = 2 * (userNum + (double) fakeNum * categoryNum) / ((double) userNum * userNum * epsilon);
		return error;
	}

	public static int getOptimalFakeNum2(double epsilon, double delta) {
		double opt = Math.exp(epsilon) * Math.log(2 / delta)
				/ (Math.log(Math.exp(-epsilon / 2)) + (-1 + Math.exp(epsilon)) * Math.log(1 + Math.exp(-epsilon / 2)));
		int fakeNum = (int) Math.ceil(opt);
		return fakeNum;
	}

}
