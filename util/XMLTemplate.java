package fgdo_java.util;

import fgdo_java.database.Workunit;

import com.twmacinta.util.MD5;

import java.io.File;
import java.io.IOException;

import java.util.LinkedList;
import java.util.StringTokenizer;

public class XMLTemplate {

	public static LinkedList<String> getContent(StringTokenizer st, String endTag) throws XMLParseException {
		LinkedList<String> content = new LinkedList<String>();

		String line = st.nextToken().trim();

		while (!line.equals(endTag)) {
			content.add(line);

			if (st.hasMoreTokens()) line = st.nextToken().trim();
			else throw new XMLParseException("Could not get content with end tag: " + endTag);
		}

		return content;
	}

	public static String processString(String content, String tag) throws XMLParseException {
		int first_index = content.indexOf("<" + tag + ">");

		if (first_index < 0) throw new XMLParseException("Expected tag <" + tag + "> in content: " + content);

		first_index += tag.length() + 2;
		int last_index = content.lastIndexOf("</" + tag + ">");

		if (first_index > content.length() || last_index < 0) throw new XMLParseException("<" + tag + ">X</" + tag + "> incorrectly specified, must be single line, without spaces in </" + tag + "> from content: " + content);

		return content.substring(first_index, last_index).trim();
	}

	public static int processInteger(String content, String tag) throws XMLParseException {
		String value = processString(content, tag); 

		int number;
		try {
			number = Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw new XMLParseException(" <" + tag + ">X</" + tag + "> did not contain an integer, from value: " + value);
		}
		return number;
	}

	public static double processDouble(String content, String tag) throws XMLParseException {
		String value = processString(content, tag);

		double number;
		try {
			number = Double.valueOf(value);
		} catch (NumberFormatException e) {
			throw new XMLParseException(" <" + tag + ">X</" + tag + "> did not contain an double, from value: " + value);
		}
		return number;
	}

	public static boolean processBoolean(String content, String tag) throws XMLParseException {
		if (content.equals("<" + tag + "/>")) return true;
		if (content.equals("<" + tag + " />")) return true;

		String value = processString(content, tag);

		if (value.equals("true")) return true;
		else if (value.equals("false")) return false;
		else if (value.equals("1")) return true;
		else if (value.equals("0")) return false;
		else {
			throw new XMLParseException(" <" + tag + ">X</" + tag +"> did not contain a boolean, from value: " + value);
		}
	}

	public static String getEntry(String xml, String tag) {
		return xml.substring( xml.indexOf("<" + tag + ">") + tag.length() + 2, xml.indexOf("</" + tag + ">") );

	}

	public static double[] processDoubleArray(String xml, String tag) {
		String arrayString = xml.substring( xml.indexOf("<" + tag + ">") + tag.length() + 2, xml.indexOf("</" + tag + ">") );
		StringTokenizer st = new StringTokenizer(arrayString, "[], ");

		double[] result = new double[st.countTokens()];
		for (int i = 0; i < result.length; i++) result[i] = Double.parseDouble(st.nextToken());

		return result;
	}

