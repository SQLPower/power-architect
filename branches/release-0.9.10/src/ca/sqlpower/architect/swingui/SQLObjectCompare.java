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
/**
 * 
 */
package ca.sqlpower.architect.swingui;

import java.util.Comparator;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectCompare implements Comparator {
	
	public int compare(Object t1, Object t2)
	{
		if (t1 instanceof SQLObject)
		{
			if (t2 instanceof SQLObject)
			{
				if (t1 != null && t2 != null)
				{
					return (((SQLObject)t1).getName()).compareTo(((SQLObject)t2).getName());
				}
				else
				{
					// if t1 is null t2 is greater
					if (t1 == null)
					{
						return -1;
					}
					else
					{
						// if t2 is null t1 is greater
						return 1;
					}
				}
			
			}
			else
			{
				return 1;
			}
		}
		else
		{
			return -1;
		}
	}
		
	
}