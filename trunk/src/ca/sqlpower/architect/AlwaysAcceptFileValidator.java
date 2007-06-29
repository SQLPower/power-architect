package ca.sqlpower.architect;


/**
 * A simple implementation of FileValidator that will accept any file.
 * This is normally used as a default for when we run headless.
 */
public class AlwaysAcceptFileValidator implements FileValidator {

    public FileValidationResponse acceptFile(String name, String path) {
        return FileValidationResponse.WRITE_OK;
    }

}
