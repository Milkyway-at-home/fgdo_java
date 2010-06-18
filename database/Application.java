package fgdo_java.database;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Application {

	private int id;
	private int create_time;
	private int min_version;
	private boolean deprecated;
	private String user_friendly_name;
	private String name;
	private int homogeneous_redundancy;
	private double weight;
	private boolean beta;
	private int target_nresults;

	public int getId() { return id; }
	public int getTargetNResults() { return target_nresults; }

	public boolean requiresReplication() { return target_nresults > 1; }

	public Application(String name, ResultSet resultSet, int offset) {
		this.name = name;
		try {
			id = resultSet.getInt(offset + 1);
			min_version = resultSet.getInt(offset + 2);
			deprecated = resultSet.getBoolean(offset + 3);
			user_friendly_name = resultSet.getString(offset + 4);
			homogeneous_redundancy = resultSet.getInt(offset + 5);
			weight = resultSet.getInt(offset + 6);
			beta = resultSet.getBoolean(offset + 7);
			target_nresults = resultSet.getInt(offset + 8);

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}


	public String toString() {
		return "[name: " + name +
			"] [id: " + id +
			"] [min_version: " + min_version +
			"] [deprecated: " + deprecated +
			"] [user_friendly_name: " + user_friendly_name +
			"] [homogeneous_redundancy: " + homogeneous_redundancy +
			"] [weight: " + weight +
			"] [beta: " + beta +
			"] [target_nresults: " + target_nresults + "]";
	}
}
