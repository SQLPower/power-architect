package ca.sqlpower.architect.swingui;

import java.io.File;

/**
 * This class stores the settings for creating a new Kettle job 
 */
public class CreateKettleJobSettings {
    
    /**
     * The name of the Kettle job
     */
    private String jobName;
    
    /**
     * The name of the target schema
     */
    private String schemaName;
    
    /**
     * The default join type for Kettle. The join types are stored as int as the values
     * are in an array in Kettle.
     */
    private int kettleJoinType;
    
    /**
     * The path to store the Kettle job at
     */
    private String filePath;
    
    /**
     * The file that represents the directory of the new Kettle job. This is set to 
     * prevent null pointer exceptions when first opening the Create Kettle Job window.
     */
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
