package ca.sqlpower.architect.profile;

import java.util.Date;

public class ProfileResult {

    private Date createDate;
    private long timeToCreate;

    public ProfileResult(long createCost) {
        this.timeToCreate = createCost;
        createDate = new Date();
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
    
    
}
