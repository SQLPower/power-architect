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
import java.awt.Dialog;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.LiquibaseDDLGenerator;
import ca.sqlpower.architect.diff.ArchitectDiffException;
import ca.sqlpower.architect.swingui.CompareDMPanel.SourceOrTargetStuff;
import ca.sqlpower.architect.swingui.CompareDMSettings.SourceOrTargetSettings;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.diff.DiffInfo;
import ca.sqlpower.diff.DiffType;
import ca.sqlpower.diff.PropertyChange;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;

public class CompareDMFormatter {

    private static final Logger logger = Logger.getLogger(CompareDMFormatter.class);
    
    private final ArchitectSwingSession session;
    private CompareDMSettings dmSetting;

    /**
     * The dialog that owns any additional dialogs popped up by this formatter.
     */
    private final Dialog dialogOwner;
    
    /**
     * A hash map of styles which dictate the color of different kinds of changes/differences.
     */
    public final static Map<DiffType, AttributeSet> DIFF_STYLES = new HashMap<DiffType, AttributeSet>();
    static {
        SimpleAttributeSet att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, Color.red);
        DIFF_STYLES.put(DiffType.LEFTONLY, att);

        att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, Color.green.darker().darker());
        DIFF_STYLES.put(DiffType.RIGHTONLY, att);

        att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, Color.black);
        DIFF_STYLES.put(DiffType.SAME, att);

        att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, Color.orange);
        DIFF_STYLES.put(DiffType.MODIFIED, att);
        
        att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, Color.orange);
        DIFF_STYLES.put(DiffType.SQL_MODIFIED, att);

        att = new SimpleAttributeSet();
        StyleConstants.setForeground(att, Color.blue);
        DIFF_STYLES.put(DiffType.KEY_CHANGED, att);
        DIFF_STYLES.put(DiffType.DROP_KEY, att);
   }

    public CompareDMFormatter(ArchitectSwingSession session, Dialog dialogOwner, CompareDMSettings compDMSet) {
        super();
        this.session = session;
        this.dialogOwner = dialogOwner;
        dmSetting = compDMSet;
    }
    
    public void formatForEnglishOutput(List<DiffChunk<SQLObject>> diff,
            List<DiffChunk<SQLObject>> diff1, SQLObject left, SQLObject right) {
        
        try {
            
            DefaultStyledDocument sourceDoc = new DefaultStyledDocument();
            DefaultStyledDocument targetDoc = new DefaultStyledDocument();
            
            sourceDoc = generateEnglishDescription(DIFF_STYLES, convertToDiffInfo(diff));
            targetDoc = generateEnglishDescription(DIFF_STYLES, convertToDiffInfo(diff1));
            
            // get the title string for the compareDMFrame   
            
            String leftTitle = toTitleText(true, left);
            String rightTitle = toTitleText(false, right);

            CompareDMFrame cf =
                new CompareDMFrame(dialogOwner, sourceDoc, targetDoc, leftTitle,rightTitle);

            cf.pack();
            cf.setVisible(true);
            
        } catch (SQLObjectException exp) {
            ASUtils.showExceptionDialog(session, "StartCompareAction failed", exp);
            logger.error("StartCompareAction failed", exp);
        } catch (BadLocationException ex) {
            ASUtils.showExceptionDialog(session,
                    "Could not create document for results", ex);
            logger.error("Could not create document for results", ex);
        } catch (Exception ex) {
            ASUtils.showExceptionDialog(session, "Unxepected Exception!", ex);
            logger.error("Unxepected Exception!", ex);
        } 
    }

    public void formatForSQLOutput(List<DiffChunk<SQLObject>> diff,
            List<DiffChunk<SQLObject>> diff1, SQLObject left, SQLObject right) {
        try {
            SourceOrTargetStuff source = dmSetting.getSourceStuff();

            DDLGenerator gen = null;
            if (dmSetting.getOutputFormat().equals(CompareDMSettings.OutputFormat.SQL)) {
                gen = dmSetting.getDdlGenerator().newInstance();
                SQLCatalog cat = (SQLCatalog) dmSetting.getSourceSettings().getCatalogObject();
                SQLSchema sch = (SQLSchema) dmSetting.getSourceSettings().getSchemaObject();
                gen.setTargetCatalog(cat == null ? null : cat.getPhysicalName());
                gen.setTargetSchema(sch == null ? null : sch.getPhysicalName());
            } else if (dmSetting.getOutputFormat().equals(CompareDMSettings.OutputFormat.LIQUIBASE)) {
				gen = new LiquibaseDDLGenerator();
			} else {
			    throw new IllegalStateException("Don't know what kind of SQL script to generate");
			}

            List<DiffChunk<SQLObject>> addRelationships = new ArrayList<DiffChunk<SQLObject>>();
            List<DiffChunk<SQLObject>> dropRelationships = new ArrayList<DiffChunk<SQLObject>>();
            List<DiffChunk<SQLObject>> nonRelationship = new ArrayList<DiffChunk<SQLObject>>    ();
            for (DiffChunk<SQLObject> d : diff) {
                if (logger.isDebugEnabled()) logger.debug(d);
                if (d.getData() instanceof SQLRelationship) {
                    if (d.getType() == DiffType.LEFTONLY) {
                        dropRelationships.add(d);
                    } else if (d.getType() == DiffType.RIGHTONLY) {
                        addRelationships.add(d);
                    }
                } else {
                    nonRelationship.add(d);
                }
            }
            sqlScriptGenerator(DIFF_STYLES, dropRelationships, gen);
            sqlScriptGenerator(DIFF_STYLES, nonRelationship, gen);
            sqlScriptGenerator(DIFF_STYLES, addRelationships, gen);
           
            // get the title string for the compareDMFrame
            String titleString = "Generated SQL Script to turn "+ toTitleText(true, left)
            + " into " + toTitleText(false, right);

            SQLDatabase db = null;

            if ( dmSetting.getSourceSettings().getDatastoreType().equals(CompareDMSettings.DatastoreType.FILE) )
                db = null;
            else if (dmSetting.getSourceSettings().getDatastoreType().equals(CompareDMSettings.DatastoreType.PROJECT) )
                db = session.getTargetDatabase();
            else
                db = source.getDatabase();
            logger.debug("We got to place #2");

            SQLScriptDialog ssd = new SQLScriptDialog(dialogOwner,
                    "Compare DM", titleString, false, gen, db == null?null:db.getDataSource(),
                            false, session);
            ssd.setVisible(true);

        } catch (ArchitectDiffException ex) {
            ASUtils.showExceptionDialog(session, "Could not perform the diff", ex);
            logger.error("Couldn't do diff", ex);
        } catch (SQLObjectException exp) {
            ASUtils.showExceptionDialog(session, "StartCompareAction failed", exp);
            logger.error("StartCompareAction failed", exp);
        } catch (BadLocationException ex) {
            ASUtils.showExceptionDialog(session,
                    "Could not create document for results", ex);
            logger.error("Could not create document for results", ex);
        } catch (Exception ex) {
            ASUtils.showExceptionDialog(session, "Unxepected Exception!", ex);
            logger.error("Unxepected Exception!", ex);
        } 

    }

    private void sqlScriptGenerator(Map<DiffType, AttributeSet> styles, List<DiffChunk<SQLObject>> diff,
            DDLGenerator gen) throws ArchitectDiffException, SQLException, SQLObjectException, BadLocationException,
            InstantiationException, IllegalAccessException {
        for (DiffChunk<SQLObject> chunk : diff) {
            if (chunk.getType() == DiffType.KEY_CHANGED) {
                if(chunk.getData() instanceof SQLTable) {
                    SQLTable t = (SQLTable) chunk.getData();
                    if (hasKey(t)) {
                        gen.addPrimaryKey(t);
                    }
                }
            } else if (chunk.getType() == DiffType.DROP_KEY) {
                if(chunk.getData() instanceof SQLTable) {
                    SQLTable t = (SQLTable) chunk.getData();
                    if (hasKey(t)) {
                        gen.dropPrimaryKey(t);
                    }
                }
            } else if (chunk.getType() == DiffType.LEFTONLY) {
                if (chunk.getData() instanceof SQLTable) {
                    SQLTable t = (SQLTable) chunk.getData();
                    gen.dropTable(t);
                } else if (chunk.getData() instanceof SQLColumn) {
                    SQLColumn c = (SQLColumn) chunk.getData();
                    gen.dropColumn(c);
                } else if (chunk.getData() instanceof SQLRelationship) {
                    SQLRelationship r = (SQLRelationship)chunk.getData();
                    gen.dropRelationship(r);

                } else {
                    throw new IllegalStateException("DiffChunk is an unexpected type.");
                }

            } else if (chunk.getType() == DiffType.RIGHTONLY) {
                if (chunk.getData() instanceof SQLTable) {
                    SQLTable t = (SQLTable) chunk.getData();
                    if (t == null ) throw new NullPointerException();
                    if (t.getObjectType().equals("TABLE")) {
                        gen.addTable(t);
                    }
                } else if (chunk.getData() instanceof SQLColumn) {
                    SQLColumn c = (SQLColumn) chunk.getData();
                    gen.addColumn(c);
                } else if (chunk.getData() instanceof SQLRelationship) {
                    SQLRelationship r = (SQLRelationship)chunk.getData();
                    gen.addRelationship(r);
                } else {
                    throw new IllegalStateException("DiffChunk is an unexpected type.");
                }
            } else if (chunk.getType() == DiffType.SQL_MODIFIED) {
                if (chunk.getData() instanceof SQLColumn) {
                    SQLColumn c = (SQLColumn) chunk.getData();
                    gen.modifyColumn(c);
                }
                for (PropertyChange change : chunk.getPropertyChanges()) {
                    if (change.getPropertyName().equals("remarks")) {
                        gen.modifyComment(chunk.getData());
                        break;
                    }
                }
            } else if (chunk.getType() == DiffType.SAME) {
                //do nothing when they're the same
                } else {
                throw new IllegalStateException("DiffChunk is an invalid type: " + chunk.getType());
            }
        }
    }
    
    /**
     * This method generates english descriptions by taking in the diff list
     * and putting the appropiate statements in the returned document.  It will iterate
     * through the diff list and identify which type of DiffChunk it is and
     * what kind of SQLType it is to produce the proper english description output
     * @throws BadLocationException
     * @throws SQLObjectException
     */        
    public static DefaultStyledDocument generateEnglishDescription(
            Map<DiffType, AttributeSet> styles, List<DiffChunk<DiffInfo>> diff)
            throws BadLocationException, SQLObjectException {
        
        DefaultStyledDocument resultDoc = new DefaultStyledDocument();
        
        for (DiffChunk<DiffInfo> chunk : diff) {
            
            DiffInfo info = chunk.getData();
            
            if (chunk.getType().equals(DiffType.DROP_KEY)) {
                //Drop key does will be shown here by a key changed type
                //Drop key is mainly used in sql script generation.
                continue;
            }
            
            AttributeSet attributes = styles.get(chunk.getType());
            MutableAttributeSet boldAttributes = new SimpleAttributeSet(attributes);
            StyleConstants.setBold(boldAttributes, true);
            
            String diffTypeEnglish;                                    
            switch (chunk.getType()) {
            case LEFTONLY:
                diffTypeEnglish = "should be removed";
                break;

            case MODIFIED:
            case SQL_MODIFIED:
                diffTypeEnglish = "should be modified";
                break;

            case SAME:
                diffTypeEnglish = "needs no changes";
                break;

            case RIGHTONLY:
                diffTypeEnglish = "should be added";
                break;

            case KEY_CHANGED:
                diffTypeEnglish = "needs a different primary key";
                break;
                
            case DROP_KEY:
                diffTypeEnglish = "needs to drop the source primary key";
                break;

            default:
                diffTypeEnglish = "!UNKNOWN DIFF TYPE!";
                logger.error("Woops, unknown diff chunk type: "+chunk.getType());
                break;
            }

            resultDoc.insertString(
                    resultDoc.getLength(),
                    info.toString() + " " + diffTypeEnglish + "\n",
                    attributes);
            
            for (PropertyChange change : chunk.getPropertyChanges()) {
                logger.debug("Formatting property change");
                String s = info.getIndent() + "\t" + change.getPropertyName();           
                s += " has been changed from " + change.getOldValue();
                s += " to " + change.getNewValue() + "\n";
                
                resultDoc.insertString(
                        resultDoc.getLength(),
                        s, attributes);
            }
        }
        
        return resultDoc;

//        String currentTableName = "";
//        
//        for (DiffChunk<SQLObject> chunk : diff) {
//            SQLObject o = chunk.getData();
//            if (suppressSimilarities && chunk.getType().equals(DiffType.SAME)) {
//                if (o instanceof SQLTable) {
//                    currentTableName = o.getName();
//                }
//                    
//                continue;
//            }
//            if (chunk.getType().equals(DiffType.DROP_KEY)) {
//                //Drop key does will be shown here by a key changed type
//                //Drop key is mainly used in sql script generation.
//                continue;
//            }
//            AttributeSet attributes = styles.get(chunk.getType());
//            MutableAttributeSet boldAttributes = new SimpleAttributeSet(attributes);
//            StyleConstants.setBold(boldAttributes, true);
//
//            if (o == null) {
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        "ERROR: null object in diff list\n",
//                        attributes);
//            } else if (o instanceof SQLTable) {
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        "Table ",
//                        attributes);
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        (o.getPhysicalName() != null || o.getPhysicalName().trim().equals("") ? o.getName() : o.getPhysicalName())+ " ",
//                        boldAttributes);
//            } else if (o instanceof SQLColumn) {
//                if (suppressSimilarities && !currentTableName.equals("")) {
//                    attributes = styles.get(DiffType.SAME);
//                    boldAttributes = new SimpleAttributeSet(attributes);
//                    StyleConstants.setBold(boldAttributes, true);
//                    resultDoc.insertString(
//                            resultDoc.getLength(), 
//                            "Table ", 
//                            attributes);
//                    resultDoc.insertString(
//                            resultDoc.getLength(), 
//                            currentTableName, 
//                            boldAttributes);
//                    resultDoc.insertString(
//                            resultDoc.getLength(), 
//                            " needs no changes\n", 
//                            attributes);
//                    currentTableName = "";
//                }
//                attributes = styles.get(chunk.getType());
//                boldAttributes = new SimpleAttributeSet(attributes);
//                StyleConstants.setBold(boldAttributes, true);
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        "\tColumn ",
//                        attributes);
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        o.getName() + " ",
//                        boldAttributes);
//            } else if (o instanceof SQLRelationship) {
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        "Foreign Key ",
//                        attributes);
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        o.getName() + " ",
//                        boldAttributes);
//            } else {
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        "Unknown object type ",
//                        attributes);
//                resultDoc.insertString(
//                        resultDoc.getLength(),
//                        o.getClass().getName() + " ",
//                        boldAttributes);
//            }
//
//
//            String diffTypeEnglish;
//            switch (chunk.getType()) {
//            case LEFTONLY:
//                diffTypeEnglish = "should be removed";
//                break;
//
//            case MODIFIED:
//                diffTypeEnglish = "should be modified";
//                break;
//
//            case SAME:
//                diffTypeEnglish = "needs no changes";
//                break;
//
//            case RIGHTONLY:
//                diffTypeEnglish = "should be added";
//                break;
//
//            case KEY_CHANGED:
//                diffTypeEnglish = "needs a different primary key";
//                break;
//                
//            case DROP_KEY:
//                diffTypeEnglish = "needs to drop the source primary key";
//                break;
//
//            default:
//                diffTypeEnglish = "!UNKNOWN DIFF TYPE!";
//                logger.error("Woops, unknown diff chunk type: "+chunk.getType());
//                break;
//            }
//
//            resultDoc.insertString(
//                    resultDoc.getLength(),
//                    diffTypeEnglish + "\n",
//                    attributes);
//        }
//        return resultDoc;
    }
    
