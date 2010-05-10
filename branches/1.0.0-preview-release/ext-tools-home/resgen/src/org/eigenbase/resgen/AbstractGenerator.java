/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/AbstractGenerator.java#3 $
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
import java.util.Date;

/**
 * Abstract base for all generators.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/AbstractGenerator.java#3 $
 */
abstract class AbstractGenerator implements Generator
{
    private final File srcFile;
    private final File file;
    private Boolean scmSafeComments = null;

    public AbstractGenerator(File srcFile, File file)
    {
        this.srcFile = srcFile;
        this.file = file;
    }

    public void setScmSafeComments(boolean enabled)
    {
        if (scmSafeComments != null) {
            throw new AssertionError(
                "SCM safe comment style may only be configured once.");
        }

        scmSafeComments = enabled ? Boolean.TRUE : Boolean.FALSE;
    }

    protected boolean useScmSafeComments()
    {
        return scmSafeComments != null && scmSafeComments.booleanValue();
    }

    /**
     * Generates code for a particular resource.
     */
    protected abstract void generateResource(
        ResourceDef.Resource resource,
        PrintWriter pw);

    protected void generateDoNotModifyHeader(PrintWriter pw) {
        if (useScmSafeComments()) {
            pw.println(
                "// This class is generated. Do NOT modify it manually.");
        } else {
            pw.println("// This class is generated. Do NOT modify it, or");
            pw.println("// add it to source control.");
        }
        pw.println();
    }

    protected void generateGeneratedByBlock(PrintWriter pw) {
        pw.println("/**");
        pw.println(" * This class was generated");
        pw.println(" * by " + ResourceGen.class);

        String file = getSrcFileForComment();
        pw.println(" * from " + file);
        if (!useScmSafeComments()) {
            pw.println(" * on " + new Date().toString() + ".");
        }
        pw.println(" * It contains a list of messages, and methods to");
        pw.println(" * retrieve and format those messages.");
        pw.println(" */");
        pw.println();
    }

    /**
     * Returns the generator's output file.  e.g., "BirthdayResource.java"
     */
    protected File getFile()
    {
        return file;
    }

    /**
     * Returns the XML or .properties source file, in a manner suitable
     * for use in source code comments.  Path information is stripped if
     * SCM-safe comment style is enabled.
     * 
     * @see #setScmSafeComments(boolean)
     */
    protected String getSrcFileForComment()
    {
        String filename = srcFile.toString().replace('\\', '/');
        
        if (useScmSafeComments()) {
            int slashPos = filename.lastIndexOf('/');
            if (slashPos > 0) {
                filename = "..." + filename.substring(slashPos);
            }
        }
        
        return filename;
    }
    
    /**
     * Returns the fully-qualified name of the class being generated,
     * for example "happy.BirthdayResource_en_US".
     */
    protected abstract String getClassName();

    /**
     * Returns the fully-qualified name of the base class.
     */
    protected abstract String getBaseClassName();

    /**
     * Returns a parameter list string, e.g. "String p0, int p1".
     */
    protected String getParameterList(String message) {
        final String [] types = getArgTypes(message);
        if (types.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(type);

            // If this is a C++ pointer type, say "const char *", don't put
            // a space between it and the variable name.
            if (!type.endsWith("&") && !type.endsWith("*")) {
                sb.append(" ");
            }
            sb.append("p");
            sb.append(Integer.toString(i));
        }
        return sb.toString();
    }

    /**
     * Returns the number and types of parameters in the given error message,
     * expressed as an array of Strings (legal values are
     * currently "String", "Number", "java.util.Date", and null) ordered by
     * parameter number.
     */
    protected abstract String [] getArgTypes(String message);

    protected String getArgumentList(String message)
    {
        final String [] types = getArgTypes(message);

        if (types.length == 0) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("p");
            sb.append(Integer.toString(i));
        }
        return sb.toString();
    }

}

// End AbstractGenerator.java
