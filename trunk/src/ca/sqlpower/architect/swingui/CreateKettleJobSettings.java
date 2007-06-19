package ca.sqlpower.architect.swingui;

import java.io.File;

/**
 * This class stores the settings for creating a new Kettle job 
 */
public class CreateKettleJobSettings {
    
    private String jobName;
    private String schemaName;
    private int kettleJoinType;
    private String filePath;
    private File parentFile = new File("");
    
    
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    public int getKettleJoinType() {
        return kettleJoinType;
    }
    public void setKettleJoinType(int kettleJoinType) {
        this.kettleJoinType = kettleJoinType;
    }
    public String getSchemaName() {
        return schemaName;
    }
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    public File getParentFile() {
        return parentFile;
    }
    public void setParentFile(File parentFile) {
        this.parentFile = parentFile;
    }
    
}
