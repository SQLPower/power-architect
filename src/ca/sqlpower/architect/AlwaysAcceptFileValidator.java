package ca.sqlpower.architect;

import java.io.File;

public class AlwaysAcceptFileValidator implements FileValidator {

    public FileValidationResponse acceptFile(File f) {
        return FileValidationResponse.WRITE_OK;
    }

}
