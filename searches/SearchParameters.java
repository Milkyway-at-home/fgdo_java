package fgdo_java.searches;

import fgdo_java.database.Workunit;
import fgdo_java.util.XMLTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import java.text.NumberFormat;


public class SearchParameters {

	private String searchName;
	protected double[] parameters;
	private String metadata;

	public SearchParameters(String searchName, int number_parameters) {
		this.searchName = searchName;
		parameters = new double[number_parameters];
	}

	public void setMetadata(String metadata) { this.metadata = metadata; }
	public String getMetadata() { return metadata; }

	public SearchParameters(Workunit workunit) {
		String xmlTemplate = workunit.getWorkunitTemplate();

		searchName = XMLTemplate.getEntry(xmlTemplate, "search_name");
		metadata = XMLTemplate.getEntry(xmlTemplate, "search_metadata");

		String parametersString = XMLTemplate.getEntry(xmlTemplate,  "search_parameters");
		StringTokenizer st = new StringTokenizer(parametersString, "[] ,");
		parameters = new double[st.countTokens()];
		for (int i = 0; i < parameters.length; i++) parameters[i] = Double.parseDouble(st.nextToken());
	}

	public String getCommandLine() {
		StringBuffer commandLine = new StringBuffer();

		commandLine.append("-np ");
		commandLine.append(parameters.length);
		commandLine.append(" -p");
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(25);
		for (int i = 0; i < parameters.length; i++) {
			commandLine.append(" ");
			commandLine.append( nf.format(parameters[i]) );
		}

		return commandLine.toString();
	}

	public String getWorkunitXML() {
		StringBuffer workunitXML = new StringBuffer("<search_name>");

		workunitXML.append(searchName);
		workunitXML.append("</search_name>\n<search_parameters>");

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(25);
		for (int i = 0; i < parameters.length; i++) {
			workunitXML.append(" ");
			workunitXML.append( nf.format(parameters[i]) );
		}

		workunitXML.append("</search_parameters>\n<search_metadata>");
		workunitXML.append(metadata);
		workunitXML.append("</search_metadata>");

		return workunitXML.toString();
	}

	public String toString() {
		return "search parameters[" + searchName + "] " + Arrays.toString(parameters) + " [" + metadata + "]";
	}

	public void writeToFile(String filename) {
		File file = new File(filename);

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(searchName + "\n");
			out.write("parameters [" + parameters.length + "]:");

			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumFractionDigits(25);
			for (int i = 0; i < parameters.length; i++) {
				out.write(" ");
				out.write( nf.format(parameters[i]) );
			}

			out.write("\n");
			out.write(metadata + "\n");
			out.close();

		} catch (IOException e) {
			System.err.println("Writing search parameters to file threw IOException: " + this);
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Writing search parameters to file threw Exception: " + this);
			System.err.println(e);
			e.printStackTrace();
		}
	}
}
