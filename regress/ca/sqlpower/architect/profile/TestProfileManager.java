package ca.sqlpower.architect.profile;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.swingui.ArchitectFrame;

public class TestProfileManager extends TestCase {
    
    public void testProfileManager() throws IOException {
        
        ArchitectFrame.getMainInstance();  // creates an ArchitectFrame, which loads settings
        //FIXME: a better approach would be to have an initialsation method
        // in the business model, which does not depend on the init routine in ArchitectFrame.
        PlDotIni plini = new PlDotIni();
        plini.read(new File("pl.regression.ini"));
        
        ArchitectDataSource ds = plini.getDataSource("regression_test");
        
    }
}
