package fgdo_java.database;

import fgdo_java.util.DirectoryTree;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.StringReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
*/

public class Result extends DatabaseEntry {
	public Result() {
	}

	public static final int RESULT_SERVER_STATE_UNSENT = 2, RESULT_SERVER_STATE_IN_PROGRESS = 4, RESULT_SERVER_STATE_OVER = 5;
	public static final String[] server_states = {"", "", "UNSENT", "", "IN_PROGRESS", "OVER" };
	public static final int RESULT_OUTCOME_INIT = 0, RESULT_OUTCOME_SUCCESS = 1, RESULT_OUTCOME_COULDNT_SEND = 2, RESULT_OUTCOME_NO_REPLY = 4, RESULT_OUTCOME_DIDNT_NEED = 5, RESULT_OUTCOME_VALIDATE_ERROR = 6, RESULT_OUTCOME_CLIENT_DETACHED = 7;
	public static final String[] outcomes = { "INIT", "SUCCESS", "COULDNT_SEND", "NO_REPLY", "DIDNT_NEED", "VALIDATE_ERROR", "CLIENT_DETACHED" };
	public static final int VALIDATE_STATE_INIT = 0, VALIDATE_STATE_VALID = 1, VALIDATE_STATE_INVALID = 2, VALIDATE_STATE_NO_CHECK = 3, VALIDATE_STATE_INCONCLUSIVE = 4, VALIDATE_STATE_TOO_LATE = 5;
	public static final String[] validate_states = { "INIT", "VALID", "INVALID", "NO_CHECK", "INCONCLUSIVE", "TOO_LATE" };

	private int id;
	private String name;
	private int validate_state;
	private int server_state;
	private int outcome;
	private double claimed_credit;
	private double granted_credit;
	private String xml_doc_in;
	private String xml_doc_out;
	private String stderr_out;
	private double cpu_time;
	private int batch;
	private double opaque;
	private int exit_status;
	private int host_id;
	private int user_id;
	private int team_id;
	private long sent_time;
	private long received_time;
	private int application_id;

	public void setValidateState(int validate_state) {
		this.validate_state = validate_state;
		appendModification("validate_state", Integer.toString(this.validate_state));
	}

	public void setServerState(int server_state) {
		this.server_state = server_state;
		appendModification("server_state", Integer.toString(this.server_state));
	}

	public void setOutcome(int outcome) {
		this.outcome = outcome;
		appendModification("outcome", Integer.toString(this.outcome));
	}

	public void setGrantedCredit(double granted_credit) {
		this.granted_credit = granted_credit;
		appendModification("granted_credit", Double.toString(this.granted_credit));
	}

	public void setStderrOut(String stderr_out) {
		this.stderr_out += stderr_out;
		appendModification("stderr_out", "'" + this.stderr_out + "'");
	}


	public int getId() { return id; }
	public int getHostId() { return host_id; }
	public int getUserId() { return user_id; }
	public int getTeamId() { return team_id; }
	public int getValidateState() { return validate_state; }
	public int getServerState() { return server_state; }
	public int getOutcome() { return outcome; }
	public long getSentTime() { return sent_time; }
	public long getReceivedTime() { return received_time; }
	public double getCPUTime() { return cpu_time; }
	public double getGrantedCredit() { return granted_credit; }
	public String getXMLDocIn() { return xml_doc_in; }
	public String getXMLDocOut() { return xml_doc_out; }
	public String getStderrOut() { return stderr_out; }

	public void commit() throws DatabaseCommitException {
		super.commit("UPDATE result", "WHERE id = " + id);
	}

	public Result(ResultSet resultSet, int offset) {
		try {
			id = resultSet.getInt(offset + 0);
			name = resultSet.getString(offset + 1);
			validate_state = resultSet.getInt(offset + 2);
			server_state = resultSet.getInt(offset + 3);
			outcome = resultSet.getInt(offset + 4);
			claimed_credit = resultSet.getDouble(offset + 5);
			granted_credit = resultSet.getDouble(offset + 6);
			xml_doc_in = resultSet.getString(offset + 7);
			xml_doc_out = resultSet.getString(offset + 8);
			stderr_out = resultSet.getString(offset + 9);
			cpu_time = resultSet.getDouble(offset + 10);
			batch = resultSet.getInt(offset + 11);
			opaque = resultSet.getDouble(offset + 12);
			exit_status = resultSet.getInt(offset + 13);
			host_id = resultSet.getInt(offset + 14);
			user_id = resultSet.getInt(offset + 15);
			team_id = resultSet.getInt(offset + 16);
			sent_time = resultSet.getLong(offset + 17);
			received_time = resultSet.getLong(offset + 18);
			application_id = resultSet.getInt(offset + 19);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public boolean needsValidation() {
		if (server_state != RESULT_SERVER_STATE_OVER) return false;
		if (outcome != RESULT_OUTCOME_SUCCESS) return false;

		switch (validate_state) {
			case Result.VALIDATE_STATE_INIT:
			case Result.VALIDATE_STATE_INCONCLUSIVE:
				return true;
			default:
				return false;
		}       
	}

	public boolean isSuccessful() {
		return server_state == RESULT_SERVER_STATE_OVER && outcome == RESULT_OUTCOME_SUCCESS;
	}

	public String getOutputFile() {
/*		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml_doc_in)));
			
		} catch (ParserConfigurationException pce) {
			System.err.println("Getting output file resulted in ParserConfigurationException: " + pce);
			pce.printStackTrace();
		} catch (SAXParseException saxe) {
			System.err.println("Getting output file resulted in SAXParseException: " + saxe);
			saxe.printStackTrace();
			System.err.println("On line number: " + saxe.getLineNumber() + ", column: " + saxe.getColumnNumber());
			System.err.println("Message: " + saxe.getMessage());
		} catch (SAXException saxe) {
			System.err.println("Getting output file resulted in SAXException: " + saxe);
			saxe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Getting output file resulted in IOException: " + ioe);
			ioe.printStackTrace();
		}
*/
		int first_index = xml_doc_in.indexOf("<file_name>") + 11;
		int last_index = xml_doc_in.indexOf("</file_name>", first_index);

		return xml_doc_in.substring(first_index, last_index);
	}

	public String getFullOutputFile() {
		String outputFile = getOutputFile();
		return DirectoryTree.getUploadDirectory(outputFile) + "/" + outputFile;
	}

	public String toString() {
		return	"[id: " + id +
			"] [name: " + name +
//			"] [validate_state: " + validate_states[validate_state] +
//			"] [server_state: " + server_states[server_state] +
//			"] [outcome: " + outcomes[outcome] +
			"] [claimed_credit: " + claimed_credit +
			"] [granted_credit: " + granted_credit +
			"] [cpu_time: " + cpu_time +
			"] [host_id: " + host_id + "]";
//			"] [user_id: " + user_id +
//			"] [team_id: " + team_id + "]";
//			"] [sent_time: " + sent_time +
//			"] [received_time: " + received_time + "]";
//			"] [application_id: " + application_id + "]";
	}
}
