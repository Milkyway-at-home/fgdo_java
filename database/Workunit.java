package fgdo_java.database;

import fgdo_java.util.DirectoryTree;
import fgdo_java.util.XMLTemplate;
import fgdo_java.util.XMLParseException;

import java.io.File;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Workunit extends DatabaseEntry {

	public Workunit() {
	}

	public static final int ASSIMILATE_INIT = 0, ASSIMILATE_READY = 1, ASSIMILATE_DONE = 2;
	public static final String[] assimilate_states = { "INIT", "READY", "DONE" };
	public static final int FILE_DELETE_INIT = 0, FILE_DELETE_READY = 1, FILE_DELETE_DONE = 2, FILE_DELETE_ERROR = 3;
	public static final String[] file_delete_states = { "INIT", "READY", "DONE", "ERROR" };

	public static final int WU_ERROR_COULDNT_SEND_RESULT = 1, WU_ERROR_TOO_MANY_ERROR_RESULTS = 2, WU_ERROR_TOO_MANY_SUCCESS_RESULTS = 4, WU_ERROR_TOO_MANY_TOTAL_RESULTS = 8, WU_ERROR_CANCELLED = 16, WU_ERROR_NO_CANONICAL_RESULT = 32;

	/**
	 *	Fields needed by the validator
	 */
	private int id;
	private int appid;
	private String name;
	private double canonical_credit;
	private int canonical_resultid;
	private int min_quorum;
	private int assimilate_state;
	private int file_delete_state;
	private long transition_time;
	private double opaque;
	private int batch;
	private int target_nresults;
	private int max_success_results;
	private int error_mask;
	private double rsc_fpops_est;

	/**
	 *	Fields needed for work creation
	 */
	private double rsc_fpops_bound;
	private double rsc_memory_bound;
	private double rsc_disk_bound;
	private double rsc_bandwidth_bound;
	private long create_time;
	private int delay_bound;
	private int need_validate;
	private int max_error_results;
	private int max_total_results;
	private String result_template_filename;
	private String workunit_template;

	public void setName(String name) {
		this.name = name;
		appendModification("name", "'" + this.name + "'");
	}

	public void setAppid(int appid) {
		this.appid = appid;
		appendModification("appid", Integer.toString(this.appid));
	}

	public void setCanonicalCredit(double canonical_credit) {
		this.canonical_credit = canonical_credit;
		appendModification("canonical_credit", Double.toString(this.canonical_credit));
	}

	public void setCanonicalResultId(int canonical_resultid) {
		this.canonical_resultid = canonical_resultid;
		appendModification("canonical_resultid", Integer.toString(this.canonical_resultid));
	}
	public void setNeedsValidate(int need_validate) {
		this.need_validate = need_validate;
		appendModification("need_validate", Integer.toString(this.need_validate));
	}

	public void updateErrorMask(int error_mask) {
		this.error_mask |= error_mask;
		appendModification("error_mask", Integer.toString(this.error_mask));
	}

	public void setTransitionTime(long transition_time) {
		this.transition_time = transition_time;
		appendModification("transition_time", Long.toString(this.transition_time));
	}

	public void setAssimilateState(int assimilate_state) {
		this.assimilate_state = assimilate_state;
		appendModification("assimilate_state", Integer.toString(this.assimilate_state));
	}

	public void setTargetNResults(int target_nresults) {
		this.target_nresults = target_nresults;
		appendModification("target_nresults", Integer.toString(this.target_nresults));
	}

	public void setCreateTime(long create_time) {
		this.create_time = create_time;
		appendModification("create_time", Long.toString(this.create_time));
	}

	public void setResultTemplateFilename(String result_template_filename) {
		this.result_template_filename = result_template_filename;
		appendModification("result_template_file", "'" + this.result_template_filename + "'");
	}

	public void setRSCFpopsEst(double rsc_fpops_est) {
		this.rsc_fpops_est = rsc_fpops_est;
		appendModification("rsc_fpops_est", Double.toString(this.rsc_fpops_est));
	}

	public void setRSCFpopsBound(double rsc_fpops_bound) {
		this.rsc_fpops_bound = rsc_fpops_bound;
		appendModification("rsc_fpops_bound", Double.toString(this.rsc_fpops_bound));
	}

	public void setRSCMemoryBound(double rsc_memory_bound) {
		this.rsc_memory_bound = rsc_memory_bound;
		appendModification("rsc_memory_bound", Double.toString(this.rsc_memory_bound));
	}

	public void setRSCBandwidthBound(double rsc_bandwidth_bound) {
		this.rsc_bandwidth_bound = rsc_bandwidth_bound;
		appendModification("rsc_bandwidth_bound", Double.toString(this.rsc_bandwidth_bound));
	}

	public void setRSCDiskBound(double rsc_disk_bound) {
		this.rsc_disk_bound = rsc_disk_bound;
		appendModification("rsc_disk_bound", Double.toString(this.rsc_disk_bound));
	}

	public void setBatch(int batch) {
		this.batch = batch;
		appendModification("batch", Integer.toString(this.batch));
	}

	public void setDelayBound(int delay_bound) {
		this.delay_bound = delay_bound;
		appendModification("delay_bound", Integer.toString(this.delay_bound));
	}

	public void setMinQuorum(int min_quorum) {
		this.min_quorum = min_quorum;
		appendModification("min_quorum", Integer.toString(this.min_quorum));
	}

	public void setMaxErrorResults(int max_error_results) {
		this.max_error_results = max_error_results;
		appendModification("max_error_results", Integer.toString(this.max_error_results));
	}

	public void setMaxTotalResults(int max_total_results) {
		this.max_total_results = max_total_results;
		appendModification("max_total_results", Integer.toString(this.max_total_results));
	}

	public void setMaxSuccessResults(int max_success_results) {
		this.max_success_results = max_success_results;
		appendModification("max_success_results", Integer.toString(this.max_success_results));
	}

	public void setWorkunitTemplate(String workunit_template) {
		this.workunit_template = workunit_template;
		appendModification("xml_doc", "'" + this.workunit_template + "'");
	}


	public int getId() { return id; }
	public int getMinQuorum() { return min_quorum; }
	public int getMaxSuccessResults() { return max_success_results; }
	public int getTargetNResults() { return target_nresults; }
	public long getTransitionTime() { return transition_time; }
	public int getMaxErrorResults() { return max_error_results; }
	public double getRSCFpopsEst() { return rsc_fpops_est; }
	public double getRSCFpopsBound() { return rsc_fpops_bound; }
	public double getRSCDiskBound() { return rsc_disk_bound; }
	public int getMaxTotalResults() { return max_total_results; }
	public String getWorkunitTemplate() { return workunit_template; }
	public double getCanonicalCredit() { return canonical_credit; }
	public int getCanonicalResultid() { return canonical_resultid; }

	public boolean isUnreplicated() {
		return target_nresults == 1;
	}

	public HashMap<Integer,Result> results = new HashMap<Integer,Result>();

	public void addResult(Result result) {
		results.put(result.getId(), result);
	}

	public Result getCanonicalResult() {
		if (canonical_resultid > 0) {
			return results.get(canonical_resultid);
		} else {
			return null;
		}
	}

	public Collection<Result> getResults() {
		return results.values();
	}

	public Workunit(ResultSet resultSet, int offset) {
		try {
			id = resultSet.getInt(offset + 0);
			name = resultSet.getString(offset + 1);
			canonical_resultid = resultSet.getInt(offset + 2);
			canonical_credit = resultSet.getDouble(offset + 3);
			min_quorum = resultSet.getInt(offset + 4);
			assimilate_state = resultSet.getInt(offset + 5);
			transition_time = resultSet.getLong(offset + 6);
			opaque = resultSet.getDouble(offset + 7);
			batch = resultSet.getInt(offset + 8);
			target_nresults = resultSet.getInt(offset + 9);
			max_success_results = resultSet.getInt(offset + 10);
			error_mask = resultSet.getInt(offset + 11);
			rsc_fpops_est = resultSet.getDouble(offset + 12);
			workunit_template = resultSet.getString(offset + 13);
		} catch (SQLException ex) {
			System.out.println("sqlexception: " + ex.getMessage());
			System.out.println("sqlstate: " + ex.getSQLState());
			System.out.println("vendorerror: " + ex.getErrorCode());
		}
	}

	public void commit() throws DatabaseCommitException {
		super.commit("UPDATE workunit", "WHERE id = " + id);
	}

	public void create(String result_template_filename, String workunit_template, String[] input_files, LinkedList<String> extraXML, String command_line, String additional_xml) throws WorkunitCreationException {
		setCreateTime(System.currentTimeMillis() / 1000);

		File result_template_file = new File(DirectoryTree.getBaseDirectory() + result_template_filename);
		if (!result_template_file.exists()) throw new WorkunitCreationException("result_template does not exist: " + result_template_filename);
		setResultTemplateFilename(result_template_filename);

		try {
			setWorkunitTemplate( XMLTemplate.processWorkunitTemplate(this, workunit_template, input_files, extraXML, command_line, additional_xml) );
		} catch (XMLParseException xe) {
			throw new WorkunitCreationException("Could not parse workunit XML template", xe);
		}

		if (getRSCFpopsEst() == 0) throw new WorkunitCreationException("rsc_fpops_est not given.");
		if (getRSCFpopsBound() == 0) throw new WorkunitCreationException("rsc_fpops_bound not given.");
		if (getRSCDiskBound() == 0) throw new WorkunitCreationException("rsc_disc_bound not given.");
		if (getTargetNResults() == 0) throw new WorkunitCreationException("target_nresults not given.");
		if (getMaxErrorResults() == 0) throw new WorkunitCreationException("max_error_results not given.");
		if (getMaxTotalResults() == 0) throw new WorkunitCreationException("max_total_results not given.");
		if (getMaxSuccessResults() == 0) throw new WorkunitCreationException("max_success_results not given.");
		if (getMaxSuccessResults() > getMaxTotalResults()) throw new WorkunitCreationException("max_success_results > max_total_results.");
		if (getMaxErrorResults() > getMaxTotalResults()) throw new WorkunitCreationException("max_error_results > max_total_results.");
		if (getTargetNResults() > getMaxSuccessResults()) throw new WorkunitCreationException("target_nresults > max_success_results.");

		//maybe need to check if wu.name is in assigned_wu string
		setTransitionTime(System.currentTimeMillis() / 1000);

		try {
			super.insert("INSERT INTO workunit");	
		} catch (DatabaseCommitException dce) {
			throw new WorkunitCreationException("could not commit changes to database.", dce);
		}
	
		if (id <= 0) {
			try {
				id = BoincDatabase.getLastInsertId();
			} catch (DatabaseRetrieveException dre) {
				throw new WorkunitCreationException("could not retrieve workunit insert id from database.", dre);
			}
		}
	}


	public String toString() {
		return "[id: " + id +
			"] [name: " + name +
			"] [canonical_resultid: " + canonical_resultid +
			"] [canonical_credit: " + canonical_credit +
			"] [min_quorum: " + min_quorum +
			"] [assimilate_state: " + assimilate_states[assimilate_state] +
			"] [transition_time: " + transition_time +
			"] [target_nresults: " + target_nresults +
			"] [max_success_results: " + max_success_results +
			"] [rsc_fpops_est: " + rsc_fpops_est + "]";
	}
}
