package ca.sqlpower.architect.diff;

import ca.sqlpower.architect.ArchitectException;

/**
 * This Exception is used to check in the StartCompareAction in the 
 * CompareDMPanel.  It is thrown when either the source or target
 * that is being compared has more than one table with the same name
 * which would lead to unreliable compare results.
 *
 */
public class ArchitectDiffException extends ArchitectException {
	public ArchitectDiffException(String message) {
		super(message);
	}
}
