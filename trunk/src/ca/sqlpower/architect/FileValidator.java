package ca.sqlpower.architect;

/**
 *  This interface is used for handling when a file is to be created
 *   but already exists. This class can be implemented to handle
 *   file overwrites in different ways, such as overwriting or not. 
 *   One example is the AlwaysAcceptFileValidator which always
 *   returns a WRITE_OK response.
 *   
 *   This class is also used to handle the desired overwriting when
 *   saving to a repository.
 */
public interface FileValidator {

    public static enum FileValidationResponse {WRITE_OK                 //This response denotes that the file should be overwritten
        
                                               , WRITE_NOT_OK           //This response denotes that the file should not be overwritten
                                               
                                               , CANCEL                 //This response denotes that the file should not be overwritten
                                                                        //and no more files should be written to
                                               
                                               , WRITE_OK_ALWAYS        //This response denotes that the file and all remaining files
                                                                        //should be overwritten
                                               
                                               , WRITE_NOT_OK_ALWAYS    //This response denotes that no further files should be overwritten
                                                                        //including the current one
                                               }

    /**
     * This method decides if a given file should be overwritten or not
     * based on the string name and its path.
     */
    public FileValidationResponse acceptFile (String name, String path);
}
