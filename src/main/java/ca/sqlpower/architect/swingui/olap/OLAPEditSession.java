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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.layout.FruchtermanReingoldForceLayout;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.undo.OLAPUndoManager;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;
import ca.sqlpower.architect.swingui.action.RedoAction;
import ca.sqlpower.architect.swingui.action.UndoAction;
import ca.sqlpower.architect.swingui.action.ZoomAction;
import ca.sqlpower.architect.swingui.action.ZoomResetAction;
import ca.sqlpower.architect.swingui.action.ZoomToFitAction;
import ca.sqlpower.architect.swingui.olap.action.CreateCalculatedMemberAction;
import ca.sqlpower.architect.swingui.olap.action.CreateCubeAction;
import ca.sqlpower.architect.swingui.olap.action.CreateCubeUsageAction;
import ca.sqlpower.architect.swingui.olap.action.CreateDimensionAction;
import ca.sqlpower.architect.swingui.olap.action.CreateDimensionUsageAction;
import ca.sqlpower.architect.swingui.olap.action.CreateHierarchyAction;
import ca.sqlpower.architect.swingui.olap.action.CreateLevelAction;
import ca.sqlpower.architect.swingui.olap.action.CreateMeasureAction;
import ca.sqlpower.architect.swingui.olap.action.CreateVirtualCubeAction;
import ca.sqlpower.architect.swingui.olap.action.ExportSchemaAction;
import ca.sqlpower.architect.swingui.olap.action.OLAPDeleteSelectedAction;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

public class OLAPEditSession extends AbstractSPListener {

    private final OLAPTree tree;
    
    /**
     * This is the playpen used within OLAP schema editor.
     */
    private final PlayPen pp;
    
    /**
     * The frame this edit session lives in.
     */
    private JFrame frame;
    
