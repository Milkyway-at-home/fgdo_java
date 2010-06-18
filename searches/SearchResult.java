package fgdo_java.searches;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Arrays;
import java.util.StringTokenizer;

import fgdo_java.util.XMLTemplate;
import fgdo_java.util.XMLParseException;

public class SearchResult implements Comparable<SearchResult> {

	private final static int MINIMUM_PRECISION = 15;

	public SearchResult(File file) throws SearchResultException {
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			searchName = in.readLine();
			if (searchName == null) throw new SearchResultException("searchName not specified");

			String parametersLine = in.readLine();
			int length = Integer.parseInt( parametersLine.substring( parametersLine.indexOf("[") + 1, parametersLine.indexOf("]") ) );
			parameters = new double[length];
			StringTokenizer st = new StringTokenizer(parametersLine.substring(parametersLine.indexOf(":") + 1), ", ");
			for (int i = 0; i < length; i++) {
				parameters[i] = Double.parseDouble(st.nextToken());
			}

			metadata = in.readLine();
			if (metadata == null) throw new SearchResultException("metadata not specified");

			String fitnessString = in.readLine();
			application = in.readLine();

			if (fitnessString == null) {
				throw new SearchResultException("fitness not specified, application: " + application);
			}
			if (fitnessString.length() < (MINIMUM_PRECISION + 3 + 9) ) {
				throw new SearchResultException("not enough fitness precision: " + fitnessString + ", length: " + fitnessString.length() + ", application: " + application);
			}

			fitness = Double.parseDouble(fitnessString.substring(9));

			if (application == null) throw new SearchResultException("application not specified");
		} catch (IOException e) {
			throw new SearchResultException("IO exception while reading file: " + file, e);
		} catch (Exception e) {
			throw new SearchResultException("exception while reading file: " + file, e);
		}
	}

	public SearchResult() {
	}

	public SearchResult(boolean valid) {
		this.valid = valid;
	}

	public SearchResult(String searchName, String application, double fitness, double[] parameters, String metadata) {
		this.searchName = searchName;
		this.application = application;
		this.fitness = fitness;
		this.metadata = metadata;

		this.parameters = new double[parameters.length];
		for (int i = 0; i < this.parameters.length; i++) this.parameters[i] = parameters[i];
	}

	private boolean valid = true;

	private String searchName;
	private String metadata;
	private String application;
	private double fitness;
	protected double[] parameters;

	public double getFitness() { return fitness; }
	public String getSearchName() { return searchName; }
	public String getMetadata() { return metadata; }
	public String getApplication() { return application; }

	public void setMetadata(String metadata) { this.metadata = metadata; }
	public void setSearchName(String searchName) { this.searchName = searchName; }

	public boolean isValid() {
		return valid;
	}

	public int compareTo(SearchResult other) {
		if (this.fitness < other.fitness) return -1;
		else if (this.fitness > other.fitness) return 1;
		else return 0;
	}

	public String toString() {
		if (valid) {
			return "[SearchResult: valid, " + searchName + ", " + application + ", " + fitness + ", " + Arrays.toString(parameters) + ", " + metadata + "]";
		} else {
			return "[SearchResult: invalid, " + searchName + ", " + application + ", " + fitness + ", " + Arrays.toString(parameters) + ", " + metadata + "]";
		}
	}

	public String toFileString() {
		StringBuffer result = new StringBuffer();

		result.append( "<fitness>" + fitness + "</fitness>" );
		result.append( " <application>" + application + "</application>" );
		result.append( " <parameters>" + Arrays.toString(parameters) + "</parameters>" );
		result.append( " <metadata>" + metadata + "</metadata>" );

		return result.toString();
	}

	public static SearchResult parseFromString(String searchName, String inputString) throws XMLParseException {
		SearchResult sr = new SearchResult();

		sr.searchName = searchName;
		sr.fitness = XMLTemplate.processDouble(inputString, "fitness");
		sr.application = XMLTemplate.processString(inputString, "application");
		sr.metadata = XMLTemplate.processString(inputString, "metadata");
		sr.parameters = XMLTemplate.processDoubleArray(inputString, "parameters");

		return sr;
	}
}
