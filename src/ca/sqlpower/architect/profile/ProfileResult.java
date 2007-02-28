package ca.sqlpower.architect.profile;

import java.sql.SQLException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;

public interface ProfileResult<T extends SQLObject> {

    public abstract T getProfiledObject();

    public abstract long getCreateStartTime();

    public abstract void setCreateStartTime(long createStartTime);

    public abstract long getTimeToCreate();

    public abstract void setCreateEndTime(long createEndTime);

    public abstract long getCreateEndTime();

    public abstract boolean isError();

    public abstract void setError(boolean error);

    public abstract Exception getException();

    public abstract void setException(Exception ex);

    public void populate();

    public void doProfile() throws SQLException, ArchitectException;

}