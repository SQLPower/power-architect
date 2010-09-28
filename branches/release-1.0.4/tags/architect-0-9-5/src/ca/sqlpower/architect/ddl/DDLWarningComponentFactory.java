package ca.sqlpower.architect.ddl;

public class DDLWarningComponentFactory {
    public final static DDLWarningComponent createComponent(DDLWarning warning) {
        return new RenameObjectDDLComponent(warning, null); // XXX
    }
}
