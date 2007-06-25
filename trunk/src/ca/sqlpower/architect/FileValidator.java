package ca.sqlpower.architect;

import java.io.File;

public interface FileValidator {

    public static enum FileValidationResponse {WRITE_OK, WRITE_NOT_OK, CANCEL, WRITE_OK_ALWAYS, WRITE_NOT_OK_ALWAYS}
    
    public FileValidationResponse acceptFile (File f);
}
