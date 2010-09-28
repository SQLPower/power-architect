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
import java.util.ArrayList;
import java.util.List;

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

    private List<TestBeans> tests;
    
    /**
     * This class holds any failed test info. It allows us to find all the errors in an area before 
     * the assert and so we can fix them all at once.
     */
    private class TestBeans {
        private final String wrongComponent;
        private final String fileName;
        private final String line;
        public TestBeans(String wrongComponent, String fileName, String line) {
            this.wrongComponent = wrongComponent;
            this.fileName = fileName;
            this.line = line;
        }
        public String getWrongComponent() {
            return wrongComponent;
        }
        public String getFileName() {
            return fileName;
        }
        public String getLine() {
            return line;
        }
    }
    /**
     * This test will fail if any .java file in the architect core directory
     * imports a swing class.
     */
    public void testCoreDependencies() throws Exception {
        tests = new ArrayList<TestBeans>();
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
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
                    //This has been added because SnapshotCollection.java imports DomainCategory which is in the enterprise folder.
                    //This shouldn't be in here, but removing it is quite a pain and so it is a special case.
                    if (javaFile.getName().equals("SnapshotCollection.java") && 
                            line.equals("import ca.sqlpower.architect.enterprise.DomainCategory;")) continue;
                    
                    TestBeans t = invalidImports(javaFile, line);
                    if(t != null) tests.add(t);
                    if (line.startsWith("public class")) break;
                } finally {
                    line = reader.readLine();
                }
            }
            if(!tests.isEmpty()) {
                System.out.println("CORE TESTS");
                for(TestBeans t : tests) {
                    System.out.println("File " + t.getFileName() + " contains " +
                            t.getWrongComponent() + " components!\n" + t.getLine());
                }
            }
            assertTrue("You have bad imports :O",tests.isEmpty());
        }
    }
    
    /**
     * This test will fail if any .java file in the architect profile directory
     * imports a swing class.
     */
    public void testProfileDependencies() throws Exception {
        tests = new ArrayList<TestBeans>();
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect/profile");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(javaFile));
            String line = reader.readLine();
            while (line != null) {
                try {
                    TestBeans t = invalidImports(javaFile, line);
                    if (t != null) tests.add(t);
                    if (line.startsWith("public class")) break;
                } finally {
                    line = reader.readLine();
                }
            }
            if(!tests.isEmpty()) {
                System.out.println("PROFILE TESTS");
                for(TestBeans t : tests) {
                    System.out.println("File " + t.getFileName() + " contains " +
                            t.getWrongComponent() + " components!\n" + t.getLine());
                }
            }
            assertTrue(tests.isEmpty());
        }
    }
    
    /**
     * This test will fail if any .java file in the architect ddl directory
     * imports a swing class.
     */
    public void testDDLDependencies() throws Exception {
        tests = new ArrayList<TestBeans>();
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect/ddl");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(javaFile));
            String line = reader.readLine();
            while (line != null) {
                try {
                    //TODO remove these swingui imports
                    //DO NOT ADD TO THIS LIST!!!! Just because there are bad imports in existence
                    //does not mean we are allowed to make things worse!
                    if (javaFile.getName().equals("ObjectPropertyModificationDDLComponent.java") && 
                            line.equals("import ca.sqlpower.architect.swingui.Messages;")) continue;
                    
                    TestBeans t = invalidImports(javaFile, line);
                    if (t != null) tests.add(t);
                } finally {
                    line = reader.readLine();
                }
            }
            if(!tests.isEmpty()) {
                System.out.println("DDL TESTS");
                for(TestBeans t : tests) {
                    System.out.println("File " + t.getFileName() + " contains " +
                            t.getWrongComponent() + " components!\n" + t.getLine());
                }
            }
            assertTrue(tests.isEmpty());
        }
    }

    /**
     * This test will fail if any .java file in the architect diff directory
     * imports a swing class.
     */
    public void testDiffDependencies() throws Exception {
        tests = new ArrayList<TestBeans>();
        File testingDirectory = new File("src/main/java/ca/sqlpower/architect/diff");
        File[] javaFiles = testingDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".java")) return true;
                return false;
            }
        });
        
        for (File javaFile : javaFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(javaFile));
            String line = reader.readLine();
            while (line != null) {
                try {
                    TestBeans t = invalidImports(javaFile, line);
                    if (t != null) tests.add(t);
                    if (line.startsWith("public class")) break;
                } finally {
                    line = reader.readLine();
                }
            }
            if(!tests.isEmpty()) {
                System.out.println("DIFF TESTS");
                for(TestBeans t : tests) {
                    System.out.println("File " + t.getFileName() + " contains " +
                            t.getWrongComponent() + " components!\n" + t.getLine());
                }
            }
            assertTrue(tests.isEmpty());
        }
    }

    /**
     * This tests for all imports in the architect library that should not be in there,
     * and if there is a problem, we add the returned testBeans to a list of errors.
     * @param javaFile the java file containing the error
     * @param line the line of the error
     */
    private TestBeans invalidImports(File javaFile, String line)
    {
        if((line.trim().startsWith("import") && (line.contains("ca.sqlpower.architect.swingui")))) {
            return new TestBeans("swing", javaFile.getName(), line);
        }
        if((line.trim().startsWith("import") && (line.contains("ca.sqlpower.architect.enterprise")))) {
            return new TestBeans("enterprise", javaFile.getName(), line);
        }
        if((line.trim().startsWith("import") && (line.contains("ca.sqlpower.architect.olap")))) {
            return new TestBeans("olap", javaFile.getName(), line);
        }
        return null;
    }
}
