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

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.ProjectLoader;
import ca.sqlpower.architect.UnclosableInputStream;
import ca.sqlpower.architect.ProjectSettings.ColumnVisibility;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.LiquibaseSettings;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.CriticGrouping;
import ca.sqlpower.architect.ddl.critic.CriticManager;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;
import ca.sqlpower.architect.olap.MondrianXMLReader;
import ca.sqlpower.architect.olap.MondrianXMLWriter;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.CompareDMSettings.SourceOrTargetSettings;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.swingui.olap.OLAPPane;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTypePhysicalProperties;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;
import ca.sqlpower.util.ExceptionReport;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.xml.XMLHelper;

/**
 * The SwingUIProject class is responsible for saving and loading projects.
 * It is capable of writing itself to an output stream, and reading in a 
 * previously-written stream to re-create a previous instance of a project at a 
 * later date.  Such "project files" are meant to be highly portable, and should 
 * remain backward compatible now that the product has been released.
 *
 * <p>Anyone who makes a change to the file reading code which causes a failure to
 * read older (release 1.0.19 or later) project files will get Airzooka'ed.
 */
public class SwingUIProjectLoader extends ProjectLoader {
    
    /**
     * The constant value within the project file representing a playpen whose
     * relationships should be drawn using rectilinear paths.
     */
    private static final String RELATIONSHIP_STYLE_RECTILINEAR = "rectilinear"; //$NON-NLS-1$

    /**
     * The constant value within the project file representing a playpen whose
     * relationships should be drawn using straight lines (often thought of as
     * diagonal, but that depends on the relative positions of the two tables
     * the relationship connects).
     */
    private static final String RELATIONSHIP_STYLE_DIRECT = "direct"; //$NON-NLS-1$

    private static final Logger logger = Logger.getLogger(SwingUIProjectLoader.class);

    /**
     * This map maps String ID codes to OLAPObject instances used in loading.
     */
    protected Map<String, OLAPObject> olapObjectLoadIdMap;

    /**
     * This holds mappings from OLAPObject instance to String ID used in saving.
     */
    protected Map<OLAPObject, String> olapObjectSaveIdMap;
    
    /**
     * This holds mappings from OLAPPane instance to String ID used in saving.
     */
    private Map<OLAPPane<?, ?>, String> olapPaneSaveIdMap;

    /**
     * This map maps String ID codes to OLAPPane instances used in loading.
     */
    protected  Map<String, OLAPPane<?, ?>> olapPaneLoadIdMap;
    
    /**
     * Shows progress during saves and loads.
     */
    private ProgressMonitor pm;

    /**
     * Sets up a new project file load/save object for the given session.
     * 
     * @param session the session that this instance will be responsible for
     * loading into and saving out from.
     * @throws NullPointerException if the given session is null
     */
    public SwingUIProjectLoader(ArchitectSwingSession session) {
        super(session);
        
        if (session == null) {
            throw new NullPointerException("Null session is not allowed!"); //$NON-NLS-1$
        }
        this.session = session;
    }

    /**
     * Override that also considers whether each OLAP edit session has modifications.
     */
    @Override
    public boolean isModified() {
        boolean olapModified = false;
        for (OLAPEditSession oSession : getSession().getOLAPEditSessions()) {
            olapModified |= oSession.isModified();
        }
        return olapModified || super.isModified();
    }
    
    public void load(InputStream in, DataSourceCollection<? extends SPDataSource> dataSources) throws IOException, SQLObjectException {
        load(in, dataSources, null);
    }
    
    // ------------- READING THE PROJECT FILE ---------------

    public void load(InputStream in, DataSourceCollection<? extends SPDataSource> dataSources,
            ArchitectSession messageDelegate) throws IOException, SQLObjectException {
        olapPaneLoadIdMap = new HashMap<String, OLAPPane<?, ?>>();
        
        UnclosableInputStream uin = new UnclosableInputStream(in);
        olapObjectLoadIdMap = new HashMap<String, OLAPObject>();
        
        // sqlObjectLoadIdMap is not ready yet when parsing the olap objects
        // so this keeps track of the id of the SQLDatabase that OLAPSessions reference.
        Map<OLAPSession, String> sessionDbMap = new HashMap<OLAPSession, String>();
        
        try {
            getSession().getUndoManager().setLoading(true);
            if (uin.markSupported()) {
                uin.mark(Integer.MAX_VALUE);
            } else {
                throw new IllegalStateException("Failed to load with an input stream that does not support mark!");
            }

            // parse the Mondrian business model parts first because the olap id
            // map is needed in the digester for parsing the olap gui
            try {
                MondrianXMLReader.parse(uin, getSession().getOLAPRootObject(), sessionDbMap, olapObjectLoadIdMap);
            } catch (SAXException e) {
                logger.error("Error parsing project file's olap schemas!", e);
                throw new SQLObjectException("SAX Exception in project file olap schemas parse!", e);
            } catch (Exception ex) {
                logger.error("General Exception in project file olap schemas parse!", ex);
                throw new SQLObjectException("Unexpected Exception", ex);
            }
            
            in.reset();
            
            super.load(in, dataSources, messageDelegate);
        } finally {
            getSession().getUndoManager().setLoading(false);
            uin.forceClose();
        }
        
        // now that the sqlObjectLoadIdMap is populated, we can set the
        // OLAPSessions' database.
        for (Map.Entry<OLAPSession, String> entry : sessionDbMap.entrySet()) {
            OLAPSession oSession = entry.getKey();
            SQLDatabase db = (SQLDatabase) sqlObjectLoadIdMap.get(entry.getValue());
            oSession.setDatabase(db);
        }
        
        // set the view positions again in the case that the viewport was invalid earlier.
        getSession().getPlayPen().setInitialViewPosition();
        for (OLAPEditSession editSession : getSession().getOLAPEditSessions()) {
            editSession.getOlapPlayPen().setInitialViewPosition();
        }
        
        // TODO change this to load the undo history from a file
        getSession().getUndoManager().discardAllEdits();
    }

