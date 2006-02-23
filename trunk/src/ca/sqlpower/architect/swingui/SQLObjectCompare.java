/**
 * 
 */
package ca.sqlpower.architect.swingui;

import java.util.Comparator;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;

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