package ca.sqlpower.architect.ddl;

import java.util.List;

import ca.sqlpower.architect.SQLObject;

public abstract class AbstractDDLWarning implements DDLWarning {

    protected List<SQLObject> involvedObjects;
    protected String message;
    boolean fixed;
    protected boolean isQuickFixable;
    protected String quickFixMesssage;
    protected SQLObject whichObjectQuickFixFixes;

    public AbstractDDLWarning(List<SQLObject> involvedObjects,
            String message, boolean isQuickFixable,
            String quickFixMesssage, SQLObject whichObjectQuickFixFixes) {
        super();
        this.involvedObjects = involvedObjects;
        this.message = message;
        this.isQuickFixable = isQuickFixable;
        this.quickFixMesssage = quickFixMesssage;
        this.whichObjectQuickFixFixes = whichObjectQuickFixFixes;
    }


    public List<SQLObject> getInvolvedObjects() {
        return involvedObjects;
    }

    public String getMessage() {
        return message;
    }

    public String getQuickFixMessage() {
        return quickFixMesssage;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isQuickFixable() {
        return isQuickFixable;
    }

    /** Dummy version for subclasses that are not quickfixable */
    public boolean quickFix() {
        throw new IllegalStateException("Called generic version of quickFix");
    }
}
