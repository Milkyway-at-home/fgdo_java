package fgdo_java.database;

public class DatabaseRetrieveException extends Exception {
	String description;
	Exception cause;

	public DatabaseRetrieveException(String description) {
		this.description = description;
	}

	public DatabaseRetrieveException(String description, Exception cause) {
		this.description = description;
		this.cause = cause;
	}

	public String toString() {
		if (cause == null) {
			return "DatabaseRetrieveException: " + description;
		} else {
			return "DatabaseRetrieveException: " + description + ": caused by: " + cause;
		}
	}
}
