package fgdo_java.searches;

import fgdo_java.util.DirectoryTree;
import fgdo_java.util.WorkunitInformation;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.LinkedList;
import java.util.StringTokenizer;

public abstract class Search {

	public static Search initializeSearch(String searchName) {
		Search search = new DifferentialEvolution( searchName );

		return search;
	}

	private WorkunitInformation workunitInformation = null;

	public WorkunitInformation getWorkunitInformation() {
		if (workunitInformation == null) {
			workunitInformation = WorkunitInformation.getFromFile(new File( DirectoryTree.getResultsDirectory() + getName() + "/workunit_info" ));
		}
		return workunitInformation;
	}

	protected String searchName;
	public String getName() { return searchName; }

	protected boolean completed = false;
	public boolean isCompleted() { return completed; }
	public void setCompleted() { completed = true; }

	public abstract boolean requiresValidation(SearchResult searchResult);

	public abstract void notifyUnvalidatedIndividual(SearchResult searchResult);
	public abstract void notifyInvalidatedIndividual(SearchResult searchResult);
	public abstract void insertValidatedIndividual(SearchResult searchResult);

	public abstract SearchParameters getNewIndividual();
}
