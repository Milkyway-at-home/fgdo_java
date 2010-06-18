package fgdo_java.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.LinkedList;
import java.util.StringTokenizer;


public class WorkunitInformation {

	private LinkedList<String> inputFiles = new LinkedList<String>();
	public LinkedList<String> getInputFiles() { return inputFiles; }
	public void addInputFile(String inputFile) {
		inputFiles.add(inputFile);
	}

	private String workunitTemplateFile;
	public String getWorkunitTemplateFilename() { return workunitTemplateFile; }
	public void setWorkunitTemplateFile(String file) {
		workunitTemplateFile = file;
	}

	private String resultTemplateFile;
	public String getResultTemplateFilename() { return resultTemplateFile; }
	public void setResultTemplateFile(String file) {
		resultTemplateFile = file;
	}


	private LinkedList<String> extraXML = new LinkedList<String>();
	public LinkedList<String> getExtraXML() { return extraXML; }
	public void addExtraXML(String xml) {
		extraXML.add(xml);
	}


	public WorkunitInformation() {
	}

	public void writeToFile(File file) throws IOException {
		BufferedWriter out = new BufferedWriter( new FileWriter(file) );

		out.write("<extra_xml>\n");
		for (String extra : extraXML) {
			out.write(extra + "\n");
		}
		out.write("</extra_xml>\n");

		out.write("<workunit_template>" + workunitTemplateFile + "</workunit_template>\n");
		out.write("<result_template>" + resultTemplateFile + "</result_template>\n");
		out.write("<input_files>\n");
		for (String inputFile : inputFiles) {
			out.write(inputFile + "\n");
		}
		out.write("</input_files>\n");

		out.close();
	}

	public static WorkunitInformation getFromFile(File file) {
		if (!file.exists()) {
			return null;
		}
		WorkunitInformation wuInfo = new WorkunitInformation();

		try {
			String fileString = DirectoryTree.fileToString(file.toString());

			StringTokenizer st = new StringTokenizer(fileString, "\n");
			st.nextToken();

			wuInfo.workunitTemplateFile = fileString.substring( fileString.indexOf("<workunit_template>") + 19, fileString.indexOf("</workunit_template>") );
			wuInfo.resultTemplateFile = fileString.substring( fileString.indexOf("<result_template>") + 17, fileString.indexOf("</result_template>") );

			String line;
			while ( !(line = st.nextToken()).equals("</extra_xml>") ) {
				wuInfo.extraXML.add(line);
			}

			while ( !st.nextToken().equals("<input_files>") ) ;

			while ( !(line = st.nextToken()).equals("</input_files>") ) {
				wuInfo.inputFiles.add(line);
			}

		} catch (IOException e) {
			System.err.println("IOException while reading workunit information from: " + file);
			System.err.println(e);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			System.err.println("Exception while reading workunit information from: " + file);
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
		return wuInfo;
	}


}
