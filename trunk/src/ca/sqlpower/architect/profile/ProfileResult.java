package ca.sqlpower.architect.profile;

import java.util.Date;

public class ProfileResult {

    private Date createDate;
    private long timeToCreate;
    private Exception ex;
    private boolean error; 

    public ProfileResult(long createCost) {
        this.timeToCreate = createCost;
        createDate = new Date();
        ex = null;
        error = false;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public long getTimeToCreate() {
        return timeToCreate;
    }

    public void setTimeToCreate(long timeToCreate) {
        this.timeToCreate = timeToCreate;
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
