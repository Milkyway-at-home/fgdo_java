package fgdo_java.searches;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

import fgdo_java.util.DirectoryTree;
import fgdo_java.util.XMLTemplate;

public class DifferentialEvolution extends Search {

	private int bestValidatedIndividual;
	private int bestUnvalidatedIndividual;
	private int currentIndividual;

	private int populationSize;
	private int numberParameters;
	private int individualsAnalyzed;

	private double[] minBound;
	private double[] maxBound;

	private double pairWeight = 0.5;
	private double crossoverRate = 0.5;
	private int recombinationPairs = 1;

	public static final int BINOMIAL_RECOMBINATION = 0;
	public static final int EXPONENTIAL_RECOMBINATION = 0;
	public static final int NO_RECOMBINATION = 0;
	private int recombinationType = DifferentialEvolution.BINOMIAL_RECOMBINATION;

	public static final int BEST_PARENT = 0;
	public static final int RANDOM_PARENT = 0;
	public static final int CURRENT_PARENT = 0;
	private int parentType = DifferentialEvolution.RANDOM_PARENT;

	private boolean pessimistic = false;

	private HashMap<Integer,SearchResult> validatedPopulation = new HashMap<Integer,SearchResult>();
	private HashMap<Integer,SearchResult> unvalidatedPopulation = new HashMap<Integer,SearchResult>();

	public DifferentialEvolution(String searchName, int populationSize, int numberParameters, double[] minBound, double[] maxBound, double pairWeight, double crossoverRate, int recombinationPairs, int recombinationType, int parentType, boolean pessimistic) {
		this(searchName, populationSize, numberParameters, minBound, maxBound);

		this.pairWeight = pairWeight;
		this.crossoverRate = crossoverRate;
		this.recombinationPairs = recombinationPairs;
		this.recombinationType = recombinationType;
		this.parentType = parentType;
		this.pessimistic = pessimistic;
	}
	
	public DifferentialEvolution(String searchName, int populationSize, int numberParameters, double[] minBound, double[] maxBound) {
		this.searchName = searchName;
		this.populationSize = populationSize;
		this.numberParameters = numberParameters;
		this.individualsAnalyzed = 0;

		this.minBound = new double[minBound.length];
		this.maxBound = new double[maxBound.length];

		for (int i = 0; i < numberParameters; i++) {
			this.minBound[i] = minBound[i];
			this.maxBound[i] = maxBound[i];
		}
	}

	public void writeToFile() throws IOException {
		BufferedWriter out;
			
		out = new BufferedWriter( new FileWriter( DirectoryTree.getResultsDirectory() + searchName + "/search" ) );

		out.write("<search_name>" + searchName + "</search_name>\n");
		out.write("<population_size>" + populationSize + "</population_size>\n");
		out.write("<number_parameters>" + numberParameters + "</number_parameters>\n");
		out.write("<individuals_analyzed>" + individualsAnalyzed + "</individuals_analyzed>\n");

		out.write("<pair_weight>" + pairWeight + "</pair_weight>\n");
		out.write("<crossover_rate>" + crossoverRate + "</crossover_rate>\n");
		out.write("<recombination_pairs>" + recombinationPairs + "</recombination_pairs>\n");
		out.write("<recombination_type>" + recombinationType + "</recombination_type>\n");
		out.write("<parent_type>" + parentType + "</parent_type>\n");
		out.write("<pessimistic>" + pessimistic + "</pessimistic>\n");

		out.write("<min_bound>" + Arrays.toString(minBound) + "</min_bound>\n");
		out.write("<max_bound>" + Arrays.toString(maxBound) + "</max_bound>\n");

		out.close();

		out = new BufferedWriter( new FileWriter( DirectoryTree.getResultsDirectory() + searchName + "/unvalidated_population" ) );
		for (Map.Entry<Integer,SearchResult> entry : unvalidatedPopulation.entrySet()) {
			out.write( "<id>" + entry.getKey() + "</id> <result>" + entry.getValue().toFileString() + "</result>\n" );

		}
		out.close();

		out = new BufferedWriter( new FileWriter( DirectoryTree.getResultsDirectory() + searchName + "/validated_population" ) );
		for (Map.Entry<Integer,SearchResult> entry : validatedPopulation.entrySet()) {
			out.write( "<id>" + entry.getKey() + "</id> <result>" + entry.getValue().toFileString() + "</result>\n" );
		}
		out.close();
	}


