package fgdo_java.daemons;

import fgdo_java.database.BoincDatabase;
import fgdo_java.database.DatabaseCommitException;
import fgdo_java.database.Workunit;
import fgdo_java.database.WorkunitCreationException;

import fgdo_java.util.DirectoryTree;
import fgdo_java.util.XMLTemplate;
import fgdo_java.util.XMLParseException;

import java.io.IOException;

import java.util.LinkedList;
import java.util.HashMap;

public class WorkGenerator {

	private static HashMap<String,String> workunitTemplates = new HashMap<String,String>();

	public static void createWorkunit(String name, String workunitTemplateFilename, String resultTemplateFilename, LinkedList<String> inputFiles, LinkedList<String> extraXML, String commandLine, String additionalXML) {
		Workunit workunit = new Workunit();
		/**
		 * Set default values.
		 */
		workunit.setName(name);

		try {
			workunit.setAppid( BoincDatabase.getApplication().getId() );
		} catch (Exception e) {
			System.err.println("Exception setting workunit application: " + e);
			e.printStackTrace();
			System.exit(0);
		}

		String workunitTemplate = workunitTemplates.get(DirectoryTree.getBaseDirectory() + workunitTemplateFilename);
		if (workunitTemplate == null) {
			try {
				workunitTemplate = DirectoryTree.fileToString(DirectoryTree.getBaseDirectory() + workunitTemplateFilename);
				workunitTemplates.put(DirectoryTree.getBaseDirectory() + workunitTemplateFilename, workunitTemplate);
			} catch (IOException e) {
				System.err.println("IOException: " + e + " while reading workunit template file: " + workunitTemplateFilename);
				e.printStackTrace();
				System.exit(0);
			}
	//		System.out.println("workunitTemplate:\n" + workunitTemplate + "\n\n");
		}

		try {
			workunit.create(resultTemplateFilename, workunitTemplate, inputFiles.toArray(new String[0]), extraXML, commandLine, additionalXML);
		} catch (WorkunitCreationException wce) {
			System.out.println(wce);
			wce.printStackTrace();
		}
	}

	public static void main(String[] arguments) {
		String workunitTemplateFilename = "/export/www/boinc/milkyway/templates/milkyway_wu.xml";
		String resultTemplateFilename = "templates/a_result.xml";
		
		LinkedList<String> inputFiles = new LinkedList<String>();
		inputFiles.add("parameter_222F5_3s.txt");
		inputFiles.add("stars222F5");
		inputFiles.add("de_s222_3s_best_4p_05r_22_search_parameters_2631_1259007554");

		String commandLine = "";
		String additionalXML = "";

		DirectoryTree.setBaseDirectory("/export/www/boinc/milkyway/");
		DirectoryTree.setBaseURL("http://milkyway.cs.rpi.edu/milkyway/");
		DirectoryTree.setResultsDirectory("/export/www/boinc/milkyway/results/milkyway/");

		createWorkunit("test_workunit_" + System.currentTimeMillis(), workunitTemplateFilename, resultTemplateFilename, inputFiles, new LinkedList<String>(), commandLine, additionalXML + "<credit>500</credit>");
	}
}
