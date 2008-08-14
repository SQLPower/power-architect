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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.olap.action.EditCubeAction;
import ca.sqlpower.architect.swingui.olap.action.EditDimensionAction;
import ca.sqlpower.architect.swingui.olap.action.EditVirtualCubeAction;
import ca.sqlpower.swingui.JTreeCollapseAllAction;
import ca.sqlpower.swingui.JTreeExpandAllAction;

public class OLAPTree extends JTree{

    private static final Logger logger = Logger.getLogger(OLAPTree.class);
    
    private ArchitectSwingSession session;
    private OLAPEditSession oSession;
    
    private JPopupMenu popup;
    private final Schema schema;
    private JTreeCollapseAllAction collapseAllAction;
    private JTreeExpandAllAction expandAllAction;
    
    public OLAPTree(ArchitectSwingSession session, OLAPEditSession oSession, Schema schema) {
        this.schema = schema;
        this.oSession = oSession;
        this.session = session;
        setModel(new OLAPTreeModel(schema));
        addMouseListener(new PopupListener());
        collapseAllAction = new JTreeCollapseAllAction(this, "Collaspe All");
        expandAllAction = new JTreeExpandAllAction(this, "Expand All");
         
    }
    
 // ----------------- popup menu stuff ----------------

    /**
     * A simple mouse listener that activates the OLAPTree's popup menu
     * when the user right-clicks (or some other platform-specific action).
     *
     * @author The Swing Tutorial (Sun Microsystems, Inc.)
     */
    private class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e,true);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e,false);
        }

        private void maybeShowPopup(MouseEvent e, boolean isPress) {

            TreePath p = getPathForLocation(e.getX(), e.getY());
            if (e.isPopupTrigger()) {
                logger.debug("TreePath is: " + p); //$NON-NLS-1$

                if (p != null) {
                    logger.debug("selected node object type is: " + p.getLastPathComponent().getClass().getName()); //$NON-NLS-1$
                }

                // if the item is already selected, don't touch the selection model
                if (!isPathSelected(p)) {
                    setSelectionPath(p);
                }
                popup = refreshMenu(p);
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            } else {
                if ( p == null && !isPress && e.getButton() == MouseEvent.BUTTON1 )
                    setSelectionPath(null);
            }
        }
    }

    protected JPopupMenu refreshMenu(TreePath p) {
        logger.debug("refreshMenu is being called."); //$NON-NLS-1$
        JPopupMenu newMenu = new JPopupMenu();
        if (p != null) {
            OLAPObject obj = (OLAPObject)p.getLastPathComponent();
            if (obj instanceof Schema) {
                newMenu.add(oSession.getCreateVirtualCubeAction());
                newMenu.add(oSession.getCreateCubeAction());
                newMenu.add(oSession.getCreateDimensionAction());
                newMenu.add(oSession.getExportSchemaAction());
            } else if (obj instanceof Dimension) {
                newMenu.add(new EditDimensionAction(session, (Dimension)obj, oSession.getOlapPlayPen() ));
            } else if (obj instanceof Cube) {
                newMenu.add(new EditCubeAction(session, (Cube)obj, oSession.getOlapPlayPen()));
            } else if (obj instanceof VirtualCube) {
                newMenu.add(new EditVirtualCubeAction(session, (VirtualCube)obj, oSession.getOlapPlayPen()));
                newMenu.add(oSession.getCreateCubeAction());
                newMenu.add(oSession.getCreateDimensionAction());
                newMenu.add(oSession.getCreateMeasureAction());
                newMenu.add(oSession.getCreateVirtualCubeAction());
            } else if (obj instanceof Measure) {
            }
            newMenu.addSeparator();
            
            newMenu.add(collapseAllAction);
            newMenu.add(expandAllAction);
        } else {

        }
        return newMenu;
    }
    
}