//  Generates the proper title text for compareDMFrame or SQLScriptDialog                
    private String toTitleText(boolean isSource, SQLObject leftOrRight) {                    
        StringBuffer fileName = new StringBuffer();
        boolean needBrackets = false;
        SourceOrTargetSettings settings;
        
        if (isSource) {
            settings = dmSetting.getSourceSettings();
        } else {
            settings = dmSetting.getTargetSettings();
        }
        
        //Deals with the file name first if avaiable
        if (settings.getDatastoreType().equals(CompareDMSettings.DatastoreType.FILE)) {                                
            File f = new File(settings.getFilePath());                                                                                         
            String tempName = f.getName();
            int lastIndex = tempName.lastIndexOf(".architect");
            if (lastIndex < 0) {
                fileName.append(tempName);
            } else {
                fileName.append(tempName.substring(0, lastIndex));
            }
            needBrackets = true;
        } else if (settings.getDatastoreType().equals(CompareDMSettings.DatastoreType.PROJECT)) {
            SwingUIProjectLoader swingUIProject = session.getProjectLoader();
            String tempName;
            if (swingUIProject.getFile() != null) {
                tempName = swingUIProject.getFile().getName();
            } else {
                tempName = "New Project";
            }
            int lastIndex = tempName.lastIndexOf(".architect");
            if (lastIndex < 0){
                fileName.append(tempName);
            } else {
                fileName.append(tempName.substring(0,lastIndex));
            }
            needBrackets = true;
        }
        
        //Add in the database name
        if (needBrackets) {
            fileName.append(" (");
        }
        fileName.append(SQLObjectUtils.toQualifiedName(leftOrRight));
        if (needBrackets) {
            fileName.append(")");
        }
        return fileName.toString(); 
    }
    
    private boolean hasKey(SQLTable t) throws SQLObjectException {
        boolean hasKey = false;
        for (SQLColumn c : t.getColumns()) {
            if (c.isPrimaryKey()) {
                hasKey=true;
                break;
            }
        }
        return hasKey;
    }
    
    private List<DiffChunk<DiffInfo>> convertToDiffInfo(List<DiffChunk<SQLObject>> diff) {
        
        List<DiffChunk<DiffInfo>> newDiff = new ArrayList<DiffChunk<DiffInfo>>();
        List<SQLObject> ancestors = new ArrayList<SQLObject>();
        ancestors.add(new SQLTable());
        int depth = 0;
        for (DiffChunk<SQLObject> chunk : diff) {               
            SQLObject data = chunk.getData();                
            String name;
            if (data instanceof SQLTable && data.getPhysicalName() != null) {
                name = data.getPhysicalName();
            } else {
                name = data.getName();
            }
            DiffInfo info = new DiffInfo(data.getClass().getSimpleName().replaceFirst("SQL", ""), name);
            
            // Set the depth property based on the object hierarchy.
            // If it is a relationship, we ignore depth, and there are only relationships left.
            if (chunk.getData() instanceof SQLRelationship) {
                depth = 0;
            } else if (ancestors.get(depth).allowsChildType(data.getClass())) {
                ancestors.add(data);
                depth++;
            } else {
                while (depth > 0 && !ancestors.get(depth - 1).allowsChildType(data.getClass())) {
                    ancestors.remove(depth);
                    depth--;
                }
                ancestors.add(depth, data);
            }
            
            info.setDepth(depth);
            DiffChunk<DiffInfo> newChunk = new DiffChunk<DiffInfo>(info, chunk.getType());
            for (PropertyChange change : chunk.getPropertyChanges()) {
                newChunk.addPropertyChange(change);
            }
            newDiff.add(newChunk);
        }
     
        return newDiff;
        
    }
}
