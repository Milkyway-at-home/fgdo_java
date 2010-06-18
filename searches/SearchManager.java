package fgdo_java.searches;

import fgdo_java.daemons.WorkGenerator;

import fgdo_java.database.BoincDatabase;
import fgdo_java.database.DatabaseRetrieveException;
import fgdo_java.database.Workunit;

import fgdo_java.util.DirectoryTree;
import fgdo_java.util.XMLTemplate;
import fgdo_java.util.WorkunitInformation;

import java.util.LinkedList;
import java.util.HashMap;

public class SearchManager {

	private static HashMap<String,Search> searches = new HashMap<String,Search>();

	public static Search getSearch(String searchName) {
		Search search = searches.get(searchName);

		if (search == null) {
			search = Search.initializeSearch(searchName);
			searches.put(searchName, search);
		}
		return search;
	}
	
	public static boolean requiresValidation(SearchResult searchResult) {
		return getSearch(searchResult.getSearchName()).requiresValidation(searchResult);
	}

	public static void notifyUnvalidatedIndividual(SearchResult searchResult) {
		getSearch(searchResult.getSearchName()).notifyUnvalidatedIndividual(searchResult);
	}

	public static void notifyInvalidatedIndividual(SearchResult searchResult) {
		getSearch(searchResult.getSearchName()).notifyInvalidatedIndividual(searchResult);
	}

	public static void notifyValidatedIndividual(SearchResult searchResult) {
		getSearch(searchResult.getSearchName()).insertValidatedIndividual(searchResult);
	}

	public static LinkedList<Search> getIncompleteSearches() {
		LinkedList<Search> incompleteSearches = new LinkedList<Search>();
		LinkedList<Search> allSearches = new LinkedList<Search>(searches.values());

		for (Search search : allSearches) {
			if (!search.isCompleted()) incompleteSearches.add(search);
		}
		return incompleteSearches;
	}

	private static int generatedWorkunits = 0;
	public static void generateWorkunits(int number) {
		LinkedList<Search> incompleteSearches = getIncompleteSearches();

		SearchParameters parameters;
		for (Search search : incompleteSearches) {
			for (int i = 0; i < number; i++) {
				parameters = search.getNewIndividual();
//				System.out.println(parameters);

				if (parameters == null) {
					System.out.println("Search: " + search.getName() + " could not generate a workunit.");
					break;
				}

				WorkunitInformation wuInfo = search.getWorkunitInformation();
				if (wuInfo == null) {
					System.out.println("wuInfo is null!! search is completed.");
					search.setCompleted();
				}

				LinkedList<String> inputFiles = new LinkedList<String>( wuInfo.getInputFiles() );
				String workunitName = search.getName() + "_" + (++generatedWorkunits) + "_" + (System.currentTimeMillis() / 1000);

				if (System.getProperty("generate_wu_file") != null) {
					String parameterFile = workunitName + "_search_parameters";
					parameters.writeToFile(DirectoryTree.getDownloadPath(parameterFile));
					inputFiles.add(parameterFile);
				} else {
//					System.out.println("Generated 0 workunits because -Dgenerate_wu_file not specified.");
//					return;
				}

				WorkGenerator.createWorkunit(workunitName, wuInfo.getWorkunitTemplateFilename(), wuInfo.getResultTemplateFilename(), inputFiles, wuInfo.getExtraXML(), parameters.getCommandLine(), parameters.getWorkunitXML());
			}
			System.out.println("Generated " + number + " workunits.");
		}
	}

	public static double getCreditFor(Workunit workunit) {
		String xml_template = "";
		
		try {
			xml_template = BoincDatabase.getWorkunitTemplate(workunit.getId());
		} catch (DatabaseRetrieveException e) {
			System.err.println("Could not retrieve credit from database for workunit: " + workunit);
			System.err.println("Exception: " + e);
			e.printStackTrace();
			return 0;
		}

		return Double.parseDouble( XMLTemplate.getEntry(xml_template, "credit") );
	}
}
