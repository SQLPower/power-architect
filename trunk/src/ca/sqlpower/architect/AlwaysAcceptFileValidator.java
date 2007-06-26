package ca.sqlpower.architect;

import java.io.File;

/**
 * A simple implementation of FileValidator that will accept any file.
 * This is normally used as a default for when we run headless.
 */
public class AlwaysAcceptFileValidator implements FileValidator {

    public FileValidationResponse acceptFile(File f) {
        return FileValidationResponse.WRITE_OK;
    }

}
