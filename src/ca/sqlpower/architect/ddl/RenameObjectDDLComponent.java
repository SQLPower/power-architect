package ca.sqlpower.architect.ddl;

import java.util.List;

import javax.swing.*;

import ca.sqlpower.architect.SQLObject;

public class RenameObjectDDLComponent extends GenericDDLWarningComponent {

    DDLWarning warning;
    JComponent component;
    Runnable changeApplicator;

    public RenameObjectDDLComponent(DDLWarning warning, Runnable changeApplicator) {
        super(warning);
        this.warning = warning;
        this.changeApplicator = changeApplicator;
        component = new JPanel();
        component.add(getQuickFixButton());                 // XXX anti-pattern
        component.add(new JLabel(warning.getMessage()));
        List<SQLObject> list = warning.getInvolvedObjects();
        for (int i = 0; i < list.size(); i++) {
            component.add(new JTextField(list.get(i).getName()));
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
