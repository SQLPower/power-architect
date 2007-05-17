package ca.sqlpower.architect.ddl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.SQLObject;

public class RenameObjectDDLComponent extends GenericDDLWarningComponent {

    final DDLWarning warning;
    JComponent component;
    /**
     * List of text fields that correspond to the name of each
     * SQLObject in the list of involved objects for the warning
     * this component holds.
     */
    final List<JTextField> textFields = new ArrayList<JTextField>();
    Runnable changeApplicator;

    public RenameObjectDDLComponent(DDLWarning warning, Runnable changeApplicator) {
        super(warning);
        this.warning = warning;
        this.changeApplicator = new Runnable() {

            public void run() {
                for (int i = 0; i < textFields.size(); i++) {
                    SQLObject obj = 
                        RenameObjectDDLComponent.this.warning.getInvolvedObjects().get(i);
                    obj.setName(textFields.get(i).getText());
                }
            }
            
        };
        component = new JPanel();
        component.add(getQuickFixButton());                 // XXX anti-pattern
        component.add(new JLabel(warning.getMessage()));
        List<SQLObject> list = warning.getInvolvedObjects();
        for (SQLObject obj : list) {
            JTextField jtf = new JTextField(obj.getName());
            component.add(jtf);
            textFields.add(jtf);
        }
    }

    public void applyChanges() {
        changeApplicator.run();
    }

    public Runnable getChangeApplicator() {
        return changeApplicator;
    }

    public JComponent getComponent() {
        return component;
    }

    public DDLWarning getWarning() {
        return warning;
    }

}