    protected Digester setupDigester() throws ParserConfigurationException, SAXException {
        Digester d = super.setupDigester();
        
        PrintSettingsFactory printSettingsFactory = new PrintSettingsFactory();
        d.addFactoryCreate("*/print-settings", printSettingsFactory);
        d.addSetProperties("*/print-settings");

        // the play pen
        RelationalPlayPenFactory ppFactory = new RelationalPlayPenFactory();
        d.addFactoryCreate("architect-project/play-pen", ppFactory); //$NON-NLS-1$
        
        TablePaneFactory tablePaneFactory = new TablePaneFactory();
        d.addFactoryCreate("*/play-pen/table-pane", tablePaneFactory); //$NON-NLS-1$
        // factory will add the tablepanes to the playpen
        
        PPRelationshipFactory ppRelationshipFactory = new PPRelationshipFactory();
        d.addFactoryCreate("*/play-pen/table-link", ppRelationshipFactory); //$NON-NLS-1$
        
        CompareDMSettingFactory settingFactory = new CompareDMSettingFactory();
        d.addFactoryCreate("architect-project/compare-dm-settings", settingFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/compare-dm-settings"); //$NON-NLS-1$

        CompareDMStuffSettingFactory sourceStuffFactory = new CompareDMStuffSettingFactory(true);
        d.addFactoryCreate("architect-project/compare-dm-settings/source-stuff", sourceStuffFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/compare-dm-settings/source-stuff"); //$NON-NLS-1$

        CompareDMStuffSettingFactory targetStuffFactory = new CompareDMStuffSettingFactory(false);
        d.addFactoryCreate("architect-project/compare-dm-settings/target-stuff", targetStuffFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/compare-dm-settings/target-stuff"); //$NON-NLS-1$

        LiquibaseSettingsFactory lbSettingsFactory = new LiquibaseSettingsFactory();
        d.addFactoryCreate("architect-project/compare-dm-settings/liquibase-settings", lbSettingsFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/compare-dm-settings/liquibase-settings"); //$NON-NLS-1$

        CreateKettleJobSettingsFactory ckjsFactory = new CreateKettleJobSettingsFactory();
        d.addFactoryCreate("architect-project/create-kettle-job-settings", ckjsFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/create-kettle-job-settings"); //$NON-NLS-1$
        
        CriticManagerFactory criticManagerFactory = new CriticManagerFactory();
        d.addFactoryCreate("architect-project/critic-manager", criticManagerFactory);
        d.addSetProperties("architect-project/critic-manager");
        
        CriticGroupingFactory criticGroupingFactory = new CriticGroupingFactory();
        d.addFactoryCreate("architect-project/critic-manager/critic-grouping", criticGroupingFactory);
        d.addSetProperties("architect-project/critic-manager/critic-grouping");
        
        CriticSettingsFactory criticSettingsFactory = new CriticSettingsFactory();
        d.addFactoryCreate("architect-project/critic-manager/critic-grouping/critic-settings", criticSettingsFactory);
        
        // olap factories
        
        OLAPEditSessionFactory editSessionFactory = new OLAPEditSessionFactory();
        d.addFactoryCreate("architect-project/olap-gui/olap-edit-session", editSessionFactory); //$NON-NLS-1$
        
        OLAPPlayPenFactory olapPPFactory = new OLAPPlayPenFactory();
        d.addFactoryCreate("architect-project/olap-gui/olap-edit-session/play-pen", olapPPFactory); //$NON-NLS-1$

        CubePaneFactory cubePaneFactory = new CubePaneFactory();
        d.addFactoryCreate("*/play-pen/cube-pane", cubePaneFactory); //$NON-NLS-1$
        
        VirtualCubePaneFactory virtualCubePaneFactory = new VirtualCubePaneFactory();
        d.addFactoryCreate("*/play-pen/virtual-cube-pane", virtualCubePaneFactory); //$NON-NLS-1$
        
        DimensionPaneFactory dimensionPaneFactory = new DimensionPaneFactory();
        d.addFactoryCreate("*/play-pen/dimension-pane", dimensionPaneFactory); //$NON-NLS-1$
        
        UsageComponentFactory usageCompFactory = new UsageComponentFactory();
        d.addFactoryCreate("*/play-pen/usage-comp", usageCompFactory); //$NON-NLS-1$

        return d;
    }
    
    private class OLAPPlayPenFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof OLAPEditSession)) {
                logger.error("Expected parent OLAPEditSession object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Parent OLAPEditSession not found!"); //$NON-NLS-1$
            }
            
            OLAPEditSession editSession = (OLAPEditSession) topItem;
            PlayPen pp = editSession.getOlapPlayPen();
            setupGenericPlayPen(pp, attributes);
            return pp;
        }
    }
    
    private class RelationalPlayPenFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            PlayPen pp = getSession().getPlayPen();
        	setupGenericPlayPen(pp, attributes);
        	
        	// default values in playpen are true
        	
        	String columnVisibility = attributes.getValue("columnVisibility"); //$NON-NLS-1$
        	if (columnVisibility != null) {
        	    getSession().setColumnVisibility(ColumnVisibility.valueOf(columnVisibility));
        	}
        	
        	String showPrimaryTag = attributes.getValue("showPrimaryTag"); //$NON-NLS-1$
        	if (showPrimaryTag == null) {
        	    getSession().setShowPkTag(true);
        	} else if (!Boolean.valueOf(showPrimaryTag)) {
        	    getSession().setShowPkTag(false);
        	} else {
        	    getSession().setShowPkTag(true);
        	}
        	
        	String showForeignTag = attributes.getValue("showForeignTag"); //$NON-NLS-1$
            if (showForeignTag == null) {
                getSession().setShowFkTag(true);
            } else if (!Boolean.valueOf(showForeignTag)) {
                getSession().setShowFkTag(false);
            } else {
                getSession().setShowFkTag(true);
            }
            
            String showAlternateTag = attributes.getValue("showAlternateTag"); //$NON-NLS-1$
            if (showAlternateTag == null) {
                getSession().setShowAkTag(true);
            } else if (!Boolean.valueOf(showAlternateTag)) {
                getSession().setShowAkTag(false);
            } else {
                getSession().setShowAkTag(true);
            }
            
            String usingLogicalNames = attributes.getValue("names-displayLogicalNames"); //$NON-NLS-1$
            if (usingLogicalNames == null) {
                getSession().setUsingLogicalNames(true);
            } else if (!Boolean.valueOf(usingLogicalNames)) {
                getSession().setUsingLogicalNames(false);
            } else {
                getSession().setUsingLogicalNames(true);
            }
        	
            String relationshipLabelVisibility = attributes.getValue("relationshipLabelVisibility"); //$NON-NLS-1$
            if (relationshipLabelVisibility == null) {
                getSession().setDisplayRelationshipLabel(true);
            } else if (!Boolean.valueOf(relationshipLabelVisibility)) {
                getSession().setDisplayRelationshipLabel(false);
            } else {
                getSession().setDisplayRelationshipLabel(true);
            }

            String quoteIdentifiers = attributes.getValue("quoteIdentifiers"); //$NON-NLS-1$
            if (quoteIdentifiers == null) {
                getSession().setQuoteIdentifiers(false);
            } else if (!Boolean.valueOf(quoteIdentifiers)) {
                getSession().setQuoteIdentifiers(false);
            } else {
                getSession().setQuoteIdentifiers(true);
            }
            
        	String relStyle = attributes.getValue("relationship-style"); //$NON-NLS-1$
            boolean direct;
            if (relStyle == null) {
                direct = false;
            } else if (relStyle.equals(RELATIONSHIP_STYLE_DIRECT)) {
                direct = true;
            } else if (relStyle.equals(RELATIONSHIP_STYLE_RECTILINEAR)) {
                direct = false;
            } else {
                logger.warn("Unknown relationship style \"\"; defaulting to rectilinear"); //$NON-NLS-1$
                direct = false;
            }
            getSession().setRelationshipLinesDirect(direct);
            return pp;
        }

    }
    
    /**
     * Sets up everything in common between all types of playpens.
     * 
     * @param pp
     *            the play pen to set up
     * @param attributes
     *            The attributes from the start tag
     */
    private void setupGenericPlayPen(PlayPen pp, Attributes attributes) {
        String zoomLevel = attributes.getValue("zoom"); //$NON-NLS-1$
        if (zoomLevel != null) {
            pp.setZoom(Double.parseDouble(zoomLevel));
        }
        
        String viewportX = attributes.getValue("viewportX"); //$NON-NLS-1$
        String viewportY = attributes.getValue("viewportY"); //$NON-NLS-1$
        
        if (viewportX != null && viewportY != null) {
            Point viewPoint = new Point(Integer.parseInt(viewportX), Integer.parseInt(viewportY));
            pp.setViewPosition(viewPoint);
        }
        logger.debug("Viewport position is " + pp.getViewPosition()); //$NON-NLS-1$
    }

    private class TablePaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof PlayPen)) {
                logger.error("Expected parent PlayPen object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Parent PlayPen object not found!"); //$NON-NLS-1$
            }
            
            PlayPen pp = (PlayPen) topItem;
            int x = Integer.parseInt(attributes.getValue("x")); //$NON-NLS-1$
            int y = Integer.parseInt(attributes.getValue("y")); //$NON-NLS-1$
            SQLTable tab = (SQLTable) sqlObjectLoadIdMap.get(attributes.getValue("table-ref")); //$NON-NLS-1$
            TablePane tp = new TablePane(tab, pp.getContentPane());
            
            String bgColorString = attributes.getValue("bgColor"); //$NON-NLS-1$
            if (bgColorString != null) {
                Color bgColor = Color.decode(bgColorString);
                tp.setBackgroundColor(bgColor);
            }
            String fgColorString = attributes.getValue("fgColor"); //$NON-NLS-1$
            if (fgColorString != null) {
                Color fgColor = Color.decode(fgColorString);
                tp.setForegroundColor(fgColor);
            }
            
            boolean rounded = "true".equals(attributes.getValue("rounded")); //$NON-NLS-1$ //$NON-NLS-2$
            tp.setRounded(rounded);
            
            boolean dashed = "true".equals(attributes.getValue("dashed")); //$NON-NLS-1$ //$NON-NLS-2$
            tp.setDashed(dashed);
                
            pp.addTablePane(tp, new Point(x, y));
            return tp;
        }
    }
    
    private class CubePaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof PlayPen)) {
                logger.error("Expected parent PlayPen object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Parent PlayPen object not found!"); //$NON-NLS-1$
            }
            
            PlayPen pp = (PlayPen) topItem;
            int x = Integer.parseInt(attributes.getValue("x")); //$NON-NLS-1$
            int y = Integer.parseInt(attributes.getValue("y")); //$NON-NLS-1$
            Cube cube = (Cube) olapObjectLoadIdMap.get(attributes.getValue("model-ref")); //$NON-NLS-1$
            CubePane cp = new CubePane(cube, pp.getContentPane());
                
            pp.addPlayPenComponent(cp, new Point(x, y));
            
            String id = attributes.getValue("id");
            if (id != null) {
                olapPaneLoadIdMap.put(id, cp);
            } else {
                logger.warn("No ID element found in cube pane element while loading project!");
            }
            
            return cp;
        }
    }
    
    private class VirtualCubePaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof PlayPen)) {
                logger.error("Expected parent PlayPen object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Parent PlayPen object not found!"); //$NON-NLS-1$
            }
            
            PlayPen pp = (PlayPen) topItem;
            int x = Integer.parseInt(attributes.getValue("x")); //$NON-NLS-1$
            int y = Integer.parseInt(attributes.getValue("y")); //$NON-NLS-1$
            VirtualCube virtualCube = (VirtualCube) olapObjectLoadIdMap.get(attributes.getValue("model-ref")); //$NON-NLS-1$
            VirtualCubePane vcp = new VirtualCubePane(virtualCube, pp.getContentPane());
                
            pp.addPlayPenComponent(vcp, new Point(x, y));
            
            String id = attributes.getValue("id");
            if (id != null) {
                olapPaneLoadIdMap.put(id, vcp);
            } else {
                logger.warn("No ID element found in virtual cube pane element while loading project!");
            }
            
            return vcp;
        }
    }
    
    private class DimensionPaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof PlayPen)) {
                logger.error("Expected parent PlayPen object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Parent PlayPen object not found!"); //$NON-NLS-1$
            }
            
            PlayPen pp = (PlayPen) topItem;
            int x = Integer.parseInt(attributes.getValue("x")); //$NON-NLS-1$
            int y = Integer.parseInt(attributes.getValue("y")); //$NON-NLS-1$
            Dimension dim = (Dimension) olapObjectLoadIdMap.get(attributes.getValue("model-ref")); //$NON-NLS-1$
            DimensionPane dp = new DimensionPane(dim, pp.getContentPane());
                
            pp.addPlayPenComponent(dp, new Point(x, y));
            
            String id = attributes.getValue("id");
            if (id != null) {
                olapPaneLoadIdMap.put(id, dp);
            } else {
                logger.warn("No ID element found in dimension pane element while loading project!");
            }
            
            return dp;
        }
    }

    private class PPRelationshipFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof PlayPen)) {
                logger.error("Expected parent PlayPen object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Parent PlayPen object not found!"); //$NON-NLS-1$
            }
            
            PlayPen pp = (PlayPen) topItem;
            Relationship r = null;
            try {
                try {
                    SQLRelationship rel =
                        (SQLRelationship) sqlObjectLoadIdMap.get(attributes.getValue("relationship-ref")); //$NON-NLS-1$
                    r = new Relationship(rel, pp.getContentPane());
                    pp.addRelationship(r);
                    r.updateUI();
                    
                    //In Architect <= 1.0.0, this is how relationships were saved.
                    //We need backwards compatability so we check for how they are saved.
                    String pkxStr = attributes.getValue("pk-x"); //$NON-NLS-1$
                    if(pkxStr != null) {
                        int pkx = Integer.parseInt(pkxStr);
                        int pky = Integer.parseInt(attributes.getValue("pk-y")); //$NON-NLS-1$
                        int fkx = Integer.parseInt(attributes.getValue("fk-x")); //$NON-NLS-1$
                        int fky = Integer.parseInt(attributes.getValue("fk-y")); //$NON-NLS-1$
                        r.setPkConnectionPoint(new Point(pkx, pky));
                        r.setFkConnectionPoint(new Point(fkx, fky));
                    } else {
                        double pk = Double.parseDouble(attributes.getValue("pkConnection")); //$NON-NLS-1$
                        double fk = Double.parseDouble(attributes.getValue("fkConnection")); //$NON-NLS-1$
                        r.setPkConnection(pk);
                        r.setFkConnection(fk);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null,
                            "Warning: your relationship point connection data was not loaded properly, but\n" +
                            "it was not a critical failure. This file could be too old or corrupted.", 
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    
                    logger.warn("Didn't set connection points because of integer parse error"); //$NON-NLS-1$
                } catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(null,
                            "Warning: your relationship point connection data was not loaded properly, but\n" +
                            "it was not a critical failure. This file could be too old or corrupted.", 
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    
                    logger.debug("No pk/fk connection points specified in save file;" //$NON-NLS-1$
                            +" not setting custom connection points"); //$NON-NLS-1$
                }

                String rOrientation = attributes.getValue("orientation");
                if (rOrientation != null) {
                    int orientation = Integer.parseInt(rOrientation); //$NON-NLS-1$
                    r.setOrientation(orientation);
                }
                String rLineColor = attributes.getValue("rLineColor"); //$NON-NLS-1$
                if (rLineColor != null) {
                    Color relationshipLineColor = Color.decode(rLineColor);
                    r.setForegroundColor(relationshipLineColor);
                }
                
                String pkLabelText = attributes.getValue("pkLabelText"); //$NON-NLS-1$
                if (pkLabelText != null) {
                    r.setTextForParentLabel(pkLabelText);
                }
                String fkLabelText = attributes.getValue("fkLabelText"); //$NON-NLS-1$
                if (fkLabelText != null) {
                    r.setTextForChildLabel(fkLabelText);
                }
            } catch (SQLObjectException e) {
                logger.error("Couldn't create relationship component", e); //$NON-NLS-1$
            } catch (NullPointerException e) {
                logger.error("Error loading a relationship.", e); //$NON-NLS-1$
            }
            return r;
        }
    }
    
    private class UsageComponentFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof PlayPen)) {
                logger.error("Expected parent PlayPen object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Parent PlayPen object not found!"); //$NON-NLS-1$
            }
            
            PlayPen pp = (PlayPen) topItem;
            OLAPObject model = olapObjectLoadIdMap.get(attributes.getValue("model-ref")); //$NON-NLS-1$
            OLAPPane<?, ?> pane1 = olapPaneLoadIdMap.get(attributes.getValue("pane1-ref")); //$NON-NLS-1$
            OLAPPane<?, ?> pane2 = olapPaneLoadIdMap.get(attributes.getValue("pane2-ref")); //$NON-NLS-1$
            UsageComponent usageComp = new UsageComponent(pp.getContentPane(), model, pane1, pane2);
            
            pp.getContentPane().addChild(usageComp, pp.getContentPane().getChildren().size());
            return usageComp;
        }
    }

    private class OLAPEditSessionFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            OLAPSession oSession =
                (OLAPSession) olapObjectLoadIdMap.get(attributes.getValue("osession-ref")); //$NON-NLS-1$
            return getSession().getOLAPEditSession(oSession);
        }
    }
    

    private class CreateKettleJobSettingsFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) throws SQLException {
            return getSession().getKettleJob();
        }
    }

    /**
     * Creates a LiquibaseSettings instance and adds it to the objectIdMap.
     */
    private class LiquibaseSettingsFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
			LiquibaseSettings settings = getSession().getCompareDMSettings().getLiquibaseSettings();
			if (settings == null) {
				settings = new LiquibaseSettings();
				getSession().getCompareDMSettings().setLiquibaseSettings(settings);
			}
            return settings;
        }
    }
    /**
     * Creates a compareDM instance and adds it to the objectIdMap.
     */
    private class CompareDMSettingFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            return getSession().getCompareDMSettings();
        }
    }

    private class CompareDMStuffSettingFactory extends AbstractObjectCreationFactory {
        private boolean source;
        public CompareDMStuffSettingFactory(boolean source) {
            this.source = source;
        }
        public Object createObject(Attributes attributes) {
            if ( source )
                return getSession().getCompareDMSettings().getSourceSettings();
            else
                return getSession().getCompareDMSettings().getTargetSettings();
        }
    }
    
    private class PrintSettingsFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes arg0) throws Exception {
            return getSession().getPrintSettings();
        }
        
    }
    
    private class CriticGroupingFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes attr) throws Exception {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof CriticManager)) {
                logger.error("Expected parent CriticManager object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Ancestor CriticManager object not found!"); //$NON-NLS-1$
            }
            CriticManager criticManager = (CriticManager) topItem;
            String platformType = attr.getValue("platformType");
            CriticGrouping group = new CriticGrouping(platformType);
            
            criticManager.addChild(group, criticManager.getChildren(CriticGrouping.class).size());
            
            return group;
        }
        
    }
    
    private class CriticManagerFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes attr) throws Exception {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof ArchitectSwingSession)) {
                logger.error("Expected parent ArchitectSwingSession object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Ancestor ArchitectSwingSession object not found!"); //$NON-NLS-1$
            }
            ArchitectSwingSession session = (ArchitectSwingSession) topItem;
            
            session.getWorkspace().getCriticManager().clear();
            return session.getWorkspace().getCriticManager();
        }
    }
    
    private class CriticSettingsFactory extends AbstractObjectCreationFactory {
        @Override
        public Object createObject(Attributes attr) throws Exception {
            Object topItem = getDigester().peek();
            if (!(topItem instanceof CriticGrouping)) {
                logger.error("Expected parent CriticGrouping object on top of stack but found: " + topItem); //$NON-NLS-1$
                throw new IllegalStateException("Ancestor CriticGrouping object not found!"); //$NON-NLS-1$
            }
            CriticGrouping group = (CriticGrouping) topItem;
            
            String criticClassName = attr.getValue("class");
            Class<?> criticClass = getClass().getClassLoader().loadClass(criticClassName);
            CriticAndSettings criticSettings = (CriticAndSettings) criticClass.getConstructor().newInstance();
            
            String severity = attr.getValue("severity");
            criticSettings.setSeverity(Severity.valueOf(severity));
            
            group.getParent().registerCritic(criticSettings);
            return criticSettings;
        }
        
    }
    
    // ------------- WRITING THE PROJECT FILE ---------------

    /**
     * Saves this project by writing an XML description of it to a temp file, then renaming.
     * The location of the file is determined by this project's <code>file</code> property.
     *
     * @param pm An optional progress monitor which will be initialised then updated
     * periodically during the save operation.  If you use a progress monitor, don't
     * invoke this method on the AWT event dispatch thread!
     */
    public void save(ProgressMonitor pm) throws IOException, SQLObjectException {
        // write to temp file and then rename (this preserves old project file
        // when there's problems)
        if (file.exists() && !file.canWrite()) {
            // write problems with architect file will muck up the save process
            throw new SQLObjectException(Messages.getString("SwingUIProject.errorSavingProject", file.getAbsolutePath())); //$NON-NLS-1$
        }
        
        if (fileVersion != null && !fileVersion.equals(ArchitectVersion.APP_FULL_VERSION.toString())) {
            String message;
            try {
                ArchitectVersion oldFileVersion = new ArchitectVersion(fileVersion);
                if (oldFileVersion.compareTo(ArchitectVersion.APP_FULL_VERSION) < 0) {
                    message = "Overwriting older file. Older versions may have problems " +
                    		"loading the newer file format.";
                } else {
                    message = "Overwriting newer file. Some data loss from loading may occur.";
                }
            } catch (Exception e) {
                message = "Overwriting file with an invalid version.";
            }
            UserPrompter prompter = getSession().createUserPrompter(message + 
                    "\nDo you wish to continue?", UserPromptType.BOOLEAN, 
                    UserPromptOptions.OK_CANCEL, UserPromptResponse.OK, 
                    UserPromptResponse.OK, "OK", "Cancel");
            UserPromptResponse response = prompter.promptUser();
            if (response.equals(UserPromptResponse.CANCEL)) {
                return;
            }
        }

        File backupFile = new File (file.getParent(), file.getName()+"~"); //$NON-NLS-1$

        // Several places we would check dir perms, but MS-Windows stupidly doesn't let use the
        // "directory write" attribute for directory writing (but instead overloads
        // it to mean 'this is a special directory'.

        File tempFile = null;
        tempFile = new File (file.getParent(),"tmp___" + file.getName()); //$NON-NLS-1$
        String encoding = "UTF-8"; //$NON-NLS-1$
        try {
            // If creating this temp file fails, feed the user back a more explanatory message
            out = new PrintWriter(tempFile,encoding);
        } catch (IOException e) {
            throw new SQLObjectException(Messages.getString("SwingUIProject.cannotCreateOutputFile") + e, e); //$NON-NLS-1$
        }

        progress = 0;
        this.pm = pm;
        if (pm != null) {
            int pmMax = 0;
            pm.setMinimum(0);
            if (getSession().isSavingEntireSource()) {
                pmMax = SQLObjectUtils.countTablesSnapshot((SQLObject) getSession().getDBTree().getModel().getRoot());
            } else {
                pmMax = SQLObjectUtils.countTables((SQLObject) getSession().getDBTree().getModel().getRoot());
            }
            logger.debug("Setting progress monitor maximum to "+pmMax); //$NON-NLS-1$
            pm.setMaximum(pmMax);
            pm.setProgress(progress);
            pm.setMillisToDecideToPopup(0);
        }

        save(out,encoding);	// Does ALL the actual I/O
        out = null;
        if (pm != null)
            pm.close();
        pm = null;

        // Do the rename dance.
        // This is a REALLY bad place for failure (especially if we've made the user wait several hours to save
        // a large project), so we MUST check failures from renameto (both places!)
        boolean fstatus = false;
        fstatus = backupFile.delete();
        logger.debug("deleting backup~ file: " + fstatus); //$NON-NLS-1$

        // If this is a brand new project, the old file does not yet exist, no point trying to rename it.
        // But if it already existed, renaming current to backup must succeed, or we give up.
        if (file.exists()) {
            fstatus = file.renameTo(backupFile);
            logger.debug("rename current file to backupFile: " + fstatus); //$NON-NLS-1$
            if (!fstatus) {
                throw new SQLObjectException((
                        Messages.getString("SwingUIProject.couldNotRenameFile", tempFile.toString(), file.toString()))); //$NON-NLS-1$
            }
        }
        fstatus = tempFile.renameTo(file);
        if (!fstatus) {
            throw new SQLObjectException((
                    Messages.getString("SwingUIProject.couldNotRenameTempFile", tempFile.toString(), file.toString()))); //$NON-NLS-1$
        }
        logger.debug("rename tempFile to current file: " + fstatus); //$NON-NLS-1$
        fileVersion = ArchitectVersion.APP_FULL_VERSION.toString();
    }

    XMLHelper ioo = new XMLHelper();
    
    /**
     * Do just the writing part of save, given a PrintWriter
     * @param out - the file to write to
     * @throws IOException
     */
    public void save(PrintWriter out, String encoding) throws IOException {
        sqlObjectSaveIdMap = new IdentityHashMap<SQLObject, String>();
        olapObjectSaveIdMap = new IdentityHashMap<OLAPObject, String>();
        dbcsSaveIdMap = new HashMap<SPDataSource, String>();
        olapPaneSaveIdMap = new HashMap<OLAPPane<?,?>, String>();
        
        ioo.indent = 0;

        try {
            ioo.println(out, "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
            ioo.println(out, "<architect-project version=\"1.0\" appversion=\""+ArchitectVersion.APP_FULL_VERSION+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
            ioo.indent++;
            ioo.println(out, "<project-name>"+SQLPowerUtils.escapeXML(getSession().getName())+"</project-name>"); //$NON-NLS-1$ //$NON-NLS-2$
            savePrintSettings(out, getSession().getPrintSettings());
            saveDataSources(out);
            saveSourceDatabases(out);
            saveTargetDatabase(out);
            saveDDLGenerator(out);
            saveCompareDMSettings(out);
            saveCreateKettleJobSettings(out);
            savePlayPen(out, getSession().getPlayPen(), true);
            saveCriticSettings(out);
            saveProfiles(out);
            
            saveOLAP(out);
            saveOLAPGUI(out);
            
            ioo.indent--;
            ioo.println(out, "</architect-project>"); //$NON-NLS-1$
            
            setModified(false);
            for (OLAPEditSession oSession : getSession().getOLAPEditSessions()) {
                oSession.saveNotify();
            }
        } catch (IOException e) {
            ioo.println(out, new ExceptionReport(e, "", ArchitectVersion.APP_FULL_VERSION.toString(), "Architect").toXML());
            throw e;
        } catch (RuntimeException e) {
            ioo.println(out, new ExceptionReport(e, "", ArchitectVersion.APP_FULL_VERSION.toString(), "Architect").toXML());
            throw e;
        } finally {
            if (out != null) out.close();
        }
    }

    public void save(OutputStream out, String encoding) throws IOException {
        save(new PrintWriter(new OutputStreamWriter(out, encoding)), encoding);
    }
    
    private void saveOLAP(PrintWriter out) {
        ioo.indent++;
        ioo.println(out, "<olap>"); //$NON-NLS-1$
        ioo.indent++;
        
        for (OLAPSession osession : getSession().getOLAPRootObject().getChildren()) {
            String id = Integer.toString(olapObjectSaveIdMap.size());
            if (olapObjectSaveIdMap.put(osession, id) != null) {
                logger.debug("Duplicate OLAPObject in project file xml: " + osession); //$NON-NLS-1$
                throw new IllegalStateException("Duplicate OLAPObject found in project file!"); //$NON-NLS-1$
            }
            StringBuilder tagText = new StringBuilder();
            tagText.append("<olap-session id=").append(quote(id)); //$NON-NLS-1$
            
            if (osession.getDatabase() != null) {
                tagText.append(" db-ref="); //$NON-NLS-1$
                tagText.append(quote(sqlObjectSaveIdMap.get(osession.getDatabase())));
            }
            tagText.append(">"); //$NON-NLS-1$
            ioo.println(out, tagText.toString());
            
            ioo.indent++;
            MondrianXMLWriter.write(out, osession.getSchema(), false, ioo.indent, olapObjectSaveIdMap);
            ioo.indent--;
            ioo.println(out, "</olap-session>"); //$NON-NLS-1$
        }
        ioo.indent--;
        ioo.println(out, "</olap>"); //$NON-NLS-1$
        ioo.indent--;
    }
    
    private void saveOLAPGUI(PrintWriter out) {
        ioo.indent++;
        ioo.println(out, "<olap-gui>");
        
        ioo.indent++;
        for (OLAPEditSession editSession : getSession().getOLAPEditSessions()) {
            ioo.println(out, "<olap-edit-session osession-ref=" + //$NON-NLS-1$ 
                    quote(olapObjectSaveIdMap.get(editSession.getOlapSession())) + ">"); //$NON-NLS-1$ 
            ioo.indent++;
            savePlayPen(out, editSession.getOlapPlayPen(), false);
            ioo.indent--;
            ioo.println(out, "</olap-edit-session>"); //$NON-NLS-1$
        }
        ioo.indent--;
        
        ioo.println(out, "</olap-gui>");
        ioo.indent--;
    }
    
    private void saveDataSources(PrintWriter out) throws IOException {
        // FIXME this needs work.  It should include everything we need in order to build
        //       the referenced parent type from scratch (except the jdbc driver path)
        //       and the code that loads a project should check if the referenced parent
        //       type exists.  If not, we need to create everything we can about the parent
        //       type, then show the driver manager gui and get the user to pick a jdbc driver file.
        ioo.println(out, "<project-data-sources>"); //$NON-NLS-1$
        ioo.indent++;
        int dsNum = 0;
        SQLObject dbTreeRoot = (SQLObject) getSession().getDBTree().getModel().getRoot();
        Iterator<? extends SQLObject> it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = it.next();
            SPDataSource ds = ((SQLDatabase) o).getDataSource();
            if (ds != null) {
                String id = dbcsSaveIdMap.get(ds);
                if (id == null) {
                    id = "DS"+dsNum; //$NON-NLS-1$
                    dbcsSaveIdMap.put(ds, id);
                }
                ioo.println(out, "<data-source id=\""+SQLPowerUtils.escapeXML(id)+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.indent++;
                Iterator<Map.Entry<String, String>> pit = ds.getPropertiesMap().entrySet().iterator();
                while (pit.hasNext()) {
                    Map.Entry<String, String> ent = pit.next();
                    if (ent.getValue() != null) {
                        ioo.println(out, "<property key="+quote((String) ent.getKey())+" value="+quote((String) ent.getValue())+" />"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                ioo.indent--;
                ioo.println(out, "</data-source>"); //$NON-NLS-1$
                dsNum++;
            }
            dsNum++;
        }
        ioo.indent--;
        ioo.println(out, "</project-data-sources>"); //$NON-NLS-1$
    }
    

    private void saveDDLGenerator(PrintWriter out) throws IOException {
        ioo.print(out, "<ddl-generator" //$NON-NLS-1$
                +" type=\""+ getSession().getDDLGenerator().getClass().getName()+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                +" allow-connection=\""+getSession().getDDLGenerator().getAllowConnection()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        if (getSession().getDDLGenerator().getTargetCatalog() != null) {
            ioo.niprint(out, " target-catalog=\""+SQLPowerUtils.escapeXML(getSession().getDDLGenerator().getTargetCatalog())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (getSession().getDDLGenerator().getTargetSchema() != null) {
            ioo.niprint(out, " target-schema=\""+SQLPowerUtils.escapeXML(getSession().getDDLGenerator().getTargetSchema())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ioo.niprint(out, ">"); //$NON-NLS-1$
        ioo.println(out, "</ddl-generator>"); //$NON-NLS-1$
		saveLiquibaseSettings(out, session.getLiquibaseSettings());
    }
    
    private void saveCreateKettleJobSettings(PrintWriter out) throws IOException {
        ioo.print(out, "<create-kettle-job-settings"); //$NON-NLS-1$
        ioo.niprint(out, " filePath=\"" + SQLPowerUtils.escapeXML(getSession().getKettleJob().getFilePath()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.niprint(out, " jobName=\"" + SQLPowerUtils.escapeXML(getSession().getKettleJob().getJobName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.niprint(out, " schemaName=\"" + SQLPowerUtils.escapeXML(getSession().getKettleJob().getSchemaName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.niprint(out, " kettleJoinType=\"" + getSession().getKettleJob().getKettleJoinType() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.niprint(out, " savingToFile=\"" + getSession().getKettleJob().isSavingToFile() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.niprintln(out, " />"); //$NON-NLS-1$
    }

    /**
     * Writes out the CompareDM settings for this project unless the user has not
     * used that feature.
     *
     * @param out
     * @throws IOException
     */
    private void saveCompareDMSettings(PrintWriter out) throws IOException {

        if ( !getSession().getCompareDMSettings().getSaveFlag() )
            return;
        ioo.print(out, "<compare-dm-settings"); //$NON-NLS-1$
        Class<? extends DDLGenerator> ddlgClass = getSession().getCompareDMSettings().getDdlGenerator();
        if (ddlgClass != null) {
            ioo.print(out, " ddlGenerator=\""+SQLPowerUtils.escapeXML(ddlgClass.getName())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ioo.print(out, " outputFormatAsString=\""+SQLPowerUtils.escapeXML(getSession().getCompareDMSettings().getOutputFormatAsString())+"\""); //$NON-NLS-1$ //$NON-NLS-2$

		// this ensures that settings that are loaded from the project are written back to the project file
		// even if the user did use the compare DM dialog
		ioo.print(out, " saveFlag=\"true\"");
        ioo.println(out, ">"); //$NON-NLS-1$
        ioo.indent++;
        ioo.print(out, "<source-stuff"); //$NON-NLS-1$
        saveSourceOrTargetAttributes(out, getSession().getCompareDMSettings().getSourceSettings());
        ioo.println(out, "/>"); //$NON-NLS-1$
        ioo.print(out, "<target-stuff"); //$NON-NLS-1$
        saveSourceOrTargetAttributes(out, getSession().getCompareDMSettings().getTargetSettings());
        ioo.println(out, "/>"); //$NON-NLS-1$
		saveLiquibaseSettings(out, getSession().getCompareDMSettings().getLiquibaseSettings());
        ioo.indent--;
        ioo.println(out, "</compare-dm-settings>"); //$NON-NLS-1$
    }
    
    /**
     * Writes the current critic settings for this project.
     */
    private void saveCriticSettings(PrintWriter out) throws IOException {
        CriticManager criticManager = getSession().getWorkspace().getCriticManager();
        ioo.println(out, "<critic-manager>");
        ioo.indent++;
        for (CriticGrouping group : criticManager.getCriticGroupings()) {
            ioo.print(out, "<critic-grouping");
            ioo.niprint(out, " platformType=\"" + group.getPlatformType() + "\"");
            ioo.niprint(out, " enabled=\"" + Boolean.toString(group.isEnabled()) + "\"");
            ioo.niprintln(out, ">");
            ioo.indent++;
            for (CriticAndSettings settings : group.getSettings()) {
                ioo.print(out, "<critic-settings");
                ioo.niprint(out, " class=\"" + settings.getClass().getName() + "\"");
                ioo.niprint(out, " severity=\"" + settings.getSeverity().name() + "\"");
                ioo.niprintln(out, "/>");
            }
            ioo.indent--;
            ioo.println(out, "</critic-grouping>");
        }
        ioo.indent--;
        ioo.println(out, "</critic-manager>");
    }

	private void saveLiquibaseSettings(PrintWriter out, LiquibaseSettings settings) {
		if (settings == null) return;
		StringBuilder xml = new StringBuilder(150);
		xml.append("<liquibase-settings");
		xml.append(" useSeparateChangeSets=\"" + Boolean.toString(settings.getUseSeparateChangeSets()) + "\"");
		String author = settings.getAuthor();
		if (StringUtils.isNotBlank(author)) {
			xml.append(" author=\"" + SQLPowerUtils.escapeXML(author) + "\"");
		}
		xml.append(" generateId=\"" + Boolean.toString(settings.getGenerateId()) + "\"");
		xml.append(" idStart=\"" + Integer.toString(settings.getIdStart()) + "\"");
		xml.append("/>");
		ioo.println(out, xml.toString());
	}

    private void saveSourceOrTargetAttributes(PrintWriter out, SourceOrTargetSettings sourceSettings) {
        ioo.print(out, " datastoreTypeAsString=\""+SQLPowerUtils.escapeXML(sourceSettings.getDatastoreTypeAsString())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        if (sourceSettings.getConnectName() != null)
            ioo.print(out, " connectName=\""+SQLPowerUtils.escapeXML(sourceSettings.getConnectName())+"\""); //$NON-NLS-1$ //$NON-NLS-2$

        if (sourceSettings.getCatalog() != null)
            ioo.print(out, " catalog=\""+SQLPowerUtils.escapeXML(sourceSettings.getCatalog())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        if (sourceSettings.getSchema() != null)
            ioo.print(out, " schema=\""+SQLPowerUtils.escapeXML(sourceSettings.getSchema())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.print(out, " filePath=\""+SQLPowerUtils.escapeXML(sourceSettings.getFilePath())+"\""); //$NON-NLS-1$ //$NON-NLS-2$

    }
    /**
     * Creates a &lt;source-databases&gt; element which contains zero
     * or more &lt;database&gt; elements.
     * @param out
     */
    private void saveSourceDatabases(PrintWriter out) throws IOException {
        ioo.println(out, "<source-databases>"); //$NON-NLS-1$
        ioo.indent++;
        SQLObject dbTreeRoot = (SQLObject) getSession().getDBTree().getModel().getRoot();
        Iterator<? extends SQLObject> it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = it.next();
            if (o != getSession().getTargetDatabase()) {
                saveSQLObject(out, o);
            }
        }
        ioo.indent--;
        ioo.println(out, "</source-databases>"); //$NON-NLS-1$
    }

    /**
     * Recursively walks through the children of db, writing to the
     * output file all SQLRelationship objects encountered.
     */
    private void saveRelationships(PrintWriter out, SQLDatabase db) throws IOException {
        ioo.println(out, "<relationships>"); //$NON-NLS-1$
        ioo.indent++;
        Iterator<? extends SQLObject> it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveRelationshipsRecurse(out, it.next());
        }
        ioo.indent--;
        ioo.println(out, "</relationships>"); //$NON-NLS-1$
    }

    /**
     * The recursive subroutine of saveRelationships.
     */
    private void saveRelationshipsRecurse(PrintWriter out, SQLObject o) throws IOException {
        if ( (!getSession().isSavingEntireSource()) && (!o.isPopulated()) ) {
            return;
        } else if (o instanceof SQLRelationship) {
            saveSQLObject(out, o);
        } else if (o.allowsChildren()) {
            Iterator<? extends SQLObject> it = o.getChildren().iterator();
            while (it.hasNext()) {
                saveRelationshipsRecurse(out, it.next());
            }
        }
    }

    private void saveTargetDatabase(PrintWriter out) throws IOException {
        SQLDatabase db = (SQLDatabase) getSession().getTargetDatabase();
        ioo.println(out, "<target-database id=\"ppdb\" dbcs-ref="+ //$NON-NLS-1$
                quote(dbcsSaveIdMap.get(db.getDataSource()))+ ">"); //$NON-NLS-1$
        sqlObjectSaveIdMap.put(db, "ppdb"); //$NON-NLS-1$
        ioo.indent++;
        Iterator<? extends SQLObject> it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveSQLObject(out, it.next());
        }
        saveRelationships(out, db);
        ioo.indent--;
        ioo.println(out, "</target-database>"); //$NON-NLS-1$
    }

    private void savePlayPen(PrintWriter out, PlayPen pp, boolean isRelational) {
        StringBuilder tagText = new StringBuilder();
        tagText.append("<play-pen zoom=\"").append(pp.getZoom()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        tagText.append(" viewportX=\"").append(pp.getViewPosition().x).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        tagText.append(" viewportY=\"").append(pp.getViewPosition().y).append("\"");  //$NON-NLS-1$ //$NON-NLS-2$
        
        if (isRelational) {
            String relStyle = getSession().getRelationshipLinesDirect() ?
                    RELATIONSHIP_STYLE_DIRECT : RELATIONSHIP_STYLE_RECTILINEAR;
            tagText.append(" relationship-style=").append(quote(relStyle)); //$NON-NLS-1$
            tagText.append(" names-displayLogicalNames=\"").append(getSession().isUsingLogicalNames()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            tagText.append(" showPrimaryTag=\"").append(getSession().isShowPkTag()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            tagText.append(" showForeignTag=\"").append(getSession().isShowFkTag()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            tagText.append(" showAlternateTag=\"").append(getSession().isShowAkTag()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            tagText.append(" columnVisibility=\"").append(getSession().getColumnVisibility()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            tagText.append(" relationshipLabelVisibility=\"").append(getSession().isDisplayRelationshipLabel()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            tagText.append(" quoteIdentifiers=\"").append(getSession().isQuoteIdentifiers()).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        tagText.append(">"); //$NON-NLS-1$
        ioo.println(out, tagText.toString());
        
        ioo.indent++;
        savePlayPenComponents(out, pp);
        ioo.indent--;

        ioo.println(out, "</play-pen>"); //$NON-NLS-1$
    }
    
    private void savePlayPenComponents(PrintWriter out, PlayPen pp) {
        List<PlayPenComponent> ppcs = new ArrayList<PlayPenComponent>(); 
        ppcs.addAll(pp.getContentPane().getChildren());
        Collections.reverse(ppcs);
        
        // save the container panes.
        for (PlayPenComponent ppc : ppcs) {
            if (ppc instanceof TablePane) {
                TablePane tp = (TablePane) ppc;
                Point p = tp.getLocation();
                
                if (sqlObjectSaveIdMap.get(tp.getModel()) == null) {
                    logger.error("Play pen tried to save a table pane at " + tp.getX() + ", " + tp.getY() + " with the model " + tp.getModel() + " and reference id " + sqlObjectSaveIdMap.get(tp.getModel()) + "." +
                    		"\nSaving a table pane with a null reference will cause an NPE on loading.");
                    throw new NullPointerException("Play pen table is saving a null reference.");
                }
                
                Color bgColor = tp.getBackgroundColor();
                String bgColorString = String.format("0x%02x%02x%02x", bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()); //$NON-NLS-1$
                Color fgColor = tp.getForegroundColor();
                String fgColorString = String.format("0x%02x%02x%02x", fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()); //$NON-NLS-1$
                
                ioo.println(out, "<table-pane table-ref="+quote(sqlObjectSaveIdMap.get(tp.getModel()))  //$NON-NLS-1$
                        +" x=\""+p.x+"\" y=\""+p.y+"\" bgColor="+ quote(bgColorString) + " fgColor=" + quote(fgColorString) + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        " rounded=\"" + tp.isRounded() + "\" dashed=\"" + tp.isDashed() + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (pm != null) {
                    pm.setProgress(++progress);
                }
            } else if (ppc instanceof CubePane) {
                CubePane cp = (CubePane) ppc;
                Point p = cp.getLocation();

                String modelId = olapObjectSaveIdMap.get(cp.getModel());
                String paneId = "CP" + olapPaneSaveIdMap.size(); //$NON-NLS-1$
                
                ioo.println(out, "<cube-pane id=" + quote(paneId) + " model-ref=" + quote(modelId) //$NON-NLS-1$ //$NON-NLS-2$
                        + " x=\"" + p.x + "\" y=\"" + p.y+"\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                olapPaneSaveIdMap.put(cp, paneId);
            } else if (ppc instanceof DimensionPane) {
                DimensionPane dp = (DimensionPane) ppc;
                Point p = dp.getLocation();
                
                String paneId = "DP" + olapPaneSaveIdMap.size();
                String modelId = olapObjectSaveIdMap.get(dp.getModel());
                
                ioo.println(out, "<dimension-pane id=" + quote(paneId) + " model-ref=" + quote(modelId) //$NON-NLS-1$ //$NON-NLS-2$
                        + " x=\"" + p.x + "\" y=\"" + p.y + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                olapPaneSaveIdMap.put(dp, paneId);
            } else if (ppc instanceof VirtualCubePane) {
                VirtualCubePane vcp = (VirtualCubePane) ppc;
                Point p = vcp.getLocation();
                
                String paneId = "VCP" + olapPaneSaveIdMap.size();
                String modelId = olapObjectSaveIdMap.get(vcp.getModel());
                
                ioo.println(out, "<virtual-cube-pane id=" + quote(paneId) + " model-ref=" + quote(modelId) //$NON-NLS-1$ //$NON-NLS-2$
                        + " x=\"" + p.x + "\" y=\"" + p.y + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                olapPaneSaveIdMap.put(vcp, paneId);
            } else if (ppc instanceof ContainerPane<?, ?>) {
                logger.warn("Skipping unhandled playpen component: " + ppc); //$NON-NLS-1$
            }
        }
        
        // save the connectors.
        for (PlayPenComponent ppc : ppcs) {
            if (ppc instanceof Relationship) {
                Relationship r = (Relationship) ppc;
                
                Color relationshipLineColor = r.getForegroundColor();                
                String rColorString = String.format("0x%02x%02x%02x", relationshipLineColor.getRed(), relationshipLineColor.getGreen(), relationshipLineColor.getBlue()); //$NON-NLS-1$
                
                ioo.println(out, "<table-link relationship-ref="+quote(sqlObjectSaveIdMap.get(r.getModel())) //$NON-NLS-1$
                        +" pkConnection=\""+r.getPkConnection()+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                        +" fkConnection=\""+r.getFkConnection()+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                        +" rLineColor="+quote(rColorString) //$NON-NLS-1$
                        +" pkLabelText="+quote(r.getTextForParentLabel()) //$NON-NLS-1$
                        +" fkLabelText="+quote(r.getTextForChildLabel()) //$NON-NLS-1$
                        +" orientation=\"" + r.getOrientation() + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (ppc instanceof UsageComponent) {
                UsageComponent usageComp = (UsageComponent) ppc;
                String modelId = olapObjectSaveIdMap.get(usageComp.getModel());
                String pane1Id = olapPaneSaveIdMap.get(usageComp.getPane1());
                String pane2Id = olapPaneSaveIdMap.get(usageComp.getPane2());
                
                ioo.println(out, "<usage-comp model-ref=" + quote(modelId) + //$NON-NLS-1$
                        " pane1-ref=" + quote(pane1Id) + " pane2-ref=" + quote(pane2Id) + "/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else if (ppc instanceof ContainerPane<?, ?>) {
                // do nothing, already handled.
            } else {
                logger.warn("Skipping unhandled playpen component: " + ppc); //$NON-NLS-1$
            }
        }
    }

    /**
     * Save all of the profiling information.
     * @param out
     */
    private void saveProfiles(PrintWriter out) {
        ProfileManager profmgr = getSession().getProfileManager();
        ioo.println(out, "<profiles topNCount=\""+profmgr.getDefaultProfileSettings().getTopNCount()+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.indent++;

        List<TableProfileResult> tableResults = profmgr.getResults();
        
        for (TableProfileResult tableResult : tableResults) {
            String profiledObjectId = sqlObjectSaveIdMap.get(tableResult.getProfiledObject());
            if (profiledObjectId == null) {
                UserPrompter prompter = getSession().createUserPrompter("Cannot save profile for table " + 
                        tableResult.getProfiledObject().getName() + ", skipping this profile and continuing save.", 
                        UserPromptType.MESSAGE, UserPromptOptions.OK, UserPromptResponse.OK, null, "OK");
                prompter.promptUser();
                continue;
            }
            ioo.print(out, "<table-profile-result"); //$NON-NLS-1$
            printCommonItems(out, tableResult, profiledObjectId);
            ioo.niprint(out, " rowCount=\"" + tableResult.getRowCount() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            ioo.niprint(out, " UUID=\"" + tableResult.getUUID() + "\"");
            ioo.niprintln(out, ">"); //$NON-NLS-1$
            ioo.indent++;
            
            List<ColumnProfileResult> columnProfileResults = tableResult.getColumnProfileResults();
            for (ColumnProfileResult cpr : columnProfileResults) {
                String profiledColumnObjectId = sqlObjectSaveIdMap.get(cpr.getProfiledObject());
                if (profiledColumnObjectId == null) {
                    UserPrompter prompter = getSession().createUserPrompter("Cannot save profile for column " + 
                            cpr.getProfiledObject().getName() + ", skipping this profile and continuing save.", 
                            UserPromptType.MESSAGE, UserPromptOptions.OK, UserPromptResponse.OK, null, "OK");
                    prompter.promptUser();
                    continue;
                }
                ioo.print(out, "<column-profile-result");
                printCommonItems(out, cpr, profiledColumnObjectId);
                ioo.niprint(out, " avgLength=\"" + cpr.getAvgLength() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

                ioo.niprint(out, " minLength=\"" + cpr.getMinLength() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.niprint(out, " maxLength=\"" + cpr.getMaxLength() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.niprint(out, " nullCount=\"" + cpr.getNullCount() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.niprint(out, " distinctValueCount=\"" + cpr.getDistinctValueCount() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.niprint(out, " UUID=\"" + cpr.getUUID() + "\"");
                ioo.niprintln(out, ">"); //$NON-NLS-1$

                ioo.indent++;

                if ( cpr.getAvgValue() != null ) {
                    ioo.println(out, "<avgValue type=\"" + //$NON-NLS-1$
                            cpr.getAvgValue().getClass().getName() +
                            "\" value=\""+ //$NON-NLS-1$
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getAvgValue())) +
                            "\"/>" ); //$NON-NLS-1$
                }
                if ( cpr.getMaxValue() != null ) {
                    ioo.println(out, "<maxValue type=\"" + //$NON-NLS-1$
                            cpr.getMaxValue().getClass().getName() +
                            "\" value=\""+ //$NON-NLS-1$
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getMaxValue())) +
                            "\"/>" ); //$NON-NLS-1$
                }
                if ( cpr.getMinValue() != null ) {
                    ioo.println(out, "<minValue type=\"" + //$NON-NLS-1$
                            cpr.getMinValue().getClass().getName() +
                            "\" value=\""+ //$NON-NLS-1$
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getMinValue())) +
                            "\"/>" ); //$NON-NLS-1$
                }

                List<ColumnValueCount> valueCount = cpr.getValueCount();
                if (valueCount != null) {
                    for (ColumnValueCount count : valueCount) {
                        ioo.println(out, "<topNvalue count=\""+ //$NON-NLS-1$
                                count.getCount()+
                                "\" type=\"" + //$NON-NLS-1$
                                (count.getValue() == null ? "" : count.getValue().getClass().getName()) + //$NON-NLS-1$
                                "\" value=\""+ //$NON-NLS-1$
                                SQLPowerUtils.escapeXML(String.valueOf(count.getValue()))+
                                "\" percent=\"" +  //$NON-NLS-1$
                                count.getPercent() + 
                                "\" otherValues=\"" + 
                                Boolean.toString(count.isOtherValues()) + "\"/>" ); //$NON-NLS-1$
                    }
                }
                ioo.indent--;

                ioo.println(out, "</column-profile-result>"); //$NON-NLS-1$
            }
            ioo.indent--;
            ioo.println(out, "</table-profile-result>");
        }
        ioo.println(out, "</profiles>"); //$NON-NLS-1$
        ioo.indent--;
    }

    private void printCommonItems(PrintWriter out, ProfileResult<?> profileResult, String profiledObjectId) {
        ioo.niprint(out, " ref-id=\"" + profiledObjectId + "\"" + //$NON-NLS-1$ //$NON-NLS-2$
                " createStartTime=\""+profileResult.getCreateStartTime()+"\"" + //$NON-NLS-1$ //$NON-NLS-2$
                " createEndTime=\""+profileResult.getCreateEndTime()+"\"" + //$NON-NLS-1$ //$NON-NLS-2$
                " exception=\""+(profileResult.getException() == null ? "false" : "true")+"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if (profileResult.getException() != null) {
            ioo.niprint(out, " exception-type=\""+SQLPowerUtils.escapeXML(profileResult.getException().getClass().getName())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
            ioo.niprint(out, " exception-message=\""+SQLPowerUtils.escapeXML(profileResult.getException().getMessage())+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * Creates an XML element describing the given SQLObject and
     * writes it to the <code>out</code> PrintWriter.
     *
     * <p>Design notes: Attribute names that are straight property
     * contents (such as name or defaultValue) are chosen so that
     * automatic JavaBeans population of object properties is
     * possible.  For the same reasons, attributes that need
     * non-automatic population (such as reference properties like
     * pkColumn) are named to purposely disable automatic JavaBeans
     * property setting.  In the pkColumn example, the XML attribute
     * name would be pk-column-ref.  Special code in the load routine
     * is responsible for deferencing the attribute and setting the
     * property manually.
     */
    private void saveSQLObject(PrintWriter out, SQLObject o) throws IOException {
        if (o instanceof SQLRelationship.SQLImportedKey) {
            // ImportedKeys only store the fkTable for a SQLRelationship, which
            // is saved with the relationship for forward compatability.
            return;
        }
        String id = sqlObjectSaveIdMap.get(o);
        if (id != null) {
            ioo.println(out, "<reference ref-id=\""+SQLPowerUtils.escapeXML(id)+"\" />"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        String type;
        Map<String,Object> propNames = new TreeMap<String,Object>();

        // properties of all SQLObject types
        propNames.put("physicalName", o.getPhysicalName()); //$NON-NLS-1$
        propNames.put("name", o.getName()); // note: there was no name attrib for SQLDatabase, SQLRelationship.ColumnMapping, and SQLExceptionNode //$NON-NLS-1$
        propNames.put("UUID", o.getUUID());
        
        if (!o.getChildrenInaccessibleReasons().isEmpty()) {
            //Only storing the top exception to prevent file format changes
            //Only the SQLTable should have multiple children inaccessible reasons.
            Throwable topException = o.getChildrenInaccessibleReason(SQLObject.class);
            propNames.put("sql-exception", topException); //$NON-NLS-1$
        }

        if (o instanceof SQLDatabase) {
            id = "DB"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "database"; //$NON-NLS-1$
            propNames.put("dbcs-ref", dbcsSaveIdMap.get(((SQLDatabase) o).getDataSource())); //$NON-NLS-1$
        } else if (o instanceof SQLCatalog) {
            id = "CAT"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "catalog"; //$NON-NLS-1$
            propNames.put("nativeTerm", ((SQLCatalog) o).getNativeTerm()); //$NON-NLS-1$
        } else if (o instanceof SQLSchema) {
            id = "SCH"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "schema"; //$NON-NLS-1$
            propNames.put("nativeTerm", ((SQLSchema) o).getNativeTerm()); //$NON-NLS-1$
        } else if (o instanceof SQLTable) {
            id = "TAB"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "table"; //$NON-NLS-1$
            propNames.put("objectType", ((SQLTable) o).getObjectType()); //$NON-NLS-1$
            // don't save primary key name. It is a propery of the PK index, not the table.
            if (pm != null) {
                pm.setProgress(++progress);
            }
        } else if (o instanceof SQLColumn) {
            id = "COL"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "column"; //$NON-NLS-1$
            SQLColumn sourceCol = ((SQLColumn) o).getSourceColumn();
            if (sourceCol != null) {
                propNames.put("source-column-ref", sqlObjectSaveIdMap.get(sourceCol)); //$NON-NLS-1$
            }
            UserDefinedSQLType userDefinedSQLType = ((SQLColumn) o).getUserDefinedSQLType();
            if (userDefinedSQLType.getUpstreamType() != null) {
                propNames.put("userDefinedTypeUUID", userDefinedSQLType.getUpstreamType().getUUID());
            }
            propNames.put("type", new Integer(((SQLColumn) o).getType())); //$NON-NLS-1$
            propNames.put("sourceDataTypeName", ((SQLColumn) o).getSourceDataTypeName()); //$NON-NLS-1$
            propNames.put("scale", new Integer(((SQLColumn) o).getScale())); //$NON-NLS-1$
            propNames.put("precision", new Integer(((SQLColumn) o).getPrecision())); //$NON-NLS-1$
            propNames.put("nullable", new Integer(((SQLColumn) o).getNullable())); //$NON-NLS-1$
            propNames.put("defaultValue", ((SQLColumn) o).getDefaultValue()); //$NON-NLS-1$
            propNames.put("primaryKeySeq", ((SQLColumn) o).isPrimaryKey() ? ((SQLColumn) o).getParent().getChildrenWithoutPopulating(SQLColumn.class).indexOf(o) : null); //$NON-NLS-1$
            propNames.put("autoIncrement", Boolean.valueOf(((SQLColumn) o).isAutoIncrement())); //$NON-NLS-1$
            propNames.put("referenceCount", new Integer(((SQLColumn)o).getReferenceCount())); //$NON-NLS-1$
            if (((SQLColumn) o).isAutoIncrementSequenceNameSet()) {
                propNames.put("autoIncrementSequenceName", ((SQLColumn) o).getAutoIncrementSequenceName()); //$NON-NLS-1$
            }
        } else if (o instanceof SQLRelationship) {
            id = "REL"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "relationship"; //$NON-NLS-1$
            propNames.put("pk-table-ref", sqlObjectSaveIdMap.get(((SQLRelationship) o).getPkTable())); //$NON-NLS-1$
            propNames.put("fk-table-ref", sqlObjectSaveIdMap.get(((SQLRelationship) o).getFkTable())); //$NON-NLS-1$
            propNames.put("updateRule", new Integer(((SQLRelationship) o).getUpdateRule().getCode())); //$NON-NLS-1$
            propNames.put("deleteRule", new Integer(((SQLRelationship) o).getDeleteRule().getCode())); //$NON-NLS-1$
            propNames.put("deferrability", new Integer(((SQLRelationship) o).getDeferrability().getCode())); //$NON-NLS-1$
            propNames.put("pkCardinality", new Integer(((SQLRelationship) o).getPkCardinality())); //$NON-NLS-1$
            propNames.put("fkCardinality", new Integer(((SQLRelationship) o).getFkCardinality())); //$NON-NLS-1$
            propNames.put("identifying", Boolean.valueOf(((SQLRelationship) o).isIdentifying())); //$NON-NLS-1$
        } else if (o instanceof SQLRelationship.ColumnMapping) {
            id = "CMP"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "column-mapping"; //$NON-NLS-1$
            propNames.put("pk-column-ref", sqlObjectSaveIdMap.get(((SQLRelationship.ColumnMapping) o).getPkColumn())); //$NON-NLS-1$
            propNames.put("fk-column-ref", sqlObjectSaveIdMap.get(((SQLRelationship.ColumnMapping) o).getFkColumn())); //$NON-NLS-1$
            propNames.put("fk-col-name", ((SQLRelationship.ColumnMapping) o).getFkColName()); //$NON-NLS-1$
            propNames.put("fk-table", sqlObjectSaveIdMap.get(((SQLRelationship.ColumnMapping) o).getFkTable())); //$NON-NLS-1$
        } else if (o instanceof SQLIndex) {
            id = "IDX"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "index"; //$NON-NLS-1$
            SQLIndex index = (SQLIndex) o;
            propNames.put("unique", index.isUnique()); //$NON-NLS-1$
            propNames.put("qualifier", index.getQualifier()); //$NON-NLS-1$
            propNames.put("clustered", index.isClustered()); //$NON-NLS-1$
            /*
             * Normally, hyphenated names are used to stop
             * BeanUtils from auto-populating a field. However in this case,
             * we are going to keep the hyphen (and break the normal scheme)
             * in order to preserve backward compatibility.
             */
            propNames.put("index-type", index.getType()); //$NON-NLS-1$
            propNames.put("primaryKeyIndex", index.isPrimaryKeyIndex()); //$NON-NLS-1$
            propNames.put("filterCondition", index.getFilterCondition()); //$NON-NLS-1$
        } else if (o instanceof SQLIndex.Column) {
            id = "IDC"+sqlObjectSaveIdMap.size(); //$NON-NLS-1$
            type = "index-column"; //$NON-NLS-1$
            SQLIndex.Column col = (SQLIndex.Column) o;
            if (col.getColumn() != null) {
                propNames.put("column-ref", sqlObjectSaveIdMap.get(col.getColumn())); //$NON-NLS-1$
            }
            propNames.put("ascendingOrDescending", col.getAscendingOrDescending().name()); //$NON-NLS-1$
        } else {
            throw new UnsupportedOperationException("Whoops, the SQLObject type " //$NON-NLS-1$
                    +o.getClass().getName()+" is not supported!"); //$NON-NLS-1$
        }
        
        sqlObjectSaveIdMap.put(o, id);
        if(logger.isDebugEnabled()) {
            // use this for debugging duplicate object problems
            ioo.print(out, "<"+type+" hashCode=\""+o.hashCode()+"\" id=\""+quote(id)+"\" ");
        } else {
            ioo.print(out, "<"+type+" id="+quote(id)+" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if ( (!getSession().isSavingEntireSource()) && (!o.isPopulated()) ) {
            ioo.niprint(out, "populated=\"false\" "); //$NON-NLS-1$
        } else {
            ioo.niprint(out, "populated=\"true\" "); //$NON-NLS-1$
        }

        Iterator<String> props = propNames.keySet().iterator();
        while (props.hasNext()) {
            Object key = props.next();
            Object value = propNames.get(key);
            if (value != null) {
                ioo.niprint(out, key+"="+quote(value.toString())+" "); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (o.allowsChildren()) {
            ioo.niprintln(out, ">"); //$NON-NLS-1$
            Iterator<? extends SQLObject> children;
            if (getSession().isSavingEntireSource()) {
                children = o.getChildren().iterator();
            } else {
                children = o.getChildrenWithoutPopulating().iterator();
            }
            ioo.indent++;
            
            //XXX Adding the folders back into the saved file format. This way the save file version
            //does not need to change. At some point when the save version increases in a major version
            //number we may want to remove this.
            //Note: columns type = 1, imported keys type = 2, exported keys type = 3, indicies type = 4
            SQLObject lastChild = null;
            String exportedKeysFolder = null;
            String importedKeysFolder = null;
            String indicesFolder = null;
            if (o instanceof SQLTable) {
                SQLTable table = (SQLTable) o;
                ioo.println(out, "<remarks>" + SQLPowerUtils.escapeXML(table.getRemarks()) + "</remarks>");
                String exception;
                if (table.getChildrenInaccessibleReason(SQLColumn.class) != null) {
                    exception = "sql-exception=\"" + 
                    table.getChildrenInaccessibleReason(SQLColumn.class) + "\" ";
                } else {
                    exception = "";
                }
                ioo.println(out, "<folder id=\"FOL" + id + "1\" populated=\"" + 
                        table.isColumnsPopulated() + "\" name=\"Columns\" " +
                        		"physicalName=\"Columns\" " + exception + "type=\"1\">");
                ioo.indent++;
                
                if (table.getChildrenInaccessibleReason(SQLImportedKey.class) != null) {
                    exception = "sql-exception=\"" + 
                    table.getChildrenInaccessibleReason(SQLImportedKey.class) + "\" ";
                } else {
                    exception = "";
                }
                importedKeysFolder = "<folder id=\"FOL" + id + "2\" populated=\"" + 
                    table.isImportedKeysPopulated() + "\" name=\"Imported Keys\" " +
                    "physicalName=\"Imported Keys\" " + exception + "type=\"2\">";
                
                if (table.getChildrenInaccessibleReason(SQLRelationship.class) != null) {
                    exception = "sql-exception=\"" + 
                    table.getChildrenInaccessibleReason(SQLRelationship.class) + "\" ";
                } else {
                    exception = "";
                }
                exportedKeysFolder = "<folder id=\"FOL" + id + "3\" populated=\"" + 
                    table.isExportedKeysPopulated() + "\" name=\"Exported Keys\" " +
                    "physicalName=\"Exported Keys\" " + exception + "type=\"3\">";
                
                if (table.getChildrenInaccessibleReason(SQLIndex.class) != null) {
                    exception = "sql-exception=\"" + 
                    table.getChildrenInaccessibleReason(SQLIndex.class) + "\" ";
                } else {
                    exception = "";
                }
                indicesFolder = "<folder id=\"FOL" + id + "4\" populated=\"" + 
                    table.isIndicesPopulated() + "\" name=\"Indices\" " +
                    "physicalName=\"Indices\" " + exception + "type=\"4\">";
            } else if (o instanceof SQLColumn) {
                ioo.println(out, "<remarks>" + SQLPowerUtils.escapeXML(((SQLColumn) o).getRemarks()) + "</remarks>");
            }
            while (children.hasNext()) {
                SQLObject child = (SQLObject) children.next();
                //another part of the XXX
                if (o instanceof SQLTable && lastChild instanceof SQLColumn 
                        && !(child instanceof SQLColumn)) {
                    ioo.println(out, "</folder>");
                    ioo.println(out, exportedKeysFolder);
                    ioo.println(out, "</folder>");
                    ioo.println(out, importedKeysFolder);
                    ioo.println(out, "</folder>");
                    ioo.println(out, indicesFolder);
                }
                if (!((child instanceof SQLRelationship)
                        || (child instanceof UserDefinedSQLType)
                        || (child instanceof SQLTypePhysicalProperties))) {
                    saveSQLObject(out, child);
                }
                lastChild = child;
            }
            //Second part of the above XXX
            if (o instanceof SQLTable) {
                ioo.indent--;
                if (lastChild == null || lastChild instanceof SQLColumn) {
                    ioo.println(out, "</folder>");
                    ioo.println(out, exportedKeysFolder);
                    ioo.println(out, "</folder>");
                    ioo.println(out, importedKeysFolder);
                    ioo.println(out, "</folder>");
                    ioo.println(out, indicesFolder);
                }
                ioo.println(out, "</folder>");
            }
            
            if (o instanceof SQLDatabase) {
                saveRelationships(out, (SQLDatabase) o);
            }
            ioo.indent--;
            ioo.println(out, "</"+type+">"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            ioo.niprintln(out, "/>"); //$NON-NLS-1$
        }
    }
    
    private void savePrintSettings(PrintWriter out, PrintSettings settings) {
        StringBuilder tagText = new StringBuilder();
        tagText.append("<print-settings ");
        
        if (settings.getPrinterName() != null) {
            tagText.append("printerName=\"" + settings.getPrinterName() + "\" ");
        }
        tagText.append("numCopies=\"" + settings.getNumCopies() + "\" ");
        tagText.append("zoom=\"" + settings.getZoom() + "\" ");
        tagText.append("pageNumbersPrinted=\"" + Boolean.toString(settings.isPageNumbersPrinted()) + "\" ");
        tagText.append("orientation=\"" + settings.getOrientation() + "\" ");
        tagText.append("paperWidth=\"" + settings.getPaperWidth() + "\" ");
        tagText.append("paperHeight=\"" + settings.getPaperHeight() + "\" ");
        tagText.append("leftBorder=\"" + settings.getLeftBorder() + "\" ");
        tagText.append("rightBorder=\"" + settings.getRightBorder() + "\" ");
        tagText.append("topBorder=\"" + settings.getTopBorder() + "\" ");
        tagText.append("bottomBorder=\"" + settings.getBottomBorder() + "\" ");
        
        tagText.append("/>"); //$NON-NLS-1$
        ioo.println(out, tagText.toString());
    }

    private String quote(String str) {
        return "\""+SQLPowerUtils.escapeXML(str)+"\""; //$NON-NLS-1$ //$NON-NLS-2$
    }
    // ------------------- accessors and mutators ---------------------
    
    @Override
    protected ArchitectSwingSession getSession() {
        return (ArchitectSwingSession) session;
    }
}