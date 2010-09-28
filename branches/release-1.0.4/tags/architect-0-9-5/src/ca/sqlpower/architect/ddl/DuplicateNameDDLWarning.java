package ca.sqlpower.architect.ddl;

import java.util.List;

import ca.sqlpower.architect.SQLObject;

/**
 * A DDLWarning for name duplications that can be fixed by calling setName() on
 * one of the involved objects
 */
public class DuplicateNameDDLWarning extends AbstractDDLWarning {

    protected String whatQuickFixShouldCallIt;

    public DuplicateNameDDLWarning(String message,
            List<SQLObject> involvedObjects,
            String quickFixMesssage,
            SQLObject whichObjectQuickFixRenames,
            String whatQuickFixShouldCallIt)
    {
        super(involvedObjects, message,
                true,
                quickFixMesssage,
                whichObjectQuickFixRenames);
        this.whatQuickFixShouldCallIt = whatQuickFixShouldCallIt;
    }

    public boolean quickFix() {
        // XXX need differentiator for setName() vs setPhysicalName()
        whichObjectQuickFixFixes.setName(whatQuickFixShouldCallIt);
        return true;
    }
}
