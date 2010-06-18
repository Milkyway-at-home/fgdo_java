package fgdo_java.daemons;

public class CreditException extends Exception {
	private String description;

	public CreditException(String description) {
		this.description = description;
	}

	public String toString() {
		return "CreditException: " + description;
	}
}
