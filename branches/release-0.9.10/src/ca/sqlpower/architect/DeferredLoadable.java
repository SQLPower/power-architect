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
package ca.sqlpower.architect;

/**
 * The DeferredLoadable interface allows a heterogeneous collection of
 * objects to be managed by a single piece of code that knows when
 * they need to become fully loaded.  This is useful for classes that
 * have costly initialization code, such as database connections or
 * queries.
 */
public interface DeferredLoadable {

	/**
	 * A call to this method should cause implementing classes to
	 * perform all their costly startup procedures.  It is allowed to
	 * be called many times, so you should be sure to only preform the
	 * costly startup operation the first time.
	 */
	public void loadNow() throws ArchitectException;

	/**
	 * This method tells callers whether or not the loadNow() will do
	 * anything costly.
	 *
	 * @return true if the load operation has already been completed;
	 * false if the load operation will be costly.
	 */
	public boolean isLoaded() throws ArchitectException;
}
