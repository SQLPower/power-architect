/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

import junit.framework.TestCase;

/**
 * The core library should not allow any swingui classes to be imported. While
 * this is not the complete limit of what we want to prevent being imported into
 * the core it is a start.
 * <p>
 * At current there are some swingui imports in the core classes. These need to be
 * cleaned up.
 */
public class ArchitectCoreDependencyTest extends TestCase {

    /**
     * This test will fail if any .java file in the architect core directory
     * imports a swing class.
     */
    public void testCoreDependencies() throws Exception {
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
            System.out.println(javaFile);
            BufferedReader reader = new BufferedReader(new FileReader(javaFile));
            String line = reader.readLine();
            while (line != null) {
                try {
                    //TODO remove these swingui imports
                    //DO NOT ADD TO THIS LIST!!!! Just because there are bad imports in existence
                    //does not mean we are allowed to make things worse!
                    if (javaFile.getName().equals("ArchitectUtils.java") && 
                            line.equals("import ca.sqlpower.architect.swingui.ASUtils;")) continue;
                    if (javaFile.getName().equals("CoreUserSettings.java") && 
                            line.equals("import ca.sqlpower.architect.swingui.ArchitectSwingUserSettings;")) continue;
                    if (javaFile.getName().equals("CoreUserSettings.java") && 
                            line.equals("import ca.sqlpower.architect.swingui.QFAUserSettings;")) continue;
                    

                    assertFalse("File " + javaFile + " contains swing components! " + line, 
                            line.trim().startsWith("import") &&  
                            line.contains("ca.sqlpower.architect.swingui"));
                    if (line.startsWith("public class")) break;
                    System.out.println(line);
                } finally {
                    line = reader.readLine();
                }
            }
        }
    }
    
    /**
     * This test will fail if any .java file in the architect profile directory
     * imports a swing class.
     */
    public void testProfileDependencies() throws Exception {
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect/profile");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
            System.out.println(javaFile);
            BufferedReader reader = new BufferedReader(new FileReader(javaFile));
            String line = reader.readLine();
            while (line != null) {
                try {
                    assertFalse("File " + javaFile + " contains swing components! " + line, 
                            line.trim().startsWith("import") &&  
                            line.contains("ca.sqlpower.architect.swingui"));
                    if (line.startsWith("public class")) break;
                    System.out.println(line);
                } finally {
                    line = reader.readLine();
                }
            }
        }
    }
    
    /**
     * This test will fail if any .java file in the architect ddl directory
     * imports a swing class.
     */
    public void testDDLDependencies() throws Exception {
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect/ddl");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
            System.out.println(javaFile);
            BufferedReader reader = new BufferedReader(new FileReader(javaFile));
            String line = reader.readLine();
            while (line != null) {
                try {
                    //TODO remove these swingui imports
                    //DO NOT ADD TO THIS LIST!!!! Just because there are bad imports in existence
                    //does not mean we are allowed to make things worse!
                    if (javaFile.getName().equals("ObjectPropertyModificationDDLComponent.java") && 
                            line.equals("import ca.sqlpower.architect.swingui.Messages;")) continue;
                    
                    assertFalse("File " + javaFile + " contains swing components! " + line, 
                            line.trim().startsWith("import") &&  
                            line.contains("ca.sqlpower.architect.swingui"));
                    if (line.startsWith("public class")) break;
                    System.out.println(line);
                } finally {
                    line = reader.readLine();
                }
            }
        }
    }

    /**
     * This test will fail if any .java file in the architect diff directory
     * imports a swing class.
     */
    public void testDiffDependencies() throws Exception {
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect/diff");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
            System.out.println(javaFile);
            BufferedReader reader = new BufferedReader(new FileReader(javaFile));
            String line = reader.readLine();
            while (line != null) {
                try {
                    assertFalse("File " + javaFile + " contains swing components! " + line, 
                            line.trim().startsWith("import") &&  
                            line.contains("ca.sqlpower.architect.swingui"));
                    if (line.startsWith("public class")) break;
                    System.out.println(line);
                } finally {
                    line = reader.readLine();
                }
            }
        }
    }
    
}
