package ca.sqlpower.architect.sqlrunner;

/** The set of all valid modes. Short, lowercase names were used
 * for simple use in \mX where X is one of the names.
 */
enum OutputMode {
	/** Mode for Text */
	t("Text"),
	/** Mode for HTML output */
	h("HTML"),
	/** Mode for SQL output */
	s("SQL"),
	/** Mode for XML output */
	x("XML");
	String name;
	OutputMode(String n) {
		name = n;
	}
	public String toString() {
		return name;
	}
}