    /**
     * The preferences node for OLAP user settings.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(OLAPEditSession.class);
    
    private static enum PrefKeys {
        /** The current split pane divider location. */
        DIVIDER_LOCATION
    }
    
    /**
     * The scroll pane of which the PlayPen is in.
     */
    private JScrollPane ppScrollPane;
    
    /**
     * OLAPSession associated with the schema that this edit session edits.
     */
    private final OLAPSession olapSession;
    
    /**
     * The undo manager for this OLAP edit session.
     */
    private final OLAPUndoManager undoManager;
    
    public static final double ZOOM_STEP = 0.25;
    
    private ZoomAction zoomInAction;
    private ZoomAction zoomOutAction;
    private ZoomResetAction zoomNormalAction;
    private ZoomToFitAction zoomToFitAction;
    private CreateDimensionAction createDimensionAction;
    private CreateCubeAction createCubeAction;
    private CreateVirtualCubeAction createVirtualCubeAction;
    private CreateMeasureAction createMeasureAction;
    private CreateHierarchyAction createHierarchyAction;
    private CreateLevelAction createLevelAction;
    private CreateDimensionUsageAction createDimensionUsageAction;
    private CreateCubeUsageAction createCubeUsageAction;
    private CreateCalculatedMemberAction createCalculatedMemberAction;
    private ExportSchemaAction exportSchemaAction;
    private OLAPDeleteSelectedAction olapDeleteSelectedAction;
    private AutoLayoutAction autoLayoutAction;
    private UndoAction undoAction;
    private RedoAction redoAction;
    
    private final ArchitectSwingSession swingSession;
    
    private static final Logger logger = Logger.getLogger(OLAPPlayPenFactory.class);


    /**
     * Creates a new editor for the given OLAP schema. The schema's OLAPObjects should
     * all belong to the given session's dbtree and playpen. This should only be called by the
     * ArchitectSwingSession, see {@link ArchitectSwingSession#getOLAPEditSession(OLAPSession)}.
     * 
     * @param session The architect swing session this editor and the given schema belong to.
     * @param olapSession The OLAPSession of the schema to edit
     */
    public OLAPEditSession(ArchitectSwingSession swingSession, OLAPSession olapSession) {
        this.olapSession = olapSession;
        this.swingSession = swingSession;

        // listen for when to remove this from the architect session.
        swingSession.getOLAPRootObject().addSPListener(this);
        
        // add to the architect session's list of edit sessions.
        swingSession.getOLAPEditSessions().add(this);

        tree = new OLAPTree(swingSession, this, olapSession.getSchema());
        tree.setCellRenderer(new OLAPTreeCellRenderer());
        undoManager = new OLAPUndoManager(olapSession);
        undoManager.addChangeListener(new ChangeListener() {           
            public void stateChanged(ChangeEvent e) {
                // this can be called before initGUI() has had a chance to create the frame
                if (frame != null) {
                    frame.setTitle(generateDialogTitle());
                }
            }
        });
        pp = OLAPPlayPenFactory.createPlayPen(swingSession, this, undoManager);
        
        
        swingSession.addSessionLifecycleListener(new SessionLifecycleListener<ArchitectSession>() {
            public void sessionClosing(SessionLifecycleEvent<ArchitectSession> e) {
                close();
            }

            public void sessionOpening(SessionLifecycleEvent<ArchitectSession> e) {
            }
        });
        //Don't initialize GUI here for cases where the Architect's frame has not been created (like loading).
    }
    
    /**
     * Returns the OLAP Schema Editor dialog. It does not come with location and
     * visibility set.
     */
    public JFrame getFrame() {
        if (frame == null) {
            initGUI();
        }
        return frame;
    }
    
    /**
     * Builds the gui components used by this edit session. The PlayPen and OLAPTree
     * will not work properly until this has been called.
     */
    private void initGUI() {
        frame = new JFrame(generateDialogTitle());
        Schema schema = olapSession.getSchema();
        zoomInAction = new ZoomAction(swingSession, pp, ZOOM_STEP);
        zoomOutAction = new ZoomAction(swingSession, pp, ZOOM_STEP * -1.0);
        zoomNormalAction = new ZoomResetAction(swingSession, pp);
        zoomToFitAction = new ZoomToFitAction(swingSession, pp);
        zoomToFitAction.putValue(AbstractAction.SHORT_DESCRIPTION, Messages.getString("ArchitectFrame.zoomToFitActionDescription")); //$NON-NLS-1$
        createDimensionAction = new CreateDimensionAction(swingSession, schema, pp);
        createCubeAction = new CreateCubeAction(swingSession, schema, pp);
        createVirtualCubeAction = new CreateVirtualCubeAction(swingSession, schema, pp);
        createMeasureAction = new CreateMeasureAction(swingSession, pp);
        createHierarchyAction = new CreateHierarchyAction(swingSession, pp);
        createLevelAction = new CreateLevelAction(swingSession, pp);
        createDimensionUsageAction = new CreateDimensionUsageAction(swingSession, pp);
        createCubeUsageAction = new CreateCubeUsageAction(swingSession, pp);
        createCalculatedMemberAction = new CreateCalculatedMemberAction(swingSession, pp);
        exportSchemaAction = new ExportSchemaAction(swingSession, schema);
        olapDeleteSelectedAction = new OLAPDeleteSelectedAction(swingSession, this);
        autoLayoutAction = new AutoLayoutAction(swingSession, pp, "Automatic Layout", "Automatic Layout", "auto_layout"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        autoLayoutAction.setLayout(new FruchtermanReingoldForceLayout());

        undoAction = new UndoAction(swingSession, undoManager);
        redoAction = new RedoAction(swingSession, undoManager);
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.add(zoomInAction);
        toolbar.add(zoomOutAction);
        toolbar.add(zoomNormalAction);
        toolbar.add(zoomToFitAction);
        
        toolbar.addSeparator();

        toolbar.add(undoAction);
        toolbar.add(redoAction);
        
        toolbar.addSeparator();

        toolbar.add(autoLayoutAction);

        toolbar.addSeparator();

        toolbar.add(createDimensionAction);
        toolbar.add(createCubeAction);
        toolbar.add(createVirtualCubeAction);
        toolbar.add(createMeasureAction);
        toolbar.add(createHierarchyAction);
        toolbar.add(createLevelAction);
        toolbar.add(createDimensionUsageAction);
        toolbar.add(createCubeUsageAction);
        toolbar.add(createCalculatedMemberAction);
        
        toolbar.addSeparator();
        
        toolbar.add(exportSchemaAction);
        
        ppScrollPane = new JScrollPane(pp);
        JPanel panel = new JPanel(new BorderLayout());
        final JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                ppScrollPane);
        splitPane.setDividerLocation(prefs.getInt(PrefKeys.DIVIDER_LOCATION.name(), 240));
        splitPane.addPropertyChangeListener("dividerLocation", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                prefs.putInt(PrefKeys.DIVIDER_LOCATION.name(), splitPane.getDividerLocation());
            }
        });
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.EAST);
        
        olapSession.getSchema().addSPListener(new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("name")) {
                    frame.setTitle(generateDialogTitle());
                }
            }
        });
        frame.setContentPane(panel);
        frame.pack();
        
        frame.setIconImage(OSUtils.SCHEMA_ICON.getImage());
        
        // show a new top menu bar for a new OLAP Frame if on MacOSX, as it will be 
        // gone otherwise
        ArchitectSwingSessionContext context = swingSession.getContext();
        if (context.isMacOSX()) {                   
            frame.setJMenuBar(swingSession.getArchitectFrame().createNewMenuBar());
        }
        
        OLAPPlayPenFactory.setupOLAPKeyboardActions(pp, this);
        
        undoManager.rememberPosition();
    }
    
    /**
     * Returns the playpen used within OLAP Schema Editor
     */
    public PlayPen getOlapPlayPen() {
        return pp;
    }
    
    public OLAPTree getOlapTree() {
        return tree;
    }
    
    /**
     * Returns the scroll pane that the PlayPen is in.
     */
    public JScrollPane getPPScrollPane() {
        return ppScrollPane;
    }

    /**
     * Cleans up all resources this session was using. This method will be
     * called automatically when the Architect swing session closes.
     */
    public void close() {
        if (frame != null) {
            frame.dispose();
        }
    }
    
    /**
     * This method must be called just after this session has been saved with
     * the project. It resets the save state, and might do other stuff that
     * client code shouldn't care about.
     */
    public void saveNotify() {
        undoManager.rememberPosition();
    }
    
    /**
     * returns true if any aspect of this session has been modified since it was
     * created or since the last time {@link #saveNotify()} was called, whichever
     * is more recent.
     */
    public boolean isModified() {
        return !undoManager.isAtRememberedPosition();
    }

    /**
     * Returns the schema edit dialog's title that includes the schema's name and
     * an asterisk if the schema has unsaved changes.
     */
    private String generateDialogTitle() {
        StringBuilder title = new StringBuilder();
        if (!undoManager.isAtRememberedPosition() && !swingSession.isEnterpriseSession()) {
            title.append("*");
        }
        title.append(olapSession.getSchema().getName());
        title.append(" - OLAP Schema Editor");
        return title.toString();
    }

    /**
     * Returns the OLAPSession this this edit session edits.
     */
    public OLAPSession getOlapSession() {
        return olapSession;
    }

    // ------ Action getter methods ------ //
    
    public CreateDimensionAction getCreateDimensionAction() {
        return createDimensionAction;
    }

    public CreateCubeAction getCreateCubeAction() {
        return createCubeAction;
    }

    public CreateVirtualCubeAction getCreateVirtualCubeAction() {
        return createVirtualCubeAction;
    }

    public CreateMeasureAction getCreateMeasureAction() {
        return createMeasureAction;
    }

    public CreateHierarchyAction getCreateHierarchyAction() {
        return createHierarchyAction;
    }

    public CreateLevelAction getCreateLevelAction() {
        return createLevelAction;
    }

    public ExportSchemaAction getExportSchemaAction() {
        return exportSchemaAction;
    }
    
    public OLAPDeleteSelectedAction getOLAPDeleteSelectedAction() {
        return olapDeleteSelectedAction;
    }

    public AutoLayoutAction getAutoLayoutAction() {
        return autoLayoutAction;
    }
    
    // ------ OLAPChildListener methods ------ //

    public void childRemoved(SPChildEvent e) {
        if (e.getChild() == olapSession) {
            // remove from architect's list of edit sessions and stop listening
            swingSession.getOLAPEditSessions().remove(this);
            swingSession.getOLAPRootObject().removeSPListener(this);
            frame.dispose();
        }
    }
    
    // -------------------------------------- //
    
    public ZoomAction getZoomInAction() {
        return zoomInAction;
    }

    public ZoomAction getZoomOutAction() {
        return zoomOutAction;
    }

    public ZoomResetAction getZoomNormalAction() {
        return zoomNormalAction;
    }

    public ZoomToFitAction getZoomToFitAction() {
        return zoomToFitAction;
    }

    public CreateDimensionUsageAction getCreateDimensionUsageAction() {
        return createDimensionUsageAction;
    }
    
    public CreateCubeUsageAction getCreateCubeUsageAction() {
        return createCubeUsageAction;
    }
    
    public CreateCalculatedMemberAction getCreateCalculatedMemberAction() {
        return createCalculatedMemberAction;
    }
}
