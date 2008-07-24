/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/FileTask.java#4 $
// Package org.eigenbase.resgen is an i18n resource generator.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
// Portions Copyright (C) 2001-2005 Kana Software, Inc. and others.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 2 of the License, or (at your
// option) any later version approved by The Eigenbase Project.
//
// This library is distributed in the hope that it will be useful, 
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.eigenbase.resgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import org.apache.tools.ant.BuildException;

/**
 * Abstract base class for an Ant task which processes a file containing
 * resources.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/FileTask.java#4 $
 */
abstract class FileTask 
{
    ResourceGenTask.Include include;
    String className;
    String fileName;

    String cppClassName;

    boolean outputJava;
    boolean outputCpp;

    abstract void process(ResourceGen generator) throws IOException;

    /**
     * Returns the XML source file, e.g. happy/BirthdayResource_en.xml.
     */
    File getFile() {
        return new File(include.root.src, fileName);
    }

    /**
     * Returns the XML source file, mangled for use in comments.
     * e.g. .../BirthdayResource_en.xml if SCM-safe comments are enabled.
     */
    String getFileForComments()
    {
        String file = getFile().toString().replace('\\', '/');
        
        if (include.root.commentStyle == 
            	ResourceGenTask.COMMENT_STYLE_SCM_SAFE) {
            int slashPos = file.lastIndexOf('/');
            if (slashPos > 0) {
                file = "..." + file.substring(slashPos);
            }
        }

        return file;
    }

    boolean checkUpToDate(ResourceGen generator, File file) {
        if (file.exists() &&
            file.lastModified() >= getFile().lastModified()) {
            generator.comment(file + " is up to date");
            return true;
        }

        return false;
    }

    void makeParentDirs(File file)
    {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
    }

    private String getPackageName()
    {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        } else {
            return className.substring(0, lastDot);
        }
    }

    private File getPackageDirectory(File file)
    {
        final String packageName = getPackageName();
        if (packageName == null) {
            return file;
        }
        return new File(file, packageName.replace('.', Util.fileSep));
    }

    /**
     * Returns the directory from which to read source files.
     */
    File getSrcDirectory()
    {
        return getPackageDirectory(include.root.src);
    }

    /**
     * Returns the directory to which to generate Java or C++ files.
     */
    File getDestDirectory()
    {
        return getPackageDirectory(include.root.dest);
    }


    /**
     * Returns the directory to which to generate .properties and .xml
     * files.
     */
    File getResourceDirectory()
    {
        return getPackageDirectory(include.root.res);
    }

    /**
     * Generates a Java class, e.g. com/foo/MyResource.java or
     * com/foo/MyResource_en_US.java, depending upon whether locale is
     * null.
     */
    void generateJava(
            ResourceGen generator,
            ResourceDef.ResourceBundle resourceList,
            Locale locale) {
        String fileName = Util.getClassNameSansPackage(className, locale) +
            ".java";
        File file = new File(getDestDirectory(), fileName);

        if (!include.root.force &&
            checkUpToDate(generator, file)) {
            return;
        }

        generator.comment("Generating " + file);
        final FileOutputStream out;
        try {
            makeParentDirs(file);

            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new BuildException("Error while writing " + file, e);
        }
        PrintWriter pw = new PrintWriter(out);
        try {
            Generator gen;
            if (locale == null) {
                String baseClassName = include.baseClassName;
                if (baseClassName == null) {
                    baseClassName = "org.eigenbase.resgen.ShadowResourceBundle";
                }
                switch (include.root.style) {
                case ResourceGenTask.STYLE_DYNAMIC:
                    gen = new JavaBaseGenerator(getFile(), file,
                        className, baseClassName, resourceList);
                    break;
                case ResourceGenTask.STYLE_FUNCTOR:
                    gen = new JavaFunctorBaseGenerator(getFile(), file,
                        className, baseClassName, resourceList);
                    break;
                default:
                    throw new AssertionError("unexpected style " +
                        include.root.style);
                }
            } else {
                // e.g. "mondrian.resource.MondrianResource_en_US"
                String className = this.className + "_" + locale.toString();
                // e.g. "mondrian.resource.MondrianResource"
                String baseClassName = this.className;
                gen = new JavaLocaleGenerator(getFile(), file, className,
                    resourceList, locale, baseClassName);
            }

            configureCommentStyle(gen);
            
            gen.generateModule(generator, resourceList, pw);
        } finally {
            pw.close();
        }
    }
    
    protected void configureCommentStyle(Generator gen)
    {
        switch(include.root.commentStyle) {
        case ResourceGenTask.COMMENT_STYLE_NORMAL:
            gen.setScmSafeComments(false);
            break;

        case ResourceGenTask.COMMENT_STYLE_SCM_SAFE:
            gen.setScmSafeComments(true);
            break;

        default:
            throw new AssertionError(
                "unexpected comment style " + include.root.commentStyle);
        }

    }
}
