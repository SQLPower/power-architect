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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ProgressMonitor;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.CoreProject;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLExceptionNode;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.CompareDMSettings.SourceOrTargetSettings;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.SQLPowerUtils;
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
public class SwingUIProject extends CoreProject {
    
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

    private static final Logger logger = Logger.getLogger(SwingUIProject.class);

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
    public SwingUIProject(ArchitectSwingSession session) throws ArchitectException {
        super(session);
        
        if (session == null) {
            throw new NullPointerException("Null session is not allowed!"); //$NON-NLS-1$
        }
        this.session = session;
    }

    // ------------- READING THE PROJECT FILE ---------------

    public void load(InputStream in, DataSourceCollection dataSources) throws IOException, ArchitectException {
        super.load(in, dataSources);
        

        // TODO change this to load the undo history from a file
        getSession().getUndoManager().discardAllEdits();
    }

    protected Digester setupDigester() throws ParserConfigurationException, SAXException {
        Digester d = super.setupDigester();

        // the play pen
        PlayPenFactory ppFactory = new PlayPenFactory();
        d.addFactoryCreate("architect-project/play-pen", ppFactory); //$NON-NLS-1$
        
        //Sets the view point again in the case that the viewport was invalid in the factory
        d.addRule("architect-project/play-pen", new Rule() { //$NON-NLS-1$
            @Override
            public void end() throws Exception {
                super.end();
                getSession().getPlayPen().setInitialViewPosition();
            }
        });
        
        TablePaneFactory tablePaneFactory = new TablePaneFactory();
        d.addFactoryCreate("architect-project/play-pen/table-pane", tablePaneFactory); //$NON-NLS-1$
        // factory will add the tablepanes to the playpen

        PPRelationshipFactory ppRelationshipFactory = new PPRelationshipFactory();
        d.addFactoryCreate("architect-project/play-pen/table-link", ppRelationshipFactory); //$NON-NLS-1$

        CompareDMSettingFactory settingFactory = new CompareDMSettingFactory();
        d.addFactoryCreate("architect-project/compare-dm-settings", settingFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/compare-dm-settings"); //$NON-NLS-1$

        CompareDMStuffSettingFactory sourceStuffFactory = new CompareDMStuffSettingFactory(true);
        d.addFactoryCreate("architect-project/compare-dm-settings/source-stuff", sourceStuffFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/compare-dm-settings/source-stuff"); //$NON-NLS-1$

        CompareDMStuffSettingFactory targetStuffFactory = new CompareDMStuffSettingFactory(false);
        d.addFactoryCreate("architect-project/compare-dm-settings/target-stuff", targetStuffFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/compare-dm-settings/target-stuff"); //$NON-NLS-1$
        
        CreateKettleJobSettingsFactory ckjsFactory = new CreateKettleJobSettingsFactory();
        d.addFactoryCreate("architect-project/create-kettle-job-settings", ckjsFactory); //$NON-NLS-1$
        d.addSetProperties("architect-project/create-kettle-job-settings"); //$NON-NLS-1$

        return d;
    }
    
    

    private class PlayPenFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
        	String zoomLevel = attributes.getValue("zoom"); //$NON-NLS-1$
        	if (zoomLevel != null) {
        	    getSession().getPlayPen().setZoom(Double.parseDouble(zoomLevel));
        	}
        	
        	String viewportX = attributes.getValue("viewportX"); //$NON-NLS-1$
        	String viewportY = attributes.getValue("viewportY"); //$NON-NLS-1$
        	
        	if (viewportX != null && viewportY != null) {
        	    Point viewPoint = new Point(Integer.parseInt(viewportX), Integer.parseInt(viewportY));
        		getSession().getPlayPen().setViewPosition(viewPoint);
        	}
        	logger.debug("Viewport position is " + getSession().getPlayPen().getViewPosition()); //$NON-NLS-1$
        	
        	// default values in playpen are true
        	String showPrimary = attributes.getValue("showPrimary"); //$NON-NLS-1$
        	if (showPrimary != null) {
        	    getSession().setShowPrimary(Boolean.valueOf(showPrimary));
        	}
        	String showForeign = attributes.getValue("showForeign"); //$NON-NLS-1$
        	if (showForeign != null) {
        	    getSession().setShowForeign(Boolean.valueOf(showForeign));        	}
        	String showIndexed = attributes.getValue("showIndexed"); //$NON-NLS-1$
        	if (showIndexed != null) {
        	    getSession().setShowIndexed(Boolean.valueOf(showIndexed));
        	}
        	String showUnique = attributes.getValue("showUnique"); //$NON-NLS-1$
        	if (showUnique != null) {
        	    getSession().setShowUnique(Boolean.valueOf(showUnique));
        	}
        	String showTheRest = attributes.getValue("showTheRest"); //$NON-NLS-1$
        	if (showTheRest != null) {
        	    getSession().setShowTheRest(Boolean.valueOf(showTheRest));
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
            return getSession().getPlayPen();
        }
    }

    private class TablePaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            int x = Integer.parseInt(attributes.getValue("x")); //$NON-NLS-1$
            int y = Integer.parseInt(attributes.getValue("y")); //$NON-NLS-1$
            SQLTable tab = (SQLTable) objectIdMap.get(attributes.getValue("table-ref")); //$NON-NLS-1$
            TablePane tp = new TablePane(tab, getSession().getPlayPen());
            
            String bgColorString = attributes.getValue("bgColor"); //$NON-NLS-1$
            if (bgColorString != null) {
                Color bgColor = Color.decode(bgColorString);
                tp.setBackground(bgColor);
            }
            String fgColorString = attributes.getValue("fgColor"); //$NON-NLS-1$
            if (fgColorString != null) {
                Color fgColor = Color.decode(fgColorString);
                tp.setForeground(fgColor);
            }
            
            boolean rounded = "true".equals(attributes.getValue("rounded")); //$NON-NLS-1$ //$NON-NLS-2$
            tp.setRounded(rounded);
            
            boolean dashed = "true".equals(attributes.getValue("dashed")); //$NON-NLS-1$ //$NON-NLS-2$
            tp.setDashed(dashed);
                
            getSession().getPlayPen().addTablePane(tp, new Point(x, y));
            return tp;
        }
    }