	public DifferentialEvolution(String searchName) {
		this.searchName = searchName;

		if (completed) return;

		File searchFile = new File( DirectoryTree.getResultsDirectory() + searchName + "/search");
		if (!searchFile.exists()) {
			System.err.println("SEARCH DOES NOT EXIST: " + searchFile);
			completed = true;
			return;
		}
		completed = false;

		try {
			String searchFileString = DirectoryTree.fileToString(searchFile.toString());

			populationSize = XMLTemplate.processInteger(searchFileString, "population_size");
			numberParameters = XMLTemplate.processInteger(searchFileString, "number_parameters");
			individualsAnalyzed = XMLTemplate.processInteger(searchFileString, "individuals_analyzed");

			pairWeight = XMLTemplate.processDouble(searchFileString, "pair_weight");
			crossoverRate = XMLTemplate.processDouble(searchFileString, "crossover_rate");
			recombinationPairs = XMLTemplate.processInteger(searchFileString, "recombination_pairs");
			recombinationType = XMLTemplate.processInteger(searchFileString, "recombination_type");
			parentType = XMLTemplate.processInteger(searchFileString, "parent_type");
			pessimistic = XMLTemplate.processBoolean(searchFileString, "pessimistic");

			minBound = XMLTemplate.processDoubleArray(searchFileString, "min_bound");
			maxBound = XMLTemplate.processDoubleArray(searchFileString, "max_bound");

		} catch (IOException e) {
			System.err.println("IOException while reading search file: " + searchFile);
			System.err.println(e);
			e.printStackTrace();
			completed = true;
			return;
		} catch (Exception e) {
			System.err.println("Exception while reading search file: " + searchFile);
			System.err.println(e);
			e.printStackTrace();
			completed = true;
			return;
		}

		StringTokenizer st;
		String line;
		try {
			String unvalidatedPopulationString = DirectoryTree.fileToString( DirectoryTree.getResultsDirectory() + searchName + "/unvalidated_population" );
			st = new StringTokenizer(unvalidatedPopulationString, "\n");
			while (st.hasMoreTokens()) {
				line = st.nextToken();
				int position = XMLTemplate.processInteger(line, "id");
				String resultString = XMLTemplate.processString(line, "result");

				SearchResult result = SearchResult.parseFromString(resultString, line);
				result.setSearchName(searchName);
				unvalidatedPopulation.put( new Integer(position), result );
			}
		} catch (IOException e) {
			System.err.println("IOException while reading unvalidated population file: ");
			System.err.println(e);
			e.printStackTrace();
			completed = true;
			return;
		} catch (Exception e) {
			System.err.println("Exception while reading unvalidated population file: ");
			System.err.println(e);
			e.printStackTrace();
			completed = true;
			return;
		}

		try {
			String validatedPopulationString = DirectoryTree.fileToString( DirectoryTree.getResultsDirectory() + searchName + "/validated_population" );
			st = new StringTokenizer(validatedPopulationString, "\n");
			while (st.hasMoreTokens()) {
				line = st.nextToken();
				int position = XMLTemplate.processInteger(line, "id");
				String resultString = XMLTemplate.processString(line, "result");

				SearchResult result = SearchResult.parseFromString(resultString, line);
				result.setSearchName(searchName);
				validatedPopulation.put( new Integer(position), result );
			}
		} catch (IOException e) {
			System.err.println("IOException while reading validated population file: ");
			System.err.println(e);
			e.printStackTrace();
			completed = true;
			return;
		} catch (Exception e) {
			System.err.println("Exception while reading validated population file: ");
			System.err.println(e);
			e.printStackTrace();
			completed = true;
			return;
		}
	}

	public boolean requiresValidation(SearchResult result) {
		if (completed) return false;

		Integer position = new Integer(getPositionFromMetadata(result.getMetadata()));
		if (position < 0) return true;
		SearchResult current = validatedPopulation.get(position);

		if (current == null || current.getFitness() < result.getFitness()) return true;
		return false;
	}

