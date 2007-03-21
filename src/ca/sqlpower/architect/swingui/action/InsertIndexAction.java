package ca.sqlpower.architect.swingui.action;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.IndexEditPanel;
import ca.sqlpower.architect.swingui.TablePane;

public class InsertIndexAction extends AbstractTableTargetedAction {

    private static final Logger logger = Logger.getLogger(InsertColumnAction.class);
    
    public InsertIndexAction() {
        super("New Index");
        putValue(SHORT_DESCRIPTION, "New Index");
        putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        setEnabled(false);
    }

    
    @Override
    public void disableAction() {
        setEnabled(false);
        logger.debug("Disabling New Index Action");
        putValue(SHORT_DESCRIPTION, "New Index");
    }


    @Override
    void processSQLObject(SQLObject so) throws ArchitectException {
        if (so instanceof SQLTable) {
            makeDialog((SQLTable) so);
        }
    }

    @Override
    void processTablePane(TablePane tp) throws ArchitectException {
        System.err.println("processTablePane tp.getModel(): "+tp.getModel());
        makeDialog(tp.getModel());
    }
    
    private void makeDialog(SQLTable parent) throws ArchitectException {
        final JDialog d;
        SQLIndex index = new SQLIndex();
        final IndexEditPanel editPanel = new IndexEditPanel(index, parent);
  
        d = ArchitectPanelBuilder.createArchitectPanelDialog(
                editPanel, ArchitectFrame.getMainInstance(),
                "Index Properties", "OK");
        d.pack();
        d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
        d.setVisible(true);
    }
}
