package ca.sqlpower.architect.profile;

import ca.sqlpower.architect.SQLObject;

public abstract class ProfileResult<T extends SQLObject> {

    private T profiledObject;
    private long createEndTime = -1L;
    private long createStartTime = -1L;
    private Exception ex;
    private boolean error;

    /**
     * Creates a new ProfileResult which will hold profile data about the given SQL Object.
     * 
     * @param profiledObject The object that this profile data refers to.  Must not be null.
     */
    public ProfileResult(T profiledObject) {
        if (profiledObject == null) throw new NullPointerException("The profiled object has to be non-null");
        this.profiledObject = profiledObject;
    }

    public T getProfiledObject() {
        return profiledObject;
    }
    
    public long getCreateStartTime() {
        return createStartTime;
    }

    public void setCreateStartTime(long createStartTime) {
        this.createStartTime = createStartTime;
    }

    public long getTimeToCreate() {
        return createEndTime-createStartTime;
    }

    public void setCreateEndTime(long createEndTime) {
        this.createEndTime = createEndTime;
    }

    public long getCreateEndTime() {
        return createEndTime;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Exception getException() {
        return ex;
    }

    public void setException(Exception ex) {
        this.ex = ex;
    }
}