	public static String processWorkunitTemplate(Workunit workunit, String workunit_template, String[] input_files, LinkedList<String> extraXML, String command_line, String additional_xml) throws XMLParseException {
		StringTokenizer st = new StringTokenizer(workunit_template, "\n\r");
		StringBuffer sb = new StringBuffer();

		String line;

		while (st.hasMoreTokens()) {
			line = st.nextToken().trim();

			if (line.startsWith("<file_info>")) {
				LinkedList<String> content = getContent(st, "</file_info>");

				boolean generated_locally = false;
				int number = -1;

				sb.append("<file_info>\n");
				for (int i = 0; i < content.size(); i++) {
					if (content.get(i).startsWith("<number>")) {
						number = processInteger(content.get(i), "number");
					} else if (content.get(i).contains("generated_locally")) {
						generated_locally = processBoolean(content.get(i), "generated_locally");
					} else {
						sb.append("\t");
						sb.append(content.get(i));
						sb.append("\n");
					}
				}

				if (number < 0) throw new XMLParseException("<number>X</number> field not specified for <file_info>...</file_info>, or X < 0.");
				if (number >= input_files.length) {
					System.out.println("WORKUNIT TEMPLATE:\n" + workunit_template);
					throw new XMLParseException("<file_info> (" + number + ") number greater than number of input files (" + input_files.length + ").");
				}

				sb.append("\t<name>");
				sb.append(input_files[number]);
				sb.append("</name>\n");

				if (generated_locally) {
					sb.append("\t<generated_locally/>\n");
				} else {
					String boinc_filename = DirectoryTree.getDownloadPath(input_files[number]);
					File file = new File(boinc_filename);
					if (!file.exists()) throw new XMLParseException(boinc_filename + " does not exist.");
					long file_size = file.length();

					String boinc_url = DirectoryTree.getDownloadURL(input_files[number]);
					String md5_checksum = "";
					try {
						md5_checksum = DirectoryTree.getMD5Hash(boinc_filename);
					} catch (IOException e) {
						throw new XMLParseException("Could not get MD5Hash of file: " + boinc_filename, e);
					}

					sb.append("\t<url>");
					sb.append(boinc_url);
					sb.append("</url>\n\t<md5_cksum>");
					sb.append(md5_checksum);
					sb.append("</md5_cksum>\n\t<nbytes>");
					sb.append(file_size);
					sb.append("</nbytes>\n");
				}

				sb.append("</file_info>\n");

			} else if (line.startsWith("<workunit>")) {
				LinkedList<String> content = getContent(st, "</workunit>");
				content.addAll(extraXML);

				sb.append("<workunit>\n");

				for (int i = 0; i < content.size(); i++) {
					if (content.get(i).startsWith("<command_line>")) {
						if (!command_line.equals("")) throw new XMLParseException("command_line specified by the processWorkunit method, but also in the workunit template.");
						command_line = processString(content.get(i), "command_line");
					}
				}
				sb.append("<command_line>\n");
				sb.append(command_line);
				sb.append("\n</command_line>\n");

				for (int i = 0; i < content.size(); i++) {
					if (content.get(i).startsWith("<command_line>")) {

					} else if (content.get(i).startsWith("<file_ref>")) {
						int file_number = processInteger(content.get(++i), "file_number");
						String open_name = processString(content.get(++i), "open_name");

						sb.append("<file_ref>\n");
						sb.append("\t<file_name>");
						sb.append(input_files[file_number]);
						sb.append("</file_name>\n\t<open_name>");
						sb.append(open_name);
						sb.append("</open_name>\n</file_ref>\n");

					} else if (content.get(i).startsWith("</file_ref>")) {
						//do nothing
					} else if (content.get(i).startsWith("<rsc_fpops_est>")) {
						workunit.setRSCFpopsEst( processDouble(content.get(i), "rsc_fpops_est") );

					} else if (content.get(i).startsWith("<rsc_fpops_bound>")) {
						workunit.setRSCFpopsBound( processDouble(content.get(i), "rsc_fpops_bound") );

					} else if (content.get(i).startsWith("<rsc_memory_bound>")) {
						workunit.setRSCMemoryBound( processDouble(content.get(i), "rsc_memory_bound") );

					} else if (content.get(i).startsWith("<rsc_bandwidth_bound>")) {
						workunit.setRSCBandwidthBound( processDouble(content.get(i), "rsc_bandwidth_bound") );

					} else if (content.get(i).startsWith("<rsc_disk_bound>")) {
						workunit.setRSCDiskBound( processDouble(content.get(i), "rsc_disk_bound") );

					} else if (content.get(i).startsWith("<batch>")) {
						workunit.setBatch( processInteger(content.get(i), "batch") );

					} else if (content.get(i).startsWith("<delay_bound>")) {
						workunit.setDelayBound( processInteger(content.get(i), "delay_bound") );

					} else if (content.get(i).startsWith("<min_quorum>")) {
						workunit.setMinQuorum( processInteger(content.get(i), "min_quorum") );

					} else if (content.get(i).startsWith("<target_nresults>")) {
						workunit.setTargetNResults( processInteger(content.get(i), "target_nresults") );

					} else if (content.get(i).startsWith("<max_error_results>")) {
						workunit.setMaxErrorResults( processInteger(content.get(i), "max_error_results") );

					} else if (content.get(i).startsWith("<max_total_results>")) {
						workunit.setMaxTotalResults( processInteger(content.get(i), "max_total_results") );

					} else if (content.get(i).startsWith("<max_success_results>")) {
						workunit.setMaxSuccessResults( processInteger(content.get(i), "max_success_results") );

					} else {
						sb.append(content.get(i));
						sb.append("\n");
					}
				}

				sb.append(additional_xml);
				sb.append("\n</workunit>\n");

			} else {
				sb.append(line);
				sb.append("\n");
			}

		}
		return sb.toString();
	}
}
