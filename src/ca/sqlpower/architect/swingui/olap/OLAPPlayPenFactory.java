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

package ca.sqlpower.architect.swingui.olap;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;

public class OLAPPlayPenFactory {

    public static PlayPen createPlayPen(ArchitectSwingSession session, OLAPEditSession oSession) {
        if (session == null) {
            throw new NullPointerException("Null session");
        }
        if (oSession == null) {
            throw new NullPointerException("Null oSession");
        }
        
        PlayPen pp = new PlayPen(session);
        
        pp.setPopupFactory(new ContextMenuFactory(session, oSession));
        pp.setupKeyboardActions();
        
        return pp;
    }

    /**
     * Sets up OLAP-specific keyboard actions on the playpen. This is done
     * separately because the OLAP session has to be finished creating the
     * actions before this will work, but it needs a playpen before the actions
     * can be created.
     * 
     * @param pp
     *            The playpen to register the keyboard actions on.
     * @param oSession
     *            The session pp belongs to, also the session that owns the
     *            actions to register.
     */
    static void setupOLAPKeyboardActions(PlayPen pp, OLAPEditSession oSession) {
        InputMap im = pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = pp.getActionMap();
        
        if (im == null) {
            throw new NullPointerException("Null input map");
        }
        if (am == null) {
            throw new NullPointerException("Null action map");
        }
        
        im.put((KeyStroke) oSession.getCreateCubeAction().getValue(Action.ACCELERATOR_KEY), "NEW CUBE"); //$NON-NLS-1$
        am.put("NEW CUBE", oSession.getCreateCubeAction()); //$NON-NLS-1$

        im.put((KeyStroke) oSession.getCreateMeasureAction().getValue(Action.ACCELERATOR_KEY), "NEW MEASURE"); //$NON-NLS-1$
        am.put("NEW MEASURE", oSession.getCreateMeasureAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateDimensionAction().getValue(Action.ACCELERATOR_KEY), "NEW DIMENSION"); //$NON-NLS-1$
        am.put("NEW DIMENSION", oSession.getCreateDimensionAction()); //$NON-NLS-1$
        
    }
}
