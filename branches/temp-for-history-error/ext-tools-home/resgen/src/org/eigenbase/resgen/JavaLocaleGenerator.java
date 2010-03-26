/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/JavaLocaleGenerator.java#2 $
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
import java.util.Locale;

/**
 * Generates a Java class for a locale.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/JavaLocaleGenerator.java#2 $
 */
public class JavaLocaleGenerator extends AbstractJavaGenerator
{
    private final Locale locale;

    JavaLocaleGenerator(
        File srcFile,
        File file,
        String className,
        ResourceDef.ResourceBundle resourceBundle,
        Locale locale,
        String baseClassName)
    {
        super(srcFile, file, className, resourceBundle, baseClassName);
        this.locale = locale;
    }

    public void generateModule(ResourceGen generator, ResourceDef.ResourceBundle resourceList, PrintWriter pw)
    {
        generateHeader(pw);
        // e.g. "happy.BirthdayResource_en_US"
        String className = getClassName();
        // e.g. "BirthdayResource_en_US"
        String classNameSansPackage = Util.removePackage(className);
        // e.g. "happy.BirthdayResource"
        final String baseClass = getBaseClassName();
        // e.g. "BirthdayResource"
        String baseClassSansPackage = Util.removePackage(baseClass);
        pw.println("public class " + classNameSansPackage + " extends " + baseClassSansPackage + " {");
        pw.println("    public " + classNameSansPackage + "() throws IOException {");
        pw.println("    }");
        pw.println("}");
        pw.println("");
        generateFooter(pw, classNameSansPackage);
    }

    public void generateResource(ResourceDef.Resource resource, PrintWriter pw)
    {
        throw new UnsupportedOperationException();
    }
}

// End JavaLocaleGenerator.java
