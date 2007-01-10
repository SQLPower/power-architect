/*
 * Created on Jan 9, 2007
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.antbuild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task for adding a Software Licence Agreement to an existing
 * Mac OS X disk image (DMG) file.  This task will only work on a
 * Mac that has the Developer Tools installed (specifically, the
 * /Developer/Tools/Rez utility must exist).  If the Rez executable
 * can't be found, the task will throw a reasonable error message.
 *
 * @author fuerth
 * @version $Id$
 */
public class AddLicenseToDMGTask extends Task {

    /**
     * Location of the hdiutil executable.  The default value of
     * "/usr/bin/hdiutil" should be appropriate for all OS X systems.
     */
    private File hdiutilExecutable = new File("/usr/bin/hdiutil");
    
    /**
     * Location of the Rez executable.  The default value of
     * "Developer/Tools/Rez" is the default install location for
     * the OS X developer tools package.
     */
    private File rezExecutable = new File("/Developer/Tools/Rez");
    
    /**
     * The text file containing the license agreement.
     */
    private File licenseFile;
    
    /**
     * The DMG file that should have the license attached to it.
     */
    private File dmgFile;
    
    /**
     * The path to a file containing all the boilerplate resource entries
     * which are required to activate the "SLA" panel when Disk Copy opens
     * a DMG file.  The resources should be in the Rez/DeRez file format.
     */
    private File resourceTemplateFile;
    
    /**
     * Controls some debugging options for this task.  When true, the
     * task will:
     * <ul>
     *   <li>Print the path to the temporary Rez file that gets created
     *   <li>Not delete the temp file so it can be examined after the ant
     *       build is complete
     * </ul>
     */
    boolean debug = false;
    
    @Override
    public void execute() throws BuildException {
        if (!rezExecutable.exists()) {
            throw new BuildException(
                    "Rez executable not found at \""+rezExecutable.getPath()+"\". " +
                    "You need to install the OS X Developer Tools.");
        }
        File tempFile = null;
        try {
            execv(hdiutilExecutable.getPath(),
                  "unflatten",
                  dmgFile.getPath());

            tempFile = File.createTempFile("dmgLicenseResource", "tmp");
            if (debug) {
                System.out.println("Temporary file path: "+tempFile.getPath());
            }
            execv("cp", resourceTemplateFile.getPath(), tempFile.getPath());
            
            appendLicenseToTemplate(tempFile);
            
            execv(rezExecutable.getPath(),
                    "-a", tempFile.getPath(),
                    "-o", dmgFile.getPath());

            execv(hdiutilExecutable.getPath(),
                    "flatten",
                    dmgFile.getPath());

        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        } finally {
            if (tempFile != null && !debug) {
                tempFile.delete();
            }
        }
    }
    
    public void setDmgFile(File dmgFile) {
        this.dmgFile = dmgFile;
    }
    
    public void setLicenseFile(File licenseFile) {
        this.licenseFile = licenseFile;
    }
    
    public void setRezExecutable(File rezExecutable) {
        this.rezExecutable = rezExecutable;
    }

    public void setResourceTemplateFile(File resourceTemplateFile) {
        this.resourceTemplateFile = resourceTemplateFile;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    /**
     * Convenience wrapper for doing a Runtime.exec() with varargs.
     * Prints the command and its exit value to System.out if debugging
     * is on.
     */
    private void execv(String ... args) throws IOException {
        StringBuilder command = new StringBuilder();
        for (String arg : args) {
            command.append(" \"").append(arg).append("\"");
        }
        
        if (debug) {
            System.out.println("Executing:"+command);
        }
        long startTime = System.currentTimeMillis();
        Process proc = Runtime.getRuntime().exec(args);
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Warning: interrupted during proc.wait()");
        }
        int exitVal = proc.exitValue();
        if (debug) {
            long finshTime = System.currentTimeMillis();
            System.out.println("Completed in "+(finshTime-startTime)+"ms with exit code "+exitVal);
        }
        if (exitVal != 0) {
            throw new RuntimeException("Command failed with exit code "+exitVal+":"+command);
        }
    }
    
    /**
     * Appends the currently-configured license text file to the given
     * file (which should be a fresh copy of the resource template file).
     * The file contents will be appended to the outFile in the "Rez" style
     * resource entry format (basically a hex dump).
     */
    private void appendLicenseToTemplate(File outFile) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(outFile, true));
        Reader in = new FileReader(licenseFile);
        out.println("data 'TEXT' (5000, \"English SLA\") {");
        int nextByte;
        int count = 0;
        while ( (nextByte = in.read()) >= 0 ) {
            if (count % 16 == 0) {
                out.print("        $\"");
            }
            
            out.format("%02X", (nextByte & 0xff));
            count++;
            
            if (count % 16 == 0) {
                out.println("\"");
            } else if (count % 2 == 0) {
                out.print(" ");
            }
        }
        
        if (count % 16 != 0) {
            out.println("\"");
        }
        out.println("};");
        
        out.flush();
        out.close();
    }
}
