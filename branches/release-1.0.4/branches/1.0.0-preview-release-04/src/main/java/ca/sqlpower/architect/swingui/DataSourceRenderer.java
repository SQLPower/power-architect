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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ca.sqlpower.sql.SPDataSource;

public final class DataSourceRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		SPDataSource ds = (SPDataSource) value;
		String label;
		if (ds == null) {
			label = Messages.getString("DataSourceRenderer.chooseConnection"); //$NON-NLS-1$
		} else {
			label = ds.getName();
		}
		return super.getListCellRendererComponent(list, label, index,
				isSelected, cellHasFocus);
	}
}