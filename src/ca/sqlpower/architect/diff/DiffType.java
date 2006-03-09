package ca.sqlpower.architect.diff;

public enum DiffType {
	LEFTONLY,
	MODIFIED,
	SAME,		// Some implementations may not use this.
	RIGHTONLY,
	KEY_CHANGED;	// primary key changed
}
