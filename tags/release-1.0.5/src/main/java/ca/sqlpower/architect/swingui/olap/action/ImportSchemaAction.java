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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianXMLReader;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.swingui.olap.SchemaEditPanel;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
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
    
    public ImportSchemaAction(ArchitectFrame frame) {
        super(frame, "Import Schema...", "Imports an OLAP schema");
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(getSession().getRecentMenu().getMostRecentFile());
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
                
                getSession().getWorkspace().begin("Importing OLAP schema");
                try {
                                            
                    final OLAPSession osession = new OLAPSession(loadedSchema);
                    osession.setDatabase(getSession().getTargetDatabase());
                    getSession().getOLAPRootObject().addChild(osession);
                    OLAPEditSession editSession = getSession().getOLAPEditSession(osession);

                    addGUIComponents(editSession);

                    final JFrame frame = editSession.getFrame();
                    frame.setLocationRelativeTo(getSession().getArchitectFrame());
                    frame.setVisible(true);
                    
                    getSession().getWorkspace().commit();
                
                    final SchemaEditPanel schemaEditPanel = new SchemaEditPanel(getSession(), loadedSchema);

                    Callable<Boolean> okCall = new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                                return schemaEditPanel.applyChanges();
                        }
                    };

                    Callable<Boolean> cancelCall = new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            frame.dispose();
                            getSession().getOLAPRootObject().removeOLAPSession(osession);
                            return true;
                        }
                    };

                    JDialog schemaEditDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                            schemaEditPanel,
                            frame,
                            "New Schema Properties",
                            DataEntryPanelBuilder.OK_BUTTON_LABEL,
                            okCall,
                            cancelCall);
                    schemaEditDialog.setLocationRelativeTo(frame);
                    schemaEditDialog.setVisible(true);                                        
                    
                } catch (Exception ex) {
                    getSession().getWorkspace().rollback("Failed to get a list of databases: " + ex.toString());
                    ASUtils.showExceptionDialogNoReport(
                            getSession().getArchitectFrame(),
                            "Failed to get list of databases.",
                            ex);
                } catch (Throwable ex) {
                    getSession().getWorkspace().rollback("Failed to import schema: " + ex.toString());
                    throw new RuntimeException(ex);
                }
                
            } catch (Exception ex) {
                logger.error("Failed to parse " + f.getName() + ".");
                ASUtils.showExceptionDialog(getSession(), "Could not read xml schema file.", ex);
            } finally {
                getSession().getRecentMenu().putRecentFileName(f.getAbsolutePath());
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
        
        Map<String, DimensionPane> dimPaneMap = new HashMap<String, DimensionPane>();
        Map<String, CubePane> cubePaneMap = new HashMap<String, CubePane>();
        Map<String, VirtualCubePane> vCubePaneMap = new HashMap<String, VirtualCubePane>();
        
        // stores the maximum height of the container panes in each section and used to
        // calculate the starting point of the next section.
        int dimMaxHeight = 0;
        int cubeMaxHeight = 0;
        int vCubeMaxHeight = 0;
        
        // creates the gui components for container panes.
        for (OLAPObject child : schema.getChildren(OLAPObject.class)) {
            if (child instanceof Dimension) {
                Dimension dim = (Dimension) child;
                DimensionPane dimPane = new DimensionPane(dim, pp.getContentPane());
                dimPaneMap.put(OLAPUtil.nameFor(dim), dimPane);
                dimMaxHeight = Math.max(dimMaxHeight, dimPane.getPreferredSize().height);
            } else if (child instanceof Cube) {
                Cube cube = (Cube) child;
                CubePane cubePane = new CubePane(cube, pp.getContentPane());
                cubePaneMap.put(OLAPUtil.nameFor(cube), cubePane);
                cubeMaxHeight = Math.max(cubeMaxHeight, cubePane.getPreferredSize().height);
            } else if (child instanceof VirtualCube) {
                VirtualCube vCube = (VirtualCube) child;
                VirtualCubePane vCubePane = new VirtualCubePane(vCube, pp.getContentPane());
                vCubePaneMap.put(OLAPUtil.nameFor(vCube), vCubePane);
                vCubeMaxHeight = Math.max(vCubeMaxHeight, vCubePane.getPreferredSize().height);
            } else {
                logger.warn("Unsupported gui component, skipping over: " + child);
            }
        }
        
        // add the container panes to their corresponding sections.
        Point p = new Point(INITIAL_POINT);
        for (DimensionPane dimPane : dimPaneMap.values()) {
            pp.addPlayPenComponent(dimPane, p);
            p.translate(dimPane.getPreferredSize().width + HORIZONTAL_OFFSET, 0);
        }
        
        p.setLocation(INITIAL_POINT.x, p.y + dimMaxHeight + VERTICAL_OFFSET);
        for (CubePane cubePane : cubePaneMap.values()) {
            pp.addPlayPenComponent(cubePane, p);
            p.translate(cubePane.getPreferredSize().width + HORIZONTAL_OFFSET, 0);
        }
        
        p.setLocation(INITIAL_POINT.x, p.y + cubeMaxHeight + VERTICAL_OFFSET);
        for (VirtualCubePane vCubePane : vCubePaneMap.values()) {
            pp.addPlayPenComponent(vCubePane, p);
            p.translate(vCubePane.getPreferredSize().width + HORIZONTAL_OFFSET, 0);
        }
        
        // creates the gui components for the usages.
        for (OLAPObject child : schema.getChildren(OLAPObject.class)) {
            if (child instanceof VirtualCube) {
                VirtualCube vCube = (VirtualCube) child;
                if (vCube.getCubeUsage() == null) continue;
                for (CubeUsage cubeUsage : vCube.getCubeUsage().getCubeUsages()) {
                    CubePane cubePane = cubePaneMap.get(cubeUsage.getCubeName());
                    VirtualCubePane vCubePane = vCubePaneMap.get(OLAPUtil.nameFor(vCube));
                    UsageComponent uc = new UsageComponent(pp.getContentPane(), cubeUsage, cubePane, vCubePane);
                    pp.getContentPane().addChild(uc, pp.getContentPane().getChildren().size());
                }
            } else if (child instanceof Cube) {
                Cube cube = (Cube) child;
                for (CubeDimension dim : cube.getDimensions()) {
                    if (dim instanceof DimensionUsage) {
                        DimensionUsage dimUsage = (DimensionUsage) dim;
                        DimensionPane dimPane = dimPaneMap.get(dimUsage.getSource());
                        CubePane cubePane = cubePaneMap.get(OLAPUtil.nameFor(cube));
                        UsageComponent uc = new UsageComponent(pp.getContentPane(), dimUsage, dimPane, cubePane);
                        pp.getContentPane().addChild(uc, pp.getContentPane().getChildren().size());
                    }
                }
            }
        }
    }
    
}
