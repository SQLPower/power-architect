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

package ca.sqlpower.architect.swingui.olap.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.PlayPen.CancelableListener;
import ca.sqlpower.architect.swingui.PlayPen.CursorManager;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.UsageComponent;

/**
 * Creates a dimension usage using similar techniques from CreateRelationshipAction
 *
 */
public class CreateDimensionUsageAction extends AbstractArchitectAction 
                implements ActionListener, SelectionListener, CancelableListener {
    
    private static final Logger logger = Logger.getLogger(CreateDimensionUsageAction.class);
    
    /**
     * CursorManager, which controls the type of the cursor
     */
    private CursorManager cursorManager;
    
    /**
     * DimensionPane containing the public dimension which we
     * will extract
     */
    private DimensionPane dp;
    
    /**
     * CubePane which will contain the dimensionUsage
     */
    private CubePane cp;

    /**
     * This property is true when we are actively creating a DimensionUsage.
     * Similar to CreateRelationshipAction
     */
    protected boolean active;
    
    public CreateDimensionUsageAction(ArchitectSwingSession session, PlayPen pp) {
        super(session, pp, "Create Dimension Usage", "Create Dimension Usage", (String) null);
        cursorManager = playpen.getCursorManager();
    }

    public void actionPerformed(ActionEvent e) {
        // At start of creation process, register to selection and cancelable listener
        if (!active) {
            logger.debug(">>>>>> Adding to selectable listeners, preparing to start the creation.");
            playpen.fireCancel();
            playpen.selectNone();
            playpen.addSelectionListener(this);
            playpen.addCancelableListener(this);
            cursorManager.placeModeStarted();
            dp = null;
            cp = null;
            active = true;
        }
    }

    public void itemDeselected(SelectionEvent e) {
        logger.debug(">>>>>> Item deselected (ignoring).");
    }

    public void itemSelected(SelectionEvent e) {
        logger.debug(">>>>>> Item selected.");
        Selectable s = e.getSelectableSource();
        if (!s.isSelected()) {
            return;
        }

        if (s instanceof DimensionPane && dp == null) {
            logger.debug(">>>>>> First selection");
            dp = (DimensionPane) s;
        } else if (s instanceof CubePane && cp == null) {
            logger.debug(">>>>>> Second selection");
            cp = (CubePane) s;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("The user clicked on an irrelevant component: "+s); //$NON-NLS-1$
                logger.debug(">>>>>> Wrong component selected, supposed to discard all data.");
            }
            reset();
        }
        
        if (cp != null && dp != null) {
            try {
                DimensionUsage du = new DimensionUsage();
                du.setName("New DimensionUsage");
                du.setSource(dp.getName());
                cp.getModel().addChild(du);
                UsageComponent uc = new UsageComponent(playpen.getContentPane(), du, dp, cp);
                playpen.getContentPane().add(uc, playpen.getContentPane().getComponentCount());
            } finally {
                reset();
            }
        }
    }

    public void cancel() {
        reset();
    }

    /**
     * Clean up and prepare itself for another dimensionusage creation
     */
    private void reset() {
        logger.debug(">>>>>> Reseting");
        dp = null;
        cp = null;
        cursorManager.placeModeFinished();
        active = false;
        playpen.removeSelectionListener(this);
        playpen.removeCancelableListener(this);
    }
}
