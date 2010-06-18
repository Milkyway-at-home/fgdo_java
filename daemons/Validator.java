package fgdo_java.daemons;

import fgdo_java.database.Application;
import fgdo_java.database.BoincDatabase;
import fgdo_java.database.DatabaseCommitException;
import fgdo_java.database.DatabaseRetrieveException;
import fgdo_java.database.Host;
import fgdo_java.database.Result;
import fgdo_java.database.Workunit;

import fgdo_java.searches.SearchManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;

import java.io.File;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Validator {

	private int appid;
	private int modulo;
	private int remainder;
	private int limit;
	private Application application;
	private Connection connection;
	private ValidationPolicy validationPolicy;
	private CreditPolicy creditPolicy;

	private double numberValidated = 0;
	private double numberInvalidated = 0;
	private double numberInconclusive = 0;

	public Validator(int modulo, int remainder, int limit, CreditPolicy creditPolicy, ValidationPolicy validationPolicy) {
		this.modulo = modulo;
		this.remainder = remainder;
		this.limit = limit;

		this.connection = BoincDatabase.getConnection();
		try {
			this.application = BoincDatabase.getApplication();
		} catch (DatabaseRetrieveException dre) {
			System.out.println(dre);
			dre.printStackTrace();
		}
		this.appid = this.application.getId();

		System.out.println("got application: " + application.toString());

		this.creditPolicy = creditPolicy;
		this.validationPolicy = validationPolicy;
	}



	public void start() {
		if (application == null) {
			System.err.println("Error, database could not find application");
			return;
		}

		int unsentResults;
		LinkedList<Workunit> validationWorkunits;

		int count;
		while (true) {
			count = 0;
			validationWorkunits = getValidationWorkunits();
			for (Workunit workunit : validationWorkunits) {
				validateWorkunit(workunit);
				count++;
			}
			double total = numberValidated + numberInvalidated + numberInconclusive;
			System.out.println("Validated " + count + " workunits, " + (numberValidated/total) + "% valid, " + (numberInvalidated/total) + "% invalid, " + (numberInconclusive/total) + "% inconclusive.");

			doAssimilatePass();

			try {
				unsentResults = BoincDatabase.getCount("result", "server_state=" + Result.RESULT_SERVER_STATE_UNSENT + " and appid=" + appid);
				System.out.println(unsentResults + " available.");
				if (unsentResults < 500 && System.getProperty("generate_wu_file") != null) {
					SearchManager.generateWorkunits(500);
				}
			} catch (DatabaseRetrieveException dre) {
				System.err.println("Could not generate new workunits, DatabaseRetrieveException on getting unsent result count: " + dre);
				dre.printStackTrace();
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
				System.err.println("Thread sleep interrupted!" + ie);
				ie.printStackTrace();
				return;
			}
		}
	}

	private static final int IMMEDIATE = 0, DELAYED = 1, NOCHANGE = 2, NEVER = 3;

	public void validateWorkunit(Workunit workunit) {
		int transition_time = NOCHANGE;

//		System.out.println("workunit: " + workunit);
//		System.out.println(workunit.getWorkunitTemplate());
//		for (Result result: workunit.getResults()) {
//			System.out.println("\tresult: " + result);
//			System.out.println("\toutput file: " + result.getFullOutputFile());
//			System.out.println("\texists? " + new File(result.getFullOutputFile()).exists());
//			System.out.println();
//		}

		Result canonicalResult = workunit.getCanonicalResult();
		if (canonicalResult != null) {
			//Have already found a canonical result

			for (Result result : workunit.getResults()) {
				if (!result.needsValidation()) continue;

				boolean retry = validationPolicy.checkPair(canonicalResult, result);

				// this might be last result, so let validator
				// trigger file delete etc. if needed
				transition_time = IMMEDIATE;

				switch (result.getValidateState()) {
					case Result.VALIDATE_STATE_VALID:	isValid(workunit, result); break;
					case Result.VALIDATE_STATE_INVALID:	isInvalid(workunit, result); break;
				}
				try {
					result.commit();
				} catch (DatabaseCommitException dce) {
					System.err.println("error assigning credit to host: " + result.getHostId());
					System.err.println(dce);
					dce.printStackTrace();
				}
			}

		} else {
			//No canonical result yet
		
			LinkedList<Result> successfulResults = new LinkedList<Result>();
			for (Result result : workunit.getResults()) {
				if (result.isSuccessful()) successfulResults.add(result);
			}

//			System.out.println("workunit min quorum: " + workunit.getMinQuorum() + ", canonical result id: " + workunit.getCanonicalResultid() + " canonical result: " + (workunit.getCanonicalResult() != null) );
			if (successfulResults.size() >= workunit.getMinQuorum()) {
				boolean retry = validationPolicy.checkSet(workunit, successfulResults);
				if (retry) transition_time = DELAYED;

				int successCount = 0;
				for (Result result : successfulResults) {
					if (result.getOutcome() == Result.RESULT_OUTCOME_VALIDATE_ERROR) {
						transition_time = IMMEDIATE;
					} else {
						successCount++;
					}

					switch (result.getValidateState()) {
						case Result.VALIDATE_STATE_VALID:	isValid(workunit, result); break;
						case Result.VALIDATE_STATE_INVALID:	isInvalid(workunit, result); break;
						case Result.VALIDATE_STATE_INIT:	isInconclusive(workunit, result); break;
					}
					try {
						result.commit();
					} catch (DatabaseCommitException dce) {
						System.err.println("error assigning credit to host: " + result.getHostId());
						System.err.println(dce);
						dce.printStackTrace();
					}
				}

				canonicalResult = workunit.getCanonicalResult();
				if (canonicalResult != null) {
//					System.out.println("found canonical result, ready for assimilate.");

					transition_time = NEVER;
					workunit.setAssimilateState(Workunit.ASSIMILATE_READY);

					for (Result result : workunit.getResults()) {
						//Don't send unsent results
						if (result.getServerState() != Result.RESULT_SERVER_STATE_UNSENT) continue;

						result.setServerState(Result.RESULT_SERVER_STATE_OVER);
						result.setOutcome(Result.RESULT_OUTCOME_DIDNT_NEED);
						try {
							result.commit();
						} catch (DatabaseCommitException dce) {
							System.err.println("error assigning credit to host: " + result.getHostId());
							System.err.println(dce);
							dce.printStackTrace();
						}
					}
				} else {
					if (successCount > workunit.getMaxSuccessResults()) {
						workunit.updateErrorMask(Workunit.WU_ERROR_TOO_MANY_SUCCESS_RESULTS);	// |= error_mask
						transition_time = IMMEDIATE;
					}

//					String outString = "no canonical result. successCount: " + successCount + ", targetNResults: " + workunit.getTargetNResults() + ", minQuorum: " + workunit.getMinQuorum() + ", maxSuccess: " + workunit.getMaxSuccessResults();
					if (successCount >= workunit.getTargetNResults()) {
						if (workunit.getMinQuorum() > successCount) {
							workunit.setTargetNResults(workunit.getMinQuorum());

//							outString += " -- setting TargetNResults to: " + workunit.getTargetNResults();
						} else {
							workunit.setTargetNResults(successCount + 1);

//							outString += " -- setting TargetNResults to: " + workunit.getTargetNResults();
						}
//						System.out.println(outString);
						transition_time = IMMEDIATE;
					}
				}
			}
		}

		switch (transition_time) {
			case IMMEDIATE:
				workunit.setTransitionTime(System.currentTimeMillis() / 1000);
				break;
			case DELAYED:
				long newTime = (System.currentTimeMillis() / 1000) + (6 * 3600);
				if (newTime < workunit.getTransitionTime()) workunit.setTransitionTime(newTime);
				break;
			case NEVER:
				workunit.setTransitionTime(Integer.MAX_VALUE);
				break;
			case NOCHANGE:
				break;
		}

		workunit.setNeedsValidate(0);
		try {
			workunit.commit();
		} catch (DatabaseCommitException dce) {
			System.err.println(dce);
			dce.printStackTrace();
		}
	}

	public void isValid(Workunit workunit, Result result) {
		numberValidated++;

		boolean success = creditPolicy.assignCredit(workunit, result);
		if (!success) {
			System.err.println("Error assigning credit to result: ");
			System.err.println("\tresult: " + result.toString());
			System.err.println("\tworkunit: " + workunit.toString());
		}

//		System.err.println("xml in:\n" + result.getXMLDocIn() + "\n");
//		System.err.println("xml out:\n" + result.getXMLDocOut() + "\n");
		try {
			Host host = new Host(result.getHostId());
			host.updateCredit(result.getGrantedCredit(), result.getSentTime(), result.getCPUTime());
			host.updateAverageTurnaround(result.getReceivedTime() - result.getSentTime());
			if (workunit.getMinQuorum() > 1) host.updateErrorRateValid();
			host.commit();

			System.err.println("valid result: " + result.toString());
		} catch (CreditException ce) {
			System.err.println("error assigning credit to host: " + result.getHostId());
			System.err.println(ce);
			ce.printStackTrace();
		} catch (DatabaseCommitException dce) {
			System.err.println("error assigning credit to host: " + result.getHostId());
			System.err.println(dce);
			dce.printStackTrace();
		} catch (DatabaseRetrieveException dre) {
			System.err.println("error assigning credit to host: " + result.getHostId());
			System.err.println(dre);
			dre.printStackTrace();
		}

		/* AFAIK WE DONT NEED THIS
		if (update_credited_job) {
			CreditedJob creditedJob = new CreditedJob(result.user_id, result.workunit_id);
			creditedJob.insert();
		}
		*/
	}

	public void isInconclusive(Workunit workunit, Result result) {
		numberInconclusive++;
		System.out.println("inconclusive result: " + result);
		result.setValidateState(Result.VALIDATE_STATE_INCONCLUSIVE);
	}

	public void isInvalid(Workunit workunit, Result result) {
		numberInvalidated++;

		try {
			Host host = new Host(result.getHostId());
			if (workunit.getMinQuorum() > 1) host.updateErrorRateInvalid();
			host.commit();

			System.err.println("invalid result: " + result.toString());
		} catch (DatabaseCommitException dce) {
			System.err.println("error assigning credit to host: " + result.getHostId());
			System.err.println(dce);
			dce.printStackTrace();
		} catch (DatabaseRetrieveException dre) {
			System.err.println("error assigning credit to host: " + result.getHostId());
			System.err.println(dre);
			dre.printStackTrace();
		}
	}

	public LinkedList<Workunit> getValidationWorkunits() {

		String query =	"SELECT " +
				"   wu.id, " +
				"   wu.name, " +
				"   wu.canonical_resultid, " +
				"   wu.canonical_credit, " +
				"   wu.min_quorum, " +
				"   wu.assimilate_state, " +
				"   wu.transition_time, " +
				"   wu.opaque, " +
				"   wu.batch, " +
				"   wu.target_nresults, " +
				"   wu.max_success_results, " +
				"   wu.error_mask, " +
				"   wu.rsc_fpops_est, " +
				"   wu.xml_doc, " +
				"   res.id, " +
				"   res.name, " +
				"   res.validate_state, " +
				"   res.server_state, " +
				"   res.outcome, " +
				"   res.claimed_credit, " +
				"   res.granted_credit, " +
				"   res.xml_doc_in, " +
				"   res.xml_doc_out, " +
				"   res.stderr_out, " +
				"   res.cpu_time, " +
				"   res.batch, " +
				"   res.opaque, " +
				"   res.exit_status, " +
				"   res.hostid, " +
				"   res.userid, " +
				"   res.teamid, " +
				"   res.sent_time, " + 
				"   res.received_time, " +
				"   res.appid " +
				"FROM " +
				"   workunit AS wu, result AS res where wu.id = res.workunitid " +
				"   and wu.appid = " + appid + " and wu.need_validate > 0 " +
				"   and wu.id % " + modulo + " = " + remainder + " " +
				"LIMIT " +
				"   " + limit;

		LinkedHashMap<Integer,Workunit> validationWorkunits = new LinkedHashMap<Integer,Workunit>();

		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);

//			ResultSetMetaData rsMetaData = rs.getMetaData();
//			int numberOfColumns = rsMetaData.getColumnCount();

//			System.out.print("row:");
//			for (int j = 1; j < numberOfColumns; j++) {
//				System.out.print(" " + rsMetaData.getColumnName(j));
//			}
//			System.out.println();

			Workunit workunit;
			while (rs.next()) {
				int workunitId = rs.getInt(1);
				workunit = validationWorkunits.get(workunitId);

				if (workunit == null) {
					workunit = new Workunit(rs, 1);
					validationWorkunits.put(workunitId, workunit);
				}

				workunit.addResult(new Result(rs, 15));
			}
			rs.close();
			statement.close();

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		return new LinkedList<Workunit>(validationWorkunits.values());
	}

	public void doAssimilatePass() {
		String query =	"SELECT id FROM workunit WHERE appid = " + appid + " and assimilate_state = " + Workunit.ASSIMILATE_READY + " and id % " + modulo + " = " + remainder + " LIMIT " + limit;

		int count = 0;
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);

			int workunitid;
			Statement updateStatement;
			while (rs.next()) {
				workunitid = rs.getInt(1);

				updateStatement = connection.createStatement();
				updateStatement.executeUpdate("UPDATE workunit SET assimilate_state = " + Workunit.ASSIMILATE_DONE + ", transition_time = " + (System.currentTimeMillis()/1000) + " WHERE id = " + workunitid);
				updateStatement.close();

				count++;
			}
			rs.close();
			statement.close();


		} catch (SQLException ex) {
			System.err.println("AssimlatePass Exception: ");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		System.out.println("Assimilated " + count + " workunits.");
	}
}
