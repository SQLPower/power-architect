/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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
