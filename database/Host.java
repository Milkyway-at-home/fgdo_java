package fgdo_java.database;

import fgdo_java.daemons.CreditPolicy;
import fgdo_java.daemons.CreditException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class Host extends DatabaseEntry implements ExpAvgItem {
	public Host(int id) throws DatabaseRetrieveException {
		this.id = id;

		Connection connection = BoincDatabase.getConnection();
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT userid, error_rate, avg_turnaround, credit_per_cpu_sec, expavg_credit, expavg_time FROM host WHERE id = " + id);

			if (!rs.first()) {
				throw new DatabaseRetrieveException("Could not lookup user: " + id + ", empty ResultSet.");
			}       
			this.userid = rs.getInt(1);
			this.error_rate = rs.getDouble(2);
			this.avg_turnaround = rs.getDouble(3);
			this.credit_per_cpu_sec = rs.getDouble(4);
			this.expavg_credit = rs.getDouble(5);
			this.expavg_time = rs.getDouble(6);

			rs.close();
			statement.close();

		} catch (SQLException ex) {
			throw new DatabaseRetrieveException("Could not lookup user: " + id, ex);
		}       
	}


	private int id;
	private int userid;
	private double error_rate;
	private double avg_turnaround;
	private double credit_per_cpu_sec;
	private double expavg_credit;
	private double expavg_time;
	private String serialnum;

	public int getId() { return id; }
	public int getUserId() { return userid; }
	public double getErrorRate() { return error_rate; }
	public double getAvgTurnaround() { return avg_turnaround; }
	public double getCreditPerCPUSec() { return credit_per_cpu_sec; }
	public double getExpAvgCredit() { return expavg_credit; }
	public double getExpAvgTime() { return expavg_time; }
	public String getSerialnum() { return serialnum; }

	public void setErrorRate(double error_rate) {
		this.error_rate = error_rate;
		appendModification("error_rate", Double.toString(this.error_rate));
	}

	public void setAvgTurnaround(double avg_turnaround) {
		this.avg_turnaround = avg_turnaround;
		appendModification("avg_turnaround", Double.toString(this.avg_turnaround));
	}

	public void setCreditPerCPUSec(double credit_per_cpu_sec) {
		this.credit_per_cpu_sec = credit_per_cpu_sec;
		appendModification("credit_per_cpu_sec", Double.toString(this.credit_per_cpu_sec));
	}

	public void setExpAvgCredit(double expavg_credit) {
		this.expavg_credit = expavg_credit;
		appendModification("expavg_credit", Double.toString(this.expavg_credit));
	}

	public void setExpAvgTime(double expavg_time) {
		this.expavg_time = expavg_time;
		appendModification("expavg_time", Double.toString(this.expavg_time));
	}

	public void updateCreditPerCPUSec(double granted_credit, double cpu_time) throws CreditException {
		final double credit_average_const = 500;
		final double max_credit_per_cpu_sec = 0.07;

		double e = Math.tanh(granted_credit/credit_average_const);
		if (e <= 0.0 || cpu_time == 0.0 || granted_credit == 0.0) return;
		if (e > 1.0 || getCreditPerCPUSec() == 0.0) e = 1.0;

		double rate =  granted_credit/cpu_time;
		if (rate < 0.0) rate = 0.0;
		if (rate > CreditPolicy.max_credit_per_cpu_sec) {
			rate = CreditPolicy.max_credit_per_cpu_sec;
			throw new CreditException("host " + getId() + " claimed too much credit (" + granted_credit + ") in too little CPU time (" + cpu_time + ")");
		}

		setCreditPerCPUSec(e * rate + (1.0 - e) * getCreditPerCPUSec());
	}

	public void updateCredit(double granted_credit, double start_time, double cpu_time) throws CreditException, DatabaseRetrieveException, DatabaseCommitException {
		CreditPolicy.updateAverages(this, start_time, granted_credit);
		updateCreditPerCPUSec(granted_credit, cpu_time);
		appendModification("total_credit", "total_credit + " + granted_credit);

		User user = new User(getUserId());
		CreditPolicy.updateAverages(user, start_time, granted_credit);
		user.appendModification("total_credit", "total_credit + " + granted_credit);
		user.commit();

		if (user.getTeamId() == 0) return;

		Team team = new Team(user.getTeamId());
		CreditPolicy.updateAverages(team, start_time, granted_credit);
		team.appendModification("total_credit", "total_credit + " + granted_credit);
		team.commit();
	}

	public void updateAverageTurnaround(double new_avg) {
		if (getAvgTurnaround() != 0) {
			new_avg = (0.7 * getAvgTurnaround()) + (0.3 * new_avg);
		}

		setAvgTurnaround(new_avg);
	}

	public void updateErrorRateValid() {
		double new_rate = getErrorRate() * 0.95;
		if (new_rate <= 0) new_rate = 0.1;

		setErrorRate(new_rate);
	}

	public void updateErrorRateInvalid() {
		double new_rate = getErrorRate() + 0.1;
		if (new_rate > 1) new_rate = 1;

		setErrorRate(new_rate);
	}

	public void commit() throws DatabaseCommitException {
		super.commit("UPDATE host", "WHERE id = " + getId());
	}
}
