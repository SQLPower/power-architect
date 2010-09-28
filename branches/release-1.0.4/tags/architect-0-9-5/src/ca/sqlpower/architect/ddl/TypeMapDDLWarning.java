package ca.sqlpower.architect.ddl;

import java.util.Arrays;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;

public class TypeMapDDLWarning extends AbstractDDLWarning {

    private SQLColumn column;
    private String message;
    private GenericTypeDescriptor oldType;
    private GenericTypeDescriptor newType;

    public TypeMapDDLWarning(SQLColumn column,
            String message,
            GenericTypeDescriptor oldType, GenericTypeDescriptor td) {
        super(Arrays.asList(new SQLObject[] { column }),
                message, false, null, null);
        this.column = column;
        this.message = message;
        this.oldType = oldType;
        this.newType = td;

    }

}
