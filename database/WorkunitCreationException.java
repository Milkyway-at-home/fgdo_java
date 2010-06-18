package fgdo_java.database;

public class WorkunitCreationException extends Exception {

	private Exception cause;
	private String description;


	public WorkunitCreationException(String description) {
		this.description = description;
	}

	public WorkunitCreationException(String description, Exception cause) {
		this.description = description;
		this.cause = cause;
	}

	public String toString() {
		if (cause == null) {
			return "WorkunitCreationException: " + description;
		} else {
			return "WorkunitCreationException: " + description + " : caused by : " + cause.toString();
		}
	}
}
