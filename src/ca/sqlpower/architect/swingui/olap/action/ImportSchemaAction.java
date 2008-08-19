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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianXMLReader;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.swingui.SPSUtils;

public class ImportSchemaAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(ImportSchemaAction.class);
    
    /**
     * The horizontal distance between gui components in the same section.
     */
    private static final int HORIZONTAL_OFFSET = 10;
    
    /**
     * The vertical distance that separates sections.
     */
    private static final int VERTICAL_OFFSET = 50;
    
    /**
     * The location of the first gui component. The x coordinate also determines
     * where each section begins horizontally.
     */
    private static final Point INITIAL_POINT = new Point(50, 50);
    
    public ImportSchemaAction(ArchitectSwingSession session) {
        super(session, "Import Schema...", "Imports an OLAP schema"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(session.getRecentMenu().getMostRecentFile());
        chooser.addChoosableFileFilter(SPSUtils.XML_FILE_FILTER);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            Schema loadedSchema = null;
            try {
                OLAPObject olapObj = MondrianXMLReader.importXML(f);
                if (olapObj instanceof Schema) {
                    loadedSchema = (Schema) olapObj;
                } else {
                    throw new IllegalStateException("File parse failed to return a schema object!");
                }
                
                OLAPSession osession = new OLAPSession(loadedSchema);
                osession.setDatabase(session.getTargetDatabase());
                session.getOLAPRootObject().addChild(osession);
                OLAPEditSession editSession = session.getOLAPEditSession(osession);
                
                addGUIComponents(editSession);
                
                JDialog d = editSession.getDialog();
                d.setLocationRelativeTo(session.getArchitectFrame());
                d.setVisible(true);
            } catch (Exception ex) {
                logger.error("Failed to parse " + f.getName() + ".");
                ASUtils.showExceptionDialog(session, "Could not read xml schema file.", ex);
            } 
            
        }
    }
    
    /**
     * Adds the gui components for the imported schema to the playpen. The
     * components will be placed in different "sections" according to their
     * types.
     * 
     * @param editSession
     *            Provides the imported schema and the playpen that the
     *            components should be added to.
     */
    private void addGUIComponents(OLAPEditSession editSession) {
        PlayPen pp = editSession.getOlapPlayPen();
        Schema schema = editSession.getOlapSession().getSchema();
        
        List<DimensionPane> dps = new ArrayList<DimensionPane>();
        List<CubePane> cps = new ArrayList<CubePane>();
        
        // stores the maximum height of the components in each section and used to
        // calculate the starting point of the next section.
        int dpMaxHeight = 0;
        int cpMaxHeight = 0;
        
        // creates the gui components for each child of the schema that we support.
        for (OLAPObject child : schema.getChildren()) {
            if (child instanceof Dimension) {
                Dimension d = (Dimension) child;
                DimensionPane dp = new DimensionPane(d, pp.getContentPane());
                dps.add(dp);
                dpMaxHeight = Math.max(dpMaxHeight, dp.getPreferredSize().height);
            } else if (child instanceof Cube) {
                Cube c = (Cube) child;
                CubePane cp = new CubePane(c, pp.getContentPane());
                cps.add(cp);
                cpMaxHeight = Math.max(cpMaxHeight, cp.getPreferredSize().height);
            } else {
                logger.warn("Unsupported gui component, skipping over: " + child);
            }
        }
        
        // add the components to their corresponding sections.
        
        Point p = new Point(INITIAL_POINT);
        for (DimensionPane dp : dps) {
            pp.addPlayPenComponent(dp, p);
            p.translate(dp.getPreferredSize().width + HORIZONTAL_OFFSET, 0);
        }
        
        p.setLocation(INITIAL_POINT.x, p.y + dpMaxHeight + VERTICAL_OFFSET);
        for (CubePane cp : cps) {
            pp.addPlayPenComponent(cp, p);
            p.translate(cp.getPreferredSize().width + HORIZONTAL_OFFSET, 0);
        }
    }
    
}
