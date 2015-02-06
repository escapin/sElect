package selectvoting.system.core;

/**
 * Error thrown if the input data is ill-formed.
 */
public class MalformedData extends Exception {
	public String description;
	public MalformedData(String description) {
		this.description = description;
	}
	public String toString() {
		return "Final Server Error: " + description;
	}
}