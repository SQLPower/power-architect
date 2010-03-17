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
package ca.sqlpower.architect.swingui;

import java.awt.event.MouseEvent;
import java.util.List;

import ca.sqlpower.object.SPObject;

public class PlayPenComponentImpl extends PlayPenComponent {


	protected PlayPenComponentImpl() {
		super("PlayPenComponent");
	}

	public void setSelected(boolean v, int multiSelectType) {
	}

	public boolean isSelected() {
		return false;
	}
	
	@Override
	public void handleMouseEvent(MouseEvent evt) {
	    
	}

	@Override
	public Object getModel() {
		return null;
	}
	
	@Override
	public String getModelName() {
	    return null;
	}

    public List<? extends SPObject> getDependencies() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeDependency(SPObject dependency) {
        // TODO Auto-generated method stub
        
    }
}