	public void insertValidatedIndividual(SearchResult result) {
		if (completed) return;

		Integer position = new Integer(getPositionFromMetadata(result.getMetadata()));
		if (position < 0) return;
		SearchResult current = validatedPopulation.get(position);
		String resultString = "inserted";

		if (current == null || current.getFitness() < result.getFitness()) {
			resultString = "inserted";
			SearchResult best = validatedPopulation.get( new Integer(bestValidatedIndividual) );
			if (best == null || best.getFitness() < result.getFitness()) {
				bestValidatedIndividual = position.intValue();
				resultString += " global best";
			}

			validatedPopulation.put(position, result);
			try {
				writeToFile();
			} catch (Exception e) {
				System.err.println("Error writing to search file: " + e);
				e.printStackTrace();
				System.exit(0);
			}
		}
		individualsAnalyzed++;
		updateValidLog(position, result, current, resultString);
	}

	public void notifyUnvalidatedIndividual(SearchResult result) {
		if (completed) return;
		if (pessimistic) return;

		Integer position = new Integer(getPositionFromMetadata(result.getMetadata()));
		if (position < 0) return;
		SearchResult current = unvalidatedPopulation.get(position);
		String resultString = "not inserted";

		if (current == null || current.getFitness() < result.getFitness()) {
			resultString = "inserted";
			SearchResult best = unvalidatedPopulation.get( new Integer(bestUnvalidatedIndividual) );
			if (best == null || best.getFitness() < result.getFitness()) {
				bestUnvalidatedIndividual = position.intValue();
				resultString += " global best";
			}

			unvalidatedPopulation.put(position, result);
			try {
				writeToFile();
			} catch (Exception e) {
				System.err.println("Error writing to search file: " + e);
				e.printStackTrace();
				System.exit(0);
			}
		}
		individualsAnalyzed++;
		updateUnvalidLog(position, result, current, resultString);
	}

	public void notifyInvalidatedIndividual(SearchResult result) {
		if (completed) return;
		if (pessimistic) return;

		Integer position = new Integer(getPositionFromMetadata(result.getMetadata()));
		if (position < 0) return;
		SearchResult current = unvalidatedPopulation.get(position);

		String resultString = "invalidated";
		if (current == null) return;
		else if (current.getFitness() == result.getFitness()) {
			unvalidatedPopulation.put(position, validatedPopulation.get(position));

			if (position.equals(new Integer(bestUnvalidatedIndividual))) {
				LinkedList<SearchResult> values = new LinkedList<SearchResult>( unvalidatedPopulation.values() );
				Collections.sort(values);

				bestUnvalidatedIndividual = getPositionFromMetadata( values.getLast().getMetadata() );
				resultString += " reverted global best";
			}

			try {
				writeToFile();
			} catch (Exception e) {
				System.err.println("Error writing to search file: " + e);
				e.printStackTrace();
				System.exit(0);
			}
		}
		updateUnvalidLog(position, result, validatedPopulation.get(position), resultString);
	}

	public SearchParameters getNewIndividual() {
		if (completed) return null;
		Integer bestIndividual;

		HashMap<Integer,SearchResult> population;
		if (pessimistic) {
			population = validatedPopulation;
			bestIndividual = bestValidatedIndividual;
		} else {
			population = unvalidatedPopulation;
			bestIndividual = bestUnvalidatedIndividual;
		}

		Integer position = new Integer(currentIndividual);
		currentIndividual++;
		if (currentIndividual >= populationSize) currentIndividual = 0;

		SearchResult current = population.get(position);
		if (current == null) return Recombination.getRandomIndividual(searchName, minBound, maxBound, "metadata: i: " + position);

		SearchResult parent = null;
		if (parentType == DifferentialEvolution.BEST_PARENT) {
			parent = population.get(new Integer(bestIndividual));
		} else if (parentType == DifferentialEvolution.CURRENT_PARENT) {
			parent = current;
		} else if (parentType == DifferentialEvolution.RANDOM_PARENT) {
			Integer parentPosition = new Integer((int)(Math.random() * ((double)populationSize)));
			parent = population.get(parentPosition);
		}
		if (parent == null) return Recombination.getRandomIndividual(searchName, minBound, maxBound, "metadata: i: " + position);

		if (population.size() < recombinationPairs * 2) return Recombination.getRandomIndividual(searchName, minBound, maxBound, "metadata: i: " + position);
		ArrayList<SearchResult> pairs = new ArrayList<SearchResult>();

		ArrayList<SearchResult> populationList = new ArrayList<SearchResult>( population.values() );
		Collections.shuffle(populationList);

		for (int i = 0; i < recombinationPairs * 2; i++) pairs.add( populationList.get(i) );

		SearchParameters result = new SearchParameters(searchName, numberParameters);
		for (int i = 0; i < numberParameters; i++) {
			result.parameters[i] = 0;

			for (int j = 0; j < recombinationPairs; j++) {
				result.parameters[i] += pairs.get( (int)(j * 2.0) ).parameters[i] - pairs.get( (int)(j * 2.0) + 1 ).parameters[i];
			}
			
			result.parameters[i] = parent.parameters[i] + pairWeight * result.parameters[i];
		}

		if (recombinationType == DifferentialEvolution.BINOMIAL_RECOMBINATION) {
			int selected = (int)(Math.random() * numberParameters);
			for (int i = 0; i < numberParameters; i++) {
				if (i != selected && Math.random() > crossoverRate) {
					result.parameters[i] = current.parameters[i];
				}
			}
		}

		result.setMetadata("metadata: i: " + position);
		Recombination.boundParameters(result, minBound, maxBound);

		return result;
	}

