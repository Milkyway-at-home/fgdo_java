package fgdo_java.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;


public class DatabaseEntry {

	private StringBuffer commitString;

	public void appendModification(String columnName, String newValue) {
		//should see if we're already modifying this column

		if (commitString == null) commitString = new StringBuffer(columnName + " = " + newValue);
		else {
			commitString.append(", ");
			commitString.append(columnName);
			commitString.append(" = ");
			commitString.append(newValue);
		}
	}


	public void insert(String insertString) throws DatabaseCommitException {
		if (commitString == null) {
//			System.err.println("NO MODIFICATIONS TO: " + this);
			return;
		}

		String queryString = insertString + " SET " + commitString;
		try {
			Connection connection = BoincDatabase.getConnection();
			Statement statement = connection.createStatement();

			int result = statement.executeUpdate( queryString );
			statement.close();

//			System.out.println(queryString + " SQL result: " + result);
		} catch (SQLException e) {
			System.out.println("ERROR updating database: " + e);
			e.printStackTrace();

			throw new DatabaseCommitException("Could not update database with: " + queryString, e);
		}
	}

	public void commit(String updateString, String whereString) throws DatabaseCommitException {
		if (commitString == null) {
//			System.err.println("NO MODIFICATIONS TO: " + this);
			return;
		}

		String queryString = updateString + " SET " + commitString + " " + whereString;
		try {
			Connection connection = BoincDatabase.getConnection();
			Statement statement = connection.createStatement();

			int result = statement.executeUpdate( queryString );
			statement.close();

//			System.out.println(queryString + " SQL result: " + result);
//			System.out.println(queryString);
		} catch (SQLException e) {
			System.out.println("ERROR updating database: " + e);
			e.printStackTrace();

			throw new DatabaseCommitException("Could not update database with: " + queryString, e);
		}
	}

}
