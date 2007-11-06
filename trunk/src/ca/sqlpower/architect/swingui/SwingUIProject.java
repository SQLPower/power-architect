/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

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
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
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
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileManager;
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
    private static final String RELATIONSHIP_STYLE_RECTILINEAR = "rectilinear";

    /**
     * The constant value within the project file representing a playpen whose
     * relationships should be drawn using straight lines (often thought of as
     * diagonal, but that depends on the relative positions of the two tables
     * the relationship connects).
     */
    private static final String RELATIONSHIP_STYLE_DIRECT = "direct";

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
            throw new NullPointerException("Null session is not allowed!");
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
        d.addFactoryCreate("architect-project/play-pen", ppFactory);

        TablePaneFactory tablePaneFactory = new TablePaneFactory();
        d.addFactoryCreate("architect-project/play-pen/table-pane", tablePaneFactory);
        // factory will add the tablepanes to the playpen

        PPRelationshipFactory ppRelationshipFactory = new PPRelationshipFactory();
        d.addFactoryCreate("architect-project/play-pen/table-link", ppRelationshipFactory);

        CompareDMSettingFactory settingFactory = new CompareDMSettingFactory();
        d.addFactoryCreate("architect-project/compare-dm-settings", settingFactory);
        d.addSetProperties("architect-project/compare-dm-settings");

        CompareDMStuffSettingFactory sourceStuffFactory = new CompareDMStuffSettingFactory(true);
        d.addFactoryCreate("architect-project/compare-dm-settings/source-stuff", sourceStuffFactory);
        d.addSetProperties("architect-project/compare-dm-settings/source-stuff");

        CompareDMStuffSettingFactory targetStuffFactory = new CompareDMStuffSettingFactory(false);
        d.addFactoryCreate("architect-project/compare-dm-settings/target-stuff", targetStuffFactory);
        d.addSetProperties("architect-project/compare-dm-settings/target-stuff");
        
        CreateKettleJobSettingsFactory ckjsFactory = new CreateKettleJobSettingsFactory();
        d.addFactoryCreate("architect-project/create-kettle-job-settings", ckjsFactory);
        d.addSetProperties("architect-project/create-kettle-job-settings");

        return d;
    }

    private class PlayPenFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            String relStyle = attributes.getValue("relationship-style");
            boolean direct;
            if (relStyle == null) {
                direct = false;
            } else if (relStyle.equals(RELATIONSHIP_STYLE_DIRECT)) {
                direct = true;
            } else if (relStyle.equals(RELATIONSHIP_STYLE_RECTILINEAR)) {
                direct = false;
            } else {
                logger.warn("Unknown relationship style \"\"; defaulting to rectilinear");
                direct = false;
            }
            getSession().setRelationshipLinesDirect(direct);
            return getSession().getPlayPen();
        }
    }

    private class TablePaneFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            int x = Integer.parseInt(attributes.getValue("x"));
            int y = Integer.parseInt(attributes.getValue("y"));
            SQLTable tab = (SQLTable) objectIdMap.get(attributes.getValue("table-ref"));
            TablePane tp = new TablePane(tab, getSession().getPlayPen());
            getSession().getPlayPen().addTablePane(tp, new Point(x, y));
            return tp;
        }
    }

    private class PPRelationshipFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            Relationship r = null;
            try {
                SQLRelationship rel =
                    (SQLRelationship) objectIdMap.get(attributes.getValue("relationship-ref"));
                r = new Relationship(getSession().getPlayPen(), rel);
                getSession().getPlayPen().addRelationship(r);

                int pkx = Integer.parseInt(attributes.getValue("pk-x"));
                int pky = Integer.parseInt(attributes.getValue("pk-y"));
                int fkx = Integer.parseInt(attributes.getValue("fk-x"));
                int fky = Integer.parseInt(attributes.getValue("fk-y"));
                r.setPkConnectionPoint(new Point(pkx, pky));
                r.setFkConnectionPoint(new Point(fkx, fky));
            } catch (ArchitectException e) {
                logger.error("Couldn't create relationship component", e);
            } catch (NumberFormatException e) {
                logger.warn("Didn't set connection points because of integer parse error");
            } catch (NullPointerException e) {
                logger.debug("No pk/fk connection points specified in save file;"
                        +" not setting custom connection points");
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
            throw new ArchitectException("problem saving project -- "
                    + "cannot write to architect file: "
                    + file.getAbsolutePath());
        }

        File backupFile = new File (file.getParent(), file.getName()+"~");

        // Several places we would check dir perms, but MS-Windows stupidly doesn't let use the
        // "directory write" attribute for directory writing (but instead overloads
        // it to mean 'this is a special directory'.

        File tempFile = null;
        tempFile = new File (file.getParent(),"tmp___" + file.getName());
        String encoding = "UTF-8";
        try {
            // If creating this temp file fails, feed the user back a more explanatory message
            out = new PrintWriter(tempFile,encoding);
        } catch (IOException e) {
            throw new ArchitectException("Unable to create output file for save operation, data NOT saved.\n" + e, e);
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
            logger.error("Setting progress monitor maximum to "+pmMax);
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
        logger.debug("deleting backup~ file: " + fstatus);

        // If this is a brand new project, the old file does not yet exist, no point trying to rename it.
        // But if it already existed, renaming current to backup must succeed, or we give up.
        if (file.exists()) {
            fstatus = file.renameTo(backupFile);
            logger.debug("rename current file to backupFile: " + fstatus);
            if (!fstatus) {
                throw new ArchitectException((
                        "Could not rename current file to backup\nProject saved in " +
                        tempFile + ": " + file + " still contains old project"));
            }
        }
        fstatus = tempFile.renameTo(file);
        if (!fstatus) {
            throw new ArchitectException((
                    "Could not rename temp file to current\nProject saved in " +
                    tempFile + ": " + file + " still contains old project"));
        }
        logger.debug("rename tempFile to current file: " + fstatus);
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
            ioo.println(out, "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");
            ioo.println(out, "<architect-project version=\"1.0\" appversion=\""+ArchitectVersion.APP_VERSION+"\">");
            ioo.indent++;
            ioo.println(out, "<project-name>"+SQLPowerUtils.escapeXML(getSession().getName())+"</project-name>");
            saveDataSources(out);
            saveSourceDatabases(out);
            saveTargetDatabase(out);
            saveDDLGenerator(out);
            saveCompareDMSettings(out);
            saveCreateKettleJobSettings(out);
            savePlayPen(out);
            saveProfiles(out);
            ioo.indent--;
            ioo.println(out, "</architect-project>");
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
        ioo.println(out, "<project-data-sources>");
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
                    id = "DS"+dsNum;
                    dbcsIdMap.put(ds, id);
                }
                ioo.println(out, "<data-source id=\""+SQLPowerUtils.escapeXML(id)+"\">");
                ioo.indent++;
                Iterator pit = ds.getPropertiesMap().entrySet().iterator();
                while (pit.hasNext()) {
                    Map.Entry ent = (Map.Entry) pit.next();
                    if (ent.getValue() != null) {
                        ioo.println(out, "<property key="+quote((String) ent.getKey())+" value="+quote((String) ent.getValue())+" />");
                    }
                }
                ioo.indent--;
                ioo.println(out, "</data-source>");
                dsNum++;
            }
            dsNum++;
        }
        ioo.indent--;
        ioo.println(out, "</project-data-sources>");
    }
    

    private void saveDDLGenerator(PrintWriter out) throws IOException {
        ioo.print(out, "<ddl-generator"
                +" type=\""+ getSession().getDDLGenerator().getClass().getName()+"\""
                +" allow-connection=\""+getSession().getDDLGenerator().getAllowConnection()+"\"");
        if (getSession().getDDLGenerator().getTargetCatalog() != null) {
            ioo.niprint(out, " target-catalog=\""+SQLPowerUtils.escapeXML(getSession().getDDLGenerator().getTargetCatalog())+"\"");
        }
        if (getSession().getDDLGenerator().getTargetSchema() != null) {
            ioo.niprint(out, " target-schema=\""+SQLPowerUtils.escapeXML(getSession().getDDLGenerator().getTargetSchema())+"\"");
        }
        ioo.niprint(out, ">");
        ioo.indent++;
        if (getSession().getDDLGenerator().getFile() != null) {
            ioo.println(out, "<file path=\""+SQLPowerUtils.escapeXML(getSession().getDDLGenerator().getFile().getPath())+"\" />");
        }
        ioo.indent--;
        ioo.println(out, "</ddl-generator>");
    }
    
    private void saveCreateKettleJobSettings(PrintWriter out) throws IOException {
        ioo.print(out, "<create-kettle-job-settings");
        ioo.niprint(out, " filePath=\"" + SQLPowerUtils.escapeXML(getSession().getKettleJob().getFilePath()) + "\"");
        ioo.niprint(out, " jobName=\"" + SQLPowerUtils.escapeXML(getSession().getKettleJob().getJobName()) + "\"");
        ioo.niprint(out, " schemaName=\"" + SQLPowerUtils.escapeXML(getSession().getKettleJob().getSchemaName()) + "\"");
        ioo.niprint(out, " kettleJoinType=\"" + getSession().getKettleJob().getKettleJoinType() + "\"");
        ioo.niprint(out, " savingToFile=\"" + getSession().getKettleJob().isSavingToFile() + "\"");
        ioo.niprintln(out, " />");
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
        ioo.print(out, "<compare-dm-settings");
        ioo.print(out, " sqlScriptFormat=\""+SQLPowerUtils.escapeXML(getSession().getCompareDMSettings().getSqlScriptFormat())+"\"");
        ioo.print(out, " outputFormatAsString=\""+SQLPowerUtils.escapeXML(getSession().getCompareDMSettings().getOutputFormatAsString())+"\"");
        ioo.println(out, ">");
        ioo.indent++;
        ioo.print(out, "<source-stuff");
        saveSourceOrTargetAttributes(out, getSession().getCompareDMSettings().getSourceSettings());
        ioo.print(out, "/>");
        ioo.print(out, "<target-stuff");
        saveSourceOrTargetAttributes(out, getSession().getCompareDMSettings().getTargetSettings());
        ioo.print(out, "/>");
        ioo.indent--;
        ioo.println(out, "</compare-dm-settings>");
    }


    private void saveSourceOrTargetAttributes(PrintWriter out, SourceOrTargetSettings sourceSettings) {
        ioo.print(out, " datastoreTypeAsString=\""+SQLPowerUtils.escapeXML(sourceSettings.getDatastoreTypeAsString())+"\"");
        if (sourceSettings.getConnectName() != null)
            ioo.print(out, " connectName=\""+SQLPowerUtils.escapeXML(sourceSettings.getConnectName())+"\"");

        if (sourceSettings.getCatalog() != null)
            ioo.print(out, " catalog=\""+SQLPowerUtils.escapeXML(sourceSettings.getCatalog())+"\"");
        if (sourceSettings.getSchema() != null)
            ioo.print(out, " schema=\""+SQLPowerUtils.escapeXML(sourceSettings.getSchema())+"\"");
        ioo.print(out, " filePath=\""+SQLPowerUtils.escapeXML(sourceSettings.getFilePath())+"\"");

    }
    /**
     * Creates a &lt;source-databases&gt; element which contains zero
     * or more &lt;database&gt; elements.
     * @param out2
     */
    private void saveSourceDatabases(PrintWriter out) throws IOException, ArchitectException {
        ioo.println(out, "<source-databases>");
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
        ioo.println(out, "</source-databases>");
    }

    /**
     * Recursively walks through the children of db, writing to the
     * output file all SQLRelationship objects encountered.
     */
    private void saveRelationships(PrintWriter out, SQLDatabase db) throws ArchitectException, IOException {
        ioo.println(out, "<relationships>");
        ioo.indent++;
        Iterator it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveRelationshipsRecurse(out, (SQLObject) it.next());
        }
        ioo.indent--;
        ioo.println(out, "</relationships>");
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
        ioo.println(out, "<target-database dbcs-ref="+
                quote(dbcsIdMap.get(db.getDataSource()).toString())+ ">");
        ioo.indent++;
        Iterator it = db.getChildren().iterator();
        while (it.hasNext()) {
            saveSQLObject(out, (SQLObject) it.next());
        }
        saveRelationships(out, db);
        ioo.indent--;
        ioo.println(out, "</target-database>");
    }

    private void savePlayPen(PrintWriter out) throws IOException, ArchitectException {
        String relStyle = getSession().getRelationshipLinesDirect() ?
                RELATIONSHIP_STYLE_DIRECT : RELATIONSHIP_STYLE_RECTILINEAR;
        ioo.println(out, "<play-pen relationship-style="+quote(relStyle)+">");
        ioo.indent++;
        for(int i = getSession().getPlayPen().getTablePanes().size()-1; i>= 0; i--) {
            TablePane tp = getSession().getPlayPen().getTablePanes().get(i);
            Point p = tp.getLocation();
            ioo.println(out, "<table-pane table-ref="+quote(objectIdMap.get(tp.getModel()).toString())+""
                    +" x=\""+p.x+"\" y=\""+p.y+"\" />");
            if (pm != null) {
                pm.setProgress(++progress);
            }
        }

        Iterator it = getSession().getPlayPen().getRelationships().iterator();
        while (it.hasNext()) {
            Relationship r = (Relationship) it.next();
            ioo.println(out, "<table-link relationship-ref="+quote(objectIdMap.get(r.getModel()).toString())
                    +" pk-x=\""+r.getPkConnectionPoint().x+"\""
                    +" pk-y=\""+r.getPkConnectionPoint().y+"\""
                    +" fk-x=\""+r.getFkConnectionPoint().x+"\""
                    +" fk-y=\""+r.getFkConnectionPoint().y+"\" />");
        }
        ioo.indent--;
        ioo.println(out, "</play-pen>");
    }

    /**
     * Save all of the profiling information.
     * @param out
     */
    private void saveProfiles(PrintWriter out) {
        TableProfileManager profmgr;
        if (getSession().getProfileManager() instanceof TableProfileManager) {
            profmgr = (TableProfileManager) getSession().getProfileManager();
        } else {
            throw new ArchitectRuntimeException(new ArchitectException("Session.getProfileManager should be a TableProfileManager"));
        }
        ioo.println(out, "<profiles topNCount=\""+profmgr.getDefaultProfileSettings().getTopNCount()+"\">");
        ioo.indent++;

        List<TableProfileResult> tableResults = profmgr.getResults();
        
        for (TableProfileResult tableResult : tableResults) {
            printCommonItems(out, tableResult);
            ioo.print(out, " rowCount=\"" + tableResult.getRowCount() + "\"");
            ioo.niprintln(out, "/>");
            
            List<ColumnProfileResult> columnProfileResults = tableResult.getColumnProfileResults();
            for (ColumnProfileResult cpr : columnProfileResults) {
                printCommonItems(out, cpr);
                ioo.niprint(out, " avgLength=\"" + cpr.getAvgLength() + "\"");

                ioo.niprint(out, " minLength=\"" + cpr.getMinLength() + "\"");
                ioo.niprint(out, " maxLength=\"" + cpr.getMaxLength() + "\"");
                ioo.niprint(out, " nullCount=\"" + cpr.getNullCount() + "\"");
                ioo.niprint(out, " distinctValueCount=\"" + cpr.getDistinctValueCount() + "\"");
                ioo.niprintln(out, ">");

                ioo.indent++;

                if ( cpr.getAvgValue() != null ) {
                    ioo.println(out, "<avgValue type=\"" +
                            cpr.getAvgValue().getClass().getName() +
                            "\" value=\""+
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getAvgValue())) +
                            "\"/>" );
                }
                if ( cpr.getMaxValue() != null ) {
                    ioo.println(out, "<maxValue type=\"" +
                            cpr.getMaxValue().getClass().getName() +
                            "\" value=\""+
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getMaxValue())) +
                            "\"/>" );
                }
                if ( cpr.getMinValue() != null ) {
                    ioo.println(out, "<minValue type=\"" +
                            cpr.getMinValue().getClass().getName() +
                            "\" value=\""+
                            SQLPowerUtils.escapeXML(String.valueOf(cpr.getMinValue())) +
                            "\"/>" );
                }

                List<ColumnValueCount> valueCount = cpr.getValueCount();
                if (valueCount != null) {
                    for (ColumnValueCount count : valueCount) {
                        ioo.println(out, "<topNvalue count=\""+
                                count.getCount()+
                                "\" type=\"" +
                                (count.getValue() == null ? "" : count.getValue().getClass().getName()) +
                                "\" value=\""+
                                SQLPowerUtils.escapeXML(String.valueOf(count.getValue()))+
                                "\"/>" );
                    }
                }
                ioo.indent--;

                ioo.println(out, "</profile-result>");
            }
        }
        ioo.println(out, "</profiles>");
        ioo.indent--;
    }

    private void printCommonItems(PrintWriter out, ProfileResult profileResult) {
        SQLObject profiledObject = profileResult.getProfiledObject();
        ioo.print(out, "<profile-result ref-id=\""+objectIdMap.get(profiledObject)+"\"" +
                " type=\"" + profileResult.getClass().getName() + "\"" +
                " createStartTime=\""+profileResult.getCreateStartTime()+"\"" +
                " createEndTime=\""+profileResult.getCreateEndTime()+"\"" +
                " exception=\""+(profileResult.getException() == null ? "false" : "true")+"\"");
        if (profileResult.getException() != null) {
            ioo.niprint(out, " exception-type=\""+SQLPowerUtils.escapeXML(profileResult.getException().getClass().getName())+"\"");
            ioo.niprint(out, " exception-message=\""+SQLPowerUtils.escapeXML(profileResult.getException().getMessage())+"\"");
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
            ioo.println(out, "<reference ref-id=\""+SQLPowerUtils.escapeXML(id)+"\" />");
            return;
        }

        String type;
        Map<String,Object> propNames = new TreeMap<String,Object>();

        // properties of all SQLObject types
        propNames.put("physicalName", o.getPhysicalName());
        propNames.put("name", o.getName()); // note: there was no name attrib for SQLDatabase, SQLRelationship.ColumnMapping, and SQLExceptionNode

        if (o instanceof SQLDatabase) {
            id = "DB"+objectIdMap.size();
            type = "database";
            propNames.put("dbcs-ref", dbcsIdMap.get(((SQLDatabase) o).getDataSource()));
        } else if (o instanceof SQLCatalog) {
            id = "CAT"+objectIdMap.size();
            type = "catalog";
            propNames.put("nativeTerm", ((SQLCatalog) o).getNativeTerm());
        } else if (o instanceof SQLSchema) {
            id = "SCH"+objectIdMap.size();
            type = "schema";
            propNames.put("nativeTerm", ((SQLSchema) o).getNativeTerm());
        } else if (o instanceof SQLTable) {
            id = "TAB"+objectIdMap.size();
            type = "table";
            propNames.put("remarks", ((SQLTable) o).getRemarks());
            propNames.put("objectType", ((SQLTable) o).getObjectType());
            // don't save primary key name. It is a propery of the PK index, not the table.
            if (pm != null) {
                pm.setProgress(++progress);
            }
        } else if (o instanceof SQLTable.Folder) {
            id = "FOL"+objectIdMap.size();
            type = "folder";
            propNames.put("type", new Integer(((SQLTable.Folder) o).getType()));
        } else if (o instanceof SQLColumn) {
            id = "COL"+objectIdMap.size();
            type = "column";
            SQLColumn sourceCol = ((SQLColumn) o).getSourceColumn();
            if (sourceCol != null) {
                propNames.put("source-column-ref", objectIdMap.get(sourceCol));
            }
            propNames.put("type", new Integer(((SQLColumn) o).getType()));
            propNames.put("sourceDataTypeName", ((SQLColumn) o).getSourceDataTypeName());
            propNames.put("scale", new Integer(((SQLColumn) o).getScale()));
            propNames.put("precision", new Integer(((SQLColumn) o).getPrecision()));
            propNames.put("nullable", new Integer(((SQLColumn) o).getNullable()));
            propNames.put("remarks", ((SQLColumn) o).getRemarks());
            propNames.put("defaultValue", ((SQLColumn) o).getDefaultValue());
            propNames.put("primaryKeySeq", ((SQLColumn) o).getPrimaryKeySeq());
            propNames.put("autoIncrement", new Boolean(((SQLColumn) o).isAutoIncrement()));
            propNames.put("referenceCount", new Integer(((SQLColumn)o).getReferenceCount()));
            if (((SQLColumn) o).isAutoIncrementSequenceNameSet()) {
                propNames.put("autoIncrementSequenceName", ((SQLColumn) o).getAutoIncrementSequenceName());
            }
        } else if (o instanceof SQLRelationship) {
            id = "REL"+objectIdMap.size();
            type = "relationship";
            propNames.put("pk-table-ref", objectIdMap.get(((SQLRelationship) o).getPkTable()));
            propNames.put("fk-table-ref", objectIdMap.get(((SQLRelationship) o).getFkTable()));
            propNames.put("updateRule", new Integer(((SQLRelationship) o).getUpdateRule()));
            propNames.put("deleteRule", new Integer(((SQLRelationship) o).getDeleteRule()));
            propNames.put("deferrability", new Integer(((SQLRelationship) o).getDeferrability().getCode()));
            propNames.put("pkCardinality", new Integer(((SQLRelationship) o).getPkCardinality()));
            propNames.put("fkCardinality", new Integer(((SQLRelationship) o).getFkCardinality()));
            propNames.put("identifying", new Boolean(((SQLRelationship) o).isIdentifying()));
        } else if (o instanceof SQLRelationship.ColumnMapping) {
            id = "CMP"+objectIdMap.size();
            type = "column-mapping";
            propNames.put("pk-column-ref", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getPkColumn()));
            propNames.put("fk-column-ref", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getFkColumn()));
        } else if (o instanceof SQLExceptionNode) {
            id = "EXC"+objectIdMap.size();
            type = "sql-exception";
            propNames.put("message", ((SQLExceptionNode) o).getMessage());
        } else if (o instanceof SQLIndex) {
            id = "IDX"+objectIdMap.size();
            type = "index";
            SQLIndex index = (SQLIndex) o;
            propNames.put("unique", index.isUnique());
            propNames.put("qualifier", index.getQualifier());
            propNames.put("index-type", index.getType().name());
            propNames.put("primaryKeyIndex", index.isPrimaryKeyIndex());
            propNames.put("filterCondition", index.getFilterCondition());
        } else if (o instanceof SQLIndex.Column) {
            id = "IDC"+objectIdMap.size();
            type = "index-column";
            SQLIndex.Column col = (SQLIndex.Column) o;
            if (col.getColumn() != null) {
                propNames.put("column-ref", objectIdMap.get(col.getColumn()));
            }
            propNames.put("ascending", col.isAscending());
            propNames.put("descending", col.isDescending());
        } else {
            throw new UnsupportedOperationException("Whoops, the SQLObject type "
                    +o.getClass().getName()+" is not supported!");
        }

        objectIdMap.put(o, id);

        boolean skipChildren = false;

        //ioo.print("<"+type+" hashCode=\""+o.hashCode()+"\" id=\""+id+"\" ");  // use this for debugging duplicate object problems
        ioo.print(out, "<"+type+" id="+quote(id)+" ");

        if (o.allowsChildren() && o.isPopulated() && o.getChildCount() == 1 && o.getChild(0) instanceof SQLExceptionNode) {
            // if the only child is an exception node, just save the parent as non-populated
            ioo.niprint(out, "populated=\"false\" ");
            skipChildren = true;
        } else if ( (!getSession().isSavingEntireSource()) && (!o.isPopulated()) ) {
            ioo.niprint(out, "populated=\"false\" ");
        } else {
            ioo.niprint(out, "populated=\"true\" ");
        }

        Iterator props = propNames.keySet().iterator();
        while (props.hasNext()) {
            Object key = props.next();
            Object value = propNames.get(key);
            if (value != null) {
                ioo.niprint(out, key+"="+quote(value.toString())+" ");
            }
        }
        if ( (!skipChildren) && o.allowsChildren() && (getSession().isSavingEntireSource() || o.isPopulated()) ) {
            ioo.niprintln(out, ">");
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
            ioo.println(out, "</"+type+">");
        } else {
            ioo.niprintln(out, "/>");
        }
    }

    private String quote(String str) {
        return "\""+SQLPowerUtils.escapeXML(str)+"\"";
    }
    // ------------------- accessors and mutators ---------------------
    
    @Override
    protected ArchitectSwingSession getSession() {
        return (ArchitectSwingSession) session;
    }
}