	public int getPositionFromMetadata(String metadata) {
		try {
			StringTokenizer st = new StringTokenizer(metadata, " ,");
			st.nextToken();
			st.nextToken();

			return Integer.parseInt(st.nextToken());
		} catch (Exception e) {
			System.err.println("Metadata [" + metadata + "] didn't specify position: " + e);
			e.printStackTrace();

			return -1;
		}
	}

	private BufferedWriter validated_log = null;
	private BufferedWriter unvalidated_log = null;

	private BufferedWriter getValidatedLog() {
		try {
			if (validated_log == null) {
				validated_log = new BufferedWriter(new FileWriter(new File(DirectoryTree.getResultsDirectory() + searchName + "/validated_log"), true ));
			}
			return validated_log;
		} catch (Exception e) {
			System.err.println("could not open validated log: " + validated_log);
			e.printStackTrace();
			return null;
		}
	}

	private BufferedWriter getUnvalidatedLog() {
		try {
			if (unvalidated_log == null) {
				unvalidated_log = new BufferedWriter(new FileWriter(new File(DirectoryTree.getResultsDirectory() + searchName + "/unvalidated_log" ), true));
			}
			return unvalidated_log;
		} catch (Exception e) {
			System.err.println("could not open unvalidated log: " + unvalidated_log);
			e.printStackTrace();
			return null;
		}
	}

	private void writeTo(BufferedWriter log, String text) {
		if (log == null) return;
		try {
			log.write(text + "\n");
			log.flush();
		} catch (Exception e) {
			System.err.println("Error writing to log: " + log);
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
	}

	public String getSearchStats(HashMap<Integer,SearchResult> population) {
		double best = 0;
		double average = 0;
		double median = 0;
		double worst = 0;

		LinkedList<SearchResult> values = new LinkedList<SearchResult>( population.values() );
		Collections.sort(values);

		worst = values.get(0).getFitness();
		best = values.get(values.size() - 1).getFitness();
		median = values.get(values.size() / 2).getFitness();

		for (SearchResult value : values) average += value.getFitness();
		average /= values.size();

		return individualsAnalyzed + "\t" + best + "\t" + average + "\t" + median + "\t" + worst;
	}

	public void updateUnvalidLog(Integer position, SearchResult result, SearchResult previous, String resultString) {
		String fitness1 = "null";
		String fitness2 = "null";
		String application = "unknown";

		if (result != null) {
			fitness1 = Double.toString(result.getFitness());
			application = result.getApplication();
		}
		if (previous != null) fitness2 = Double.toString(previous.getFitness());

		writeTo(getUnvalidatedLog(), getSearchStats(unvalidatedPopulation));

//		System.err.println("Updated Unvalidated Population: [" + position + "] [new result: " + fitness1 + "] [old result: " + fitness2 + "] [action: " + resultString + "] [application: " + application + "]");
	}

	public void updateValidLog(Integer position, SearchResult result, SearchResult previous, String resultString) {
		String fitness1 = "null";
		String fitness2 = "null";
		String application = "unknown";

		if (result != null) {
			fitness1 = Double.toString(result.getFitness());
			application = result.getApplication();
		}
		if (previous != null) fitness2 = Double.toString(previous.getFitness());

		writeTo(getValidatedLog(), getSearchStats(validatedPopulation));

//		System.err.println("Updated Validated Population: [" + position + "] [new result: " + fitness1 + "] [old result: " + fitness2 + "] [action: " + resultString + "] [application: " + application + "]");
	}
}
