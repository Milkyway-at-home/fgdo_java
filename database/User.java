package fgdo_java.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class User extends DatabaseEntry implements ExpAvgItem {

	public User(int id) throws DatabaseRetrieveException {
		this.id = id;

		Connection connection = BoincDatabase.getConnection();
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT teamid, expavg_credit, expavg_time FROM user WHERE id = " + id);

			if (!rs.first()) {
				throw new DatabaseRetrieveException("Could not lookup user: " + id + ", empty ResultSet.");
			}
			this.teamid = rs.getInt(1);
			this.expavg_credit = rs.getDouble(2);
			this.expavg_time = rs.getDouble(3);

			rs.close();
			statement.close();

		} catch (SQLException ex) {
			throw new DatabaseRetrieveException("Could not lookup user: " + id, ex);
		}
	}

	private int id;
	private int teamid;
	private double expavg_credit;
	private double expavg_time;

	public int getId() { return id; }
	public int getTeamId() { return teamid; }

	public double getExpAvgCredit() { return expavg_credit; }
	public double getExpAvgTime() { return expavg_time; }

	public void setExpAvgCredit(double expavg_credit) {
		this.expavg_credit = expavg_credit;
		appendModification("expavg_credit", Double.toString(this.expavg_credit));
	}

	public void setExpAvgTime(double expavg_time) {
		this.expavg_time = expavg_time;
		appendModification("expavg_time", Double.toString(this.expavg_time));
	}

	public void commit() throws DatabaseCommitException {
		super.commit("UPDATE user", "WHERE id = " + getId());
	}
}
