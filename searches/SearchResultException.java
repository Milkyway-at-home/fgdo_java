package fgdo_java.searches;

public class SearchResultException extends Exception {
	private String description;
	private Exception cause;

	public SearchResultException(String description) {
		this.description = description;
	}

	public SearchResultException(String description, Exception cause) {
		this.description = description;
		this.cause = cause;
	}

	public String toString() {
		if (cause == null) {
			return "SearchResultException: " + description;
		} else {
			return "SearchResultException: " + description + ", caused by: " + cause;
		}
	}
}