    private class PPRelationshipFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Relationship r = null;
            try {
                SQLRelationship rel =
                    (SQLRelationship) objectIdMap.get(attributes.getValue("relationship-ref")); //$NON-NLS-1$
                r = new Relationship(getSession().getPlayPen(), rel);
                getSession().getPlayPen().addRelationship(r);
                r.updateUI();

                int pkx = Integer.parseInt(attributes.getValue("pk-x")); //$NON-NLS-1$
                int pky = Integer.parseInt(attributes.getValue("pk-y")); //$NON-NLS-1$
                int fkx = Integer.parseInt(attributes.getValue("fk-x")); //$NON-NLS-1$
                int fky = Integer.parseInt(attributes.getValue("fk-y")); //$NON-NLS-1$
                int orientation = Integer.parseInt(attributes.getValue("orientation")); //$NON-NLS-1$
                ((RelationshipUI) r.getUI()).setOrientation(orientation);
                r.setPkConnectionPoint(new Point(pkx, pky));
                r.setFkConnectionPoint(new Point(fkx, fky));
            } catch (ArchitectException e) {
                logger.error("Couldn't create relationship component", e); //$NON-NLS-1$
            } catch (NumberFormatException e) {
                logger.warn("Didn't set connection points because of integer parse error"); //$NON-NLS-1$
            } catch (NullPointerException e) {
                logger.debug("No pk/fk connection points specified in save file;" //$NON-NLS-1$
                        +" not setting custom connection points"); //$NON-NLS-1$
            }
            return r;
        }
    }


    private class CreateKettleJobSettingsFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) throws SQLException {
            return getSession().getKettleJob();
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
    
    // ------------- WRITING THE PROJECT FILE ---------------

    /**
     * Saves this project by writing an XML description of it to a temp file, then renaming.
     * The location of the file is determined by this project's <code>file</code> property.
     *
     * @param pm An optional progress monitor which will be initialised then updated
     * periodically during the save operation.  If you use a progress monitor, don't
     * invoke this method on the AWT event dispatch thread!
     */
    public void save(ProgressMonitor pm) throws IOException, ArchitectException {
        // write to temp file and then rename (this preserves old project file
        // when there's problems)
        if (file.exists() && !file.canWrite()) {
            // write problems with architect file will muck up the save process
            throw new ArchitectException(Messages.getString("SwingUIProject.errorSavingProject", file.getAbsolutePath())); //$NON-NLS-1$
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
            throw new ArchitectException(Messages.getString("SwingUIProject.cannotCreateOutputFile") + e, e); //$NON-NLS-1$
        }

        progress = 0;
        this.pm = pm;
        if (pm != null) {
            int pmMax = 0;
            pm.setMinimum(0);
            if (getSession().isSavingEntireSource()) {
                pmMax = ArchitectUtils.countTablesSnapshot((SQLObject) getSession().getSourceDatabases().getModel().getRoot());
            } else {
                pmMax = ArchitectUtils.countTables((SQLObject) getSession().getSourceDatabases().getModel().getRoot());
            }
            logger.error("Setting progress monitor maximum to "+pmMax); //$NON-NLS-1$
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
                throw new ArchitectException((
                        Messages.getString("SwingUIProject.couldNotRenameFile", tempFile.toString(), file.toString()))); //$NON-NLS-1$
            }
        }
        fstatus = tempFile.renameTo(file);
        if (!fstatus) {
            throw new ArchitectException((
                    Messages.getString("SwingUIProject.couldNotRenameTempFile", tempFile.toString(), file.toString()))); //$NON-NLS-1$
        }
        logger.debug("rename tempFile to current file: " + fstatus); //$NON-NLS-1$
    }

    XMLHelper ioo = new XMLHelper();
    
    /**
     * Do just the writing part of save, given a PrintWriter
     * @param out - the file to write to
     * @return True iff the save completed OK
     * @throws IOException
     * @throws ArchitectException
     */
    public void save(PrintWriter out, String encoding) throws IOException, ArchitectException {
        objectIdMap = new IdentityHashMap();
        dbcsIdMap = new HashMap();
        ioo.indent = 0;

        try {
            ioo.println(out, "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
            ioo.println(out, "<architect-project version=\"1.0\" appversion=\""+ArchitectVersion.APP_VERSION+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
            ioo.indent++;
            ioo.println(out, "<project-name>"+SQLPowerUtils.escapeXML(getSession().getName())+"</project-name>"); //$NON-NLS-1$ //$NON-NLS-2$
            saveDataSources(out);
            saveSourceDatabases(out);
            saveTargetDatabase(out);
            saveDDLGenerator(out);
            saveCompareDMSettings(out);
            saveCreateKettleJobSettings(out);
            savePlayPen(out);
            saveProfiles(out);
            ioo.indent--;
            ioo.println(out, "</architect-project>"); //$NON-NLS-1$
            setModified(false);
        } finally {
            if (out != null) out.close();
        }
    }
    
    public void save(OutputStream out, String encoding) throws IOException, ArchitectException {
        save(new PrintWriter(new OutputStreamWriter(out, encoding)), encoding);
    }

    private void saveDataSources(PrintWriter out) throws IOException, ArchitectException {
        // FIXME this needs work.  It should include everything we need in order to build
        //       the referenced parent type from scratch (except the jdbc driver path)
        //       and the code that loads a project should check if the referenced parent
        //       type exists.  If not, we need to create everything we can about the parent
        //       type, then show the driver manager gui and get the user to pick a jdbc driver file.
        ioo.println(out, "<project-data-sources>"); //$NON-NLS-1$
        ioo.indent++;
        int dsNum = 0;
        SQLObject dbTreeRoot = (SQLObject) getSession().getSourceDatabases().getModel().getRoot();
        Iterator it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = (SQLObject) it.next();
            SPDataSource ds = ((SQLDatabase) o).getDataSource();
            if (ds != null) {
                String id = (String) dbcsIdMap.get(ds);
                if (id == null) {
                    id = "DS"+dsNum; //$NON-NLS-1$
                    dbcsIdMap.put(ds, id);
                }
                ioo.println(out, "<data-source id=\""+SQLPowerUtils.escapeXML(id)+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.indent++;
                Iterator pit = ds.getPropertiesMap().entrySet().iterator();
                while (pit.hasNext()) {
                    Map.Entry ent = (Map.Entry) pit.next();
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
        ioo.println(out, ">"); //$NON-NLS-1$
        ioo.indent++;
        ioo.print(out, "<source-stuff"); //$NON-NLS-1$
        saveSourceOrTargetAttributes(out, getSession().getCompareDMSettings().getSourceSettings());
        ioo.print(out, "/>"); //$NON-NLS-1$
        ioo.print(out, "<target-stuff"); //$NON-NLS-1$
        saveSourceOrTargetAttributes(out, getSession().getCompareDMSettings().getTargetSettings());
        ioo.print(out, "/>"); //$NON-NLS-1$
        ioo.indent--;
        ioo.println(out, "</compare-dm-settings>"); //$NON-NLS-1$
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
     * @param out2
     */
    private void saveSourceDatabases(PrintWriter out) throws IOException, ArchitectException {
        ioo.println(out, "<source-databases>"); //$NON-NLS-1$
        ioo.indent++;
        SQLObject dbTreeRoot = (SQLObject) getSession().getSourceDatabases().getModel().getRoot();
        Iterator it = dbTreeRoot.getChildren().iterator();
        while (it.hasNext()) {
            SQLObject o = (SQLObject) it.next();
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
    private void saveRelationships(PrintWriter out, SQLDatabase db) throws ArchitectException, IOException {
        ioo.println(out, "<relationships>"); //$NON-NLS-1$
        ioo.indent++;
        Iterator it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveRelationshipsRecurse(out, (SQLObject) it.next());
        }
        ioo.indent--;
        ioo.println(out, "</relationships>"); //$NON-NLS-1$
    }

    /**
     * The recursive subroutine of saveRelationships.
     */
    private void saveRelationshipsRecurse(PrintWriter out, SQLObject o) throws ArchitectException, IOException {
        if ( (!getSession().isSavingEntireSource()) && (!o.isPopulated()) ) {
            return;
        } else if (o instanceof SQLRelationship) {
            saveSQLObject(out, o);
        } else if (o.allowsChildren()) {
            Iterator it = o.getChildren().iterator();
            while (it.hasNext()) {
                saveRelationshipsRecurse(out, (SQLObject) it.next());
            }
        }
    }

    private void saveTargetDatabase(PrintWriter out) throws IOException, ArchitectException {
        SQLDatabase db = (SQLDatabase) getSession().getTargetDatabase();
        ioo.println(out, "<target-database dbcs-ref="+ //$NON-NLS-1$
                quote(dbcsIdMap.get(db.getDataSource()).toString())+ ">"); //$NON-NLS-1$
        ioo.indent++;
        Iterator it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveSQLObject(out, (SQLObject) it.next());
        }
        saveRelationships(out, db);
        ioo.indent--;
        ioo.println(out, "</target-database>"); //$NON-NLS-1$
    }

    private void savePlayPen(PrintWriter out) throws IOException, ArchitectException {
        String relStyle = getSession().getRelationshipLinesDirect() ?
                RELATIONSHIP_STYLE_DIRECT : RELATIONSHIP_STYLE_RECTILINEAR;
        ioo.println(out, "<play-pen zoom=\"" + getSession().getPlayPen().getZoom() +  //$NON-NLS-1$
        		"\" viewportX=\"" + getSession().getPlayPen().getViewPosition().x +  //$NON-NLS-1$
        		"\" viewportY=\"" + getSession().getPlayPen().getViewPosition().y +  //$NON-NLS-1$
        		"\" relationship-style="+quote(relStyle) + //$NON-NLS-1$
        		" showPrimaryTag=\"" + getSession().isShowPkTag() + //$NON-NLS-1$
        		"\" showForeignTag=\"" + getSession().isShowFkTag() + //$NON-NLS-1$
        		"\" showAlternateTag=\"" + getSession().isShowAkTag() + //$NON-NLS-1$
        		"\" showPrimary=\"" + getSession().isShowPrimary() + //$NON-NLS-1$
        		"\" showForeign=\"" + getSession().isShowForeign() + //$NON-NLS-1$
        		"\" showIndexed=\"" + getSession().isShowIndexed() + //$NON-NLS-1$
        		"\" showUnique=\"" + getSession().isShowUnique() + //$NON-NLS-1$
        		"\" showTheRest=\"" + getSession().isShowTheRest() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        ioo.indent++;
        for(int i = getSession().getPlayPen().getTablePanes().size()-1; i>= 0; i--) {
            TablePane tp = getSession().getPlayPen().getTablePanes().get(i);
            Point p = tp.getLocation();
            
            Color bgColor = tp.getBackground();
            String bgColorString = String.format("0x%02x%02x%02x", bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()); //$NON-NLS-1$
            Color fgColor = tp.getForeground();
            String fgColorString = String.format("0x%02x%02x%02x", fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()); //$NON-NLS-1$
            
            ioo.println(out, "<table-pane table-ref="+quote(objectIdMap.get(tp.getModel()).toString())+"" //$NON-NLS-1$ //$NON-NLS-2$
                    +" x=\""+p.x+"\" y=\""+p.y+"\" bgColor=\""+bgColorString+"\" fgColor=\""+fgColorString+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    "\" rounded=\"" + tp.isRounded() + "\" dashed=\"" + tp.isDashed() + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (pm != null) {
                pm.setProgress(++progress);
            }
        }

        Iterator it = getSession().getPlayPen().getRelationships().iterator();
        while (it.hasNext()) {
            Relationship r = (Relationship) it.next();
            ioo.println(out, "<table-link relationship-ref="+quote(objectIdMap.get(r.getModel()).toString()) //$NON-NLS-1$
                    +" pk-x=\""+r.getPkConnectionPoint().x+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                    +" pk-y=\""+r.getPkConnectionPoint().y+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                    +" fk-x=\""+r.getFkConnectionPoint().x+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                    +" fk-y=\""+r.getFkConnectionPoint().y+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                    +" orientation=\"" + ((RelationshipUI)r.getUI()).getOrientation() + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ioo.indent--;
        ioo.println(out, "</play-pen>"); //$NON-NLS-1$
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
            printCommonItems(out, tableResult);
            ioo.print(out, " rowCount=\"" + tableResult.getRowCount() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            ioo.niprintln(out, "/>"); //$NON-NLS-1$
            
            List<ColumnProfileResult> columnProfileResults = tableResult.getColumnProfileResults();
            for (ColumnProfileResult cpr : columnProfileResults) {
                printCommonItems(out, cpr);
                ioo.niprint(out, " avgLength=\"" + cpr.getAvgLength() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

                ioo.niprint(out, " minLength=\"" + cpr.getMinLength() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.niprint(out, " maxLength=\"" + cpr.getMaxLength() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.niprint(out, " nullCount=\"" + cpr.getNullCount() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                ioo.niprint(out, " distinctValueCount=\"" + cpr.getDistinctValueCount() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
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
                                count.getPercent() + "\"/>" ); //$NON-NLS-1$
                    }
                }
                ioo.indent--;

                ioo.println(out, "</profile-result>"); //$NON-NLS-1$
            }
        }
        ioo.println(out, "</profiles>"); //$NON-NLS-1$
        ioo.indent--;
    }

    private void printCommonItems(PrintWriter out, ProfileResult profileResult) {
        SQLObject profiledObject = profileResult.getProfiledObject();
        ioo.print(out, "<profile-result ref-id=\""+objectIdMap.get(profiledObject)+"\"" + //$NON-NLS-1$ //$NON-NLS-2$
                " type=\"" + profileResult.getClass().getName() + "\"" + //$NON-NLS-1$ //$NON-NLS-2$
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
    private void saveSQLObject(PrintWriter out, SQLObject o) throws IOException, ArchitectException {
        String id = (String) objectIdMap.get(o);
        if (id != null) {
            ioo.println(out, "<reference ref-id=\""+SQLPowerUtils.escapeXML(id)+"\" />"); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        String type;
        Map<String,Object> propNames = new TreeMap<String,Object>();

        // properties of all SQLObject types
        propNames.put("physicalName", o.getPhysicalName()); //$NON-NLS-1$
        propNames.put("name", o.getName()); // note: there was no name attrib for SQLDatabase, SQLRelationship.ColumnMapping, and SQLExceptionNode //$NON-NLS-1$

        if (o instanceof SQLDatabase) {
            id = "DB"+objectIdMap.size(); //$NON-NLS-1$
            type = "database"; //$NON-NLS-1$
            propNames.put("dbcs-ref", dbcsIdMap.get(((SQLDatabase) o).getDataSource())); //$NON-NLS-1$
        } else if (o instanceof SQLCatalog) {
            id = "CAT"+objectIdMap.size(); //$NON-NLS-1$
            type = "catalog"; //$NON-NLS-1$
            propNames.put("nativeTerm", ((SQLCatalog) o).getNativeTerm()); //$NON-NLS-1$
        } else if (o instanceof SQLSchema) {
            id = "SCH"+objectIdMap.size(); //$NON-NLS-1$
            type = "schema"; //$NON-NLS-1$
            propNames.put("nativeTerm", ((SQLSchema) o).getNativeTerm()); //$NON-NLS-1$
        } else if (o instanceof SQLTable) {
            id = "TAB"+objectIdMap.size(); //$NON-NLS-1$
            type = "table"; //$NON-NLS-1$
            propNames.put("remarks", ((SQLTable) o).getRemarks()); //$NON-NLS-1$
            propNames.put("objectType", ((SQLTable) o).getObjectType()); //$NON-NLS-1$
            // don't save primary key name. It is a propery of the PK index, not the table.
            if (pm != null) {
                pm.setProgress(++progress);
            }
        } else if (o instanceof SQLTable.Folder) {
            id = "FOL"+objectIdMap.size(); //$NON-NLS-1$
            type = "folder"; //$NON-NLS-1$
            propNames.put("type", new Integer(((SQLTable.Folder) o).getType())); //$NON-NLS-1$
        } else if (o instanceof SQLColumn) {
            id = "COL"+objectIdMap.size(); //$NON-NLS-1$
            type = "column"; //$NON-NLS-1$
            SQLColumn sourceCol = ((SQLColumn) o).getSourceColumn();
            if (sourceCol != null) {
                propNames.put("source-column-ref", objectIdMap.get(sourceCol)); //$NON-NLS-1$
            }
            propNames.put("type", new Integer(((SQLColumn) o).getType())); //$NON-NLS-1$
            propNames.put("sourceDataTypeName", ((SQLColumn) o).getSourceDataTypeName()); //$NON-NLS-1$
            propNames.put("scale", new Integer(((SQLColumn) o).getScale())); //$NON-NLS-1$
            propNames.put("precision", new Integer(((SQLColumn) o).getPrecision())); //$NON-NLS-1$
            propNames.put("nullable", new Integer(((SQLColumn) o).getNullable())); //$NON-NLS-1$
            propNames.put("remarks", ((SQLColumn) o).getRemarks()); //$NON-NLS-1$
            propNames.put("defaultValue", ((SQLColumn) o).getDefaultValue()); //$NON-NLS-1$
            propNames.put("primaryKeySeq", ((SQLColumn) o).getPrimaryKeySeq()); //$NON-NLS-1$
            propNames.put("autoIncrement", new Boolean(((SQLColumn) o).isAutoIncrement())); //$NON-NLS-1$
            propNames.put("referenceCount", new Integer(((SQLColumn)o).getReferenceCount())); //$NON-NLS-1$
            if (((SQLColumn) o).isAutoIncrementSequenceNameSet()) {
                propNames.put("autoIncrementSequenceName", ((SQLColumn) o).getAutoIncrementSequenceName()); //$NON-NLS-1$
            }
        } else if (o instanceof SQLRelationship) {
            id = "REL"+objectIdMap.size(); //$NON-NLS-1$
            type = "relationship"; //$NON-NLS-1$
            propNames.put("pk-table-ref", objectIdMap.get(((SQLRelationship) o).getPkTable())); //$NON-NLS-1$
            propNames.put("fk-table-ref", objectIdMap.get(((SQLRelationship) o).getFkTable())); //$NON-NLS-1$
            propNames.put("updateRule", new Integer(((SQLRelationship) o).getUpdateRule())); //$NON-NLS-1$
            propNames.put("deleteRule", new Integer(((SQLRelationship) o).getDeleteRule())); //$NON-NLS-1$
            propNames.put("deferrability", new Integer(((SQLRelationship) o).getDeferrability().getCode())); //$NON-NLS-1$
            propNames.put("pkCardinality", new Integer(((SQLRelationship) o).getPkCardinality())); //$NON-NLS-1$
            propNames.put("fkCardinality", new Integer(((SQLRelationship) o).getFkCardinality())); //$NON-NLS-1$
            propNames.put("identifying", new Boolean(((SQLRelationship) o).isIdentifying())); //$NON-NLS-1$
        } else if (o instanceof SQLRelationship.ColumnMapping) {
            id = "CMP"+objectIdMap.size(); //$NON-NLS-1$
            type = "column-mapping"; //$NON-NLS-1$
            propNames.put("pk-column-ref", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getPkColumn())); //$NON-NLS-1$
            propNames.put("fk-column-ref", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getFkColumn())); //$NON-NLS-1$
        } else if (o instanceof SQLExceptionNode) {
            id = "EXC"+objectIdMap.size(); //$NON-NLS-1$
            type = "sql-exception"; //$NON-NLS-1$
            propNames.put("message", ((SQLExceptionNode) o).getMessage()); //$NON-NLS-1$
        } else if (o instanceof SQLIndex) {
            id = "IDX"+objectIdMap.size(); //$NON-NLS-1$
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
            id = "IDC"+objectIdMap.size(); //$NON-NLS-1$
            type = "index-column"; //$NON-NLS-1$
            SQLIndex.Column col = (SQLIndex.Column) o;
            if (col.getColumn() != null) {
                propNames.put("column-ref", objectIdMap.get(col.getColumn())); //$NON-NLS-1$
            }
            propNames.put("ascendingOrDescending", col.getAscendingOrDescending().name()); //$NON-NLS-1$
        } else {
            throw new UnsupportedOperationException("Whoops, the SQLObject type " //$NON-NLS-1$
                    +o.getClass().getName()+" is not supported!"); //$NON-NLS-1$
        }

        objectIdMap.put(o, id);

        boolean skipChildren = false;

        //ioo.print("<"+type+" hashCode=\""+o.hashCode()+"\" id=\""+id+"\" ");  // use this for debugging duplicate object problems
        ioo.print(out, "<"+type+" id="+quote(id)+" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        if (o.allowsChildren() && o.isPopulated() && o.getChildCount() == 1 && o.getChild(0) instanceof SQLExceptionNode) {
            // if the only child is an exception node, just save the parent as non-populated
            ioo.niprint(out, "populated=\"false\" "); //$NON-NLS-1$
            skipChildren = true;
        } else if ( (!getSession().isSavingEntireSource()) && (!o.isPopulated()) ) {
            ioo.niprint(out, "populated=\"false\" "); //$NON-NLS-1$
        } else {
            ioo.niprint(out, "populated=\"true\" "); //$NON-NLS-1$
        }

        Iterator props = propNames.keySet().iterator();
        while (props.hasNext()) {
            Object key = props.next();
            Object value = propNames.get(key);
            if (value != null) {
                ioo.niprint(out, key+"="+quote(value.toString())+" "); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if ( (!skipChildren) && o.allowsChildren() && (getSession().isSavingEntireSource() || o.isPopulated()) ) {
            ioo.niprintln(out, ">"); //$NON-NLS-1$
            Iterator children = o.getChildren().iterator();
            ioo.indent++;
            while (children.hasNext()) {
                SQLObject child = (SQLObject) children.next();
                if ( ! (child instanceof SQLRelationship)) {
                    saveSQLObject(out, child);
                }
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

    private String quote(String str) {
        return "\""+SQLPowerUtils.escapeXML(str)+"\""; //$NON-NLS-1$ //$NON-NLS-2$
    }
    // ------------------- accessors and mutators ---------------------
    
    @Override
    protected ArchitectSwingSession getSession() {
        return (ArchitectSwingSession) session;
    }
}