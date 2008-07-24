/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/AbstractJavaGenerator.java#2 $
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
import java.io.PrintWriter;

/**
 * Abstract base for all generators which generate Java code.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/AbstractJavaGenerator.java#2 $
 */
abstract class AbstractJavaGenerator extends AbstractGenerator
{
    protected final String className;
    protected final ResourceDef.ResourceBundle resourceBundle;
    protected final String baseClassName;

    private static final String JAVA_STRING = "String";
    private static final String JAVA_NUMBER = "Number";
    private static final String JAVA_DATE_TIME = "java.util.Date";
    private static final String[] JAVA_TYPE_NAMES =
        {JAVA_STRING, JAVA_NUMBER, JAVA_DATE_TIME, JAVA_DATE_TIME};

    AbstractJavaGenerator(
        File srcFile,
        File file,
        String className,
        ResourceDef.ResourceBundle resourceBundle,
        String baseClassName)
    {
        super(srcFile, file);
        this.className = className;
        this.baseClassName = baseClassName;
        this.resourceBundle = resourceBundle;
    }

    /**
     * Returns the type of error which is to be thrown by this resource.
     * Result is null if this is not an error.
     */
    protected String getErrorClass(
            ResourceDef.Exception exception) {
        if (exception.className != null) {
            return exception.className;
        } else if (resourceBundle.exceptionClassName != null) {
            return resourceBundle.exceptionClassName;
        } else {
            return "java.lang.RuntimeException";
        }
    }

    protected String getPackageName()
    {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        } else {
            return className.substring(0,lastDot);
        }
    }

    protected String[] getArgTypes(String message) {
        return ResourceDefinition.getArgTypes(message, JAVA_TYPE_NAMES);
    }

    protected void generateHeader(PrintWriter pw) {
        generateDoNotModifyHeader(pw);
        String packageName = getPackageName();
        if (packageName != null) {
            pw.println("package " + packageName + ";");
        }
        pw.println("import java.io.IOException;");
        pw.println("import java.util.Locale;");
        pw.println("import org.eigenbase.resgen.*;");
        pw.println();
        generateGeneratedByBlock(pw);
    }

    protected void generateFooter(PrintWriter pw, String className) {
        pw.println("// End " + className + ".java");
    }

    protected String getClassName()
    {
        return className;
    }

    protected String getBaseClassName()
    {
        return baseClassName;
    }
}

// End AbstractJavaGenerator.java
