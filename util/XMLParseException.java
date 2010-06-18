package fgdo_java.util;

public class XMLParseException extends Exception {

	private String description;
	private Exception cause;

	public XMLParseException(String description) {
		this.description = description;
	}

	public XMLParseException(String description, Exception cause) {
		this.description = description;
		this.cause = cause;
	}

	public String toString() {
		if (cause == null) {
			return "XMLParseException" + description;
		} else {
			return "XMLParseException" + description + ", caused by: " + cause;
		}
	}
	
}
