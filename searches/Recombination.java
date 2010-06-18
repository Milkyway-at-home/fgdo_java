package fgdo_java.searches;


public class Recombination {

	public static void boundParameters(SearchParameters parameters, double[] minBound, double[] maxBound) {
		for (int i = 0; i < minBound.length; i++) {
			if (parameters.parameters[i] > maxBound[i]) parameters.parameters[i] = maxBound[i];
			if (parameters.parameters[i] < minBound[i]) parameters.parameters[i] = minBound[i];
		}
	}

	public static SearchParameters getRandomIndividual(String searchName, double[] minBound, double[] maxBound, String metadata) {
		SearchParameters result = new SearchParameters(searchName, minBound.length);

		for (int i = 0; i < minBound.length; i++) {
			result.parameters[i] = minBound[i] + (Math.random() * (maxBound[i] - minBound[i]));
		}
		result.setMetadata(metadata);

		return result;
	}
}
