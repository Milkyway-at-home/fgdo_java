package fgdo_java.database;

import java.util.HashMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;


public class BoincDatabase {
	private static Connection connection = null;
	private static Application application = null;

	public static Connection getConnection() {
		if (connection == null) {
            String databaseName = System.getProperty("databaseName");
            String databaseUser = System.getProperty("databaseUser");
            String databasePassword = System.getProperty("databasePassword");

            if (databaseName == null || databaseUser == null || databasePassword == null) {
                System.err.println("Could not get database connection, need to specify -DdatabaseName='database name' -DdatabaseUser='database username' -DdatabasePassword='database password'");
                System.err.println();
                System.exit(0);
            }

			try {
				// force loading of the jdbc.odbc Driver
				try {
					Class.forName("org.gjt.mm.mysql.Driver").newInstance();
				} catch (Exception e) {
					System.err.println("Exception: " + e);
				}

				connection = DriverManager.getConnection(databaseName, databaseUser, databasePassword);

			} catch (SQLException ex) {
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
				ex.printStackTrace();
			}
		}
		return connection;
	}

	public static int getLastInsertId() throws DatabaseRetrieveException {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select LAST_INSERT_ID()");
			if (resultSet.first()) {
				int result = resultSet.getInt(1);
				resultSet.close();
				statement.close();

				return result;
			} else {
				throw new DatabaseRetrieveException("Could not get last insert id - no result set.");
			}

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			throw new DatabaseRetrieveException("Could not get last insert id.", ex);
		}
	}

	public static int getCount(String table, String query) throws DatabaseRetrieveException {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + table + " WHERE " + query);
			if (resultSet.first()) {
				int result = resultSet.getInt(1);
				resultSet.close();
				statement.close();

				return result;
			} else {
				throw new DatabaseRetrieveException("Could not get count FROM " + table + " WHERE " + query); 
			}

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			throw new DatabaseRetrieveException("SQLException: Could not get count FROM " + table + " WHERE " + query, ex); 
		}
	}

	public static Application getApplication() throws DatabaseRetrieveException {
		if (application != null) return application;

		String applicationName = System.getProperty("application");
		if (applicationName == null) {
			System.err.println("Unknown application: " + applicationName);
			System.err.println("Must specify application name with system property:  java -Dapplication=<application_name> ...");
			System.exit(0);
		}

		getConnection();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT id, min_version, deprecated, user_friendly_name, homogeneous_redundancy, weight, beta, target_nresults FROM app WHERE name = '" + applicationName + "'");
			if (resultSet.first()) {
				application = new Application(applicationName, resultSet, 0);
				resultSet.close();
				statement.close();

				return application;
			} else {
				throw new DatabaseRetrieveException("Could not get application - no result set for application: " + applicationName);
			}

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			throw new DatabaseRetrieveException("Could not get application - no result set.", ex);
		}
	}

	public static String getWorkunitTemplate(int id) throws DatabaseRetrieveException {

		getConnection();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT xml_doc FROM workunit WHERE id = " + id);

			if (resultSet.first()) {
				String result = resultSet.getString(1);
				resultSet.close();
				statement.close();

				return result;
			} else {
				throw new DatabaseRetrieveException("Could not get workunit -- not result set for id " + id);
			}

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			throw new DatabaseRetrieveException("Could not get application - no result set.", ex);
		}
	}
}
