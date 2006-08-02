package ca.sqlpower.architect.profile;

import java.util.Date;

public class ProfileResult {

    private long createEndTime;
    private long createStartTime;
    private Exception ex;
    private boolean error; 

    public ProfileResult(long createStartTime) {
        this.createEndTime = -1;
        this.createStartTime = createStartTime;
        ex = null;
        error = false;
    }
    
    public Date getCreateDate() {
        return new Date(createStartTime);
    }

    public long getTimeToCreate() {
        return createEndTime-createStartTime;
    }

    public void setCreateEndTime(long createEndTime) {
        this.createEndTime = createEndTime;
    }
    
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }
    
    
}
