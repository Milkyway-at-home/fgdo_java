package fgdo_java.database;

public class DatabaseCommitException extends Exception {
	String description;
	Exception cause;

	public DatabaseCommitException(String description) {
		this.description = description;
	}

	public DatabaseCommitException(String description, Exception cause) {
		this.description = description;
		this.cause = cause;
	}

	public String toString() {
		if (cause == null) {
			return "DatabaseCommitException: " + description;
		} else {
			return "DatabaseCommitException: " + description + ": caused by: " + cause;
		}
	}
}
