package ca.sqlpower.architect.profile;

public abstract class ProfileResult {

    private long createEndTime = -1L;
    private long createStartTime = -1L;
    private Exception ex;
    private boolean error;

    public ProfileResult() {

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
