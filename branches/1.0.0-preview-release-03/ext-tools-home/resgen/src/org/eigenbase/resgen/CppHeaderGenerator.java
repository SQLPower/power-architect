/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/CppHeaderGenerator.java#3 $
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
 * Generates a C++ header file containing resource definitions.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/CppHeaderGenerator.java#3 $
 */
public class CppHeaderGenerator extends CppGenerator
{
    /**
     * Creates a C++ header generator.
     *
     * @param srcFile
     * @param file
     * @param className
     * @param baseClassName Name of base class, must not be null, typically
     * @param defaultExceptionClassName
     */
    public CppHeaderGenerator(
        File srcFile,
        File file,
        String className,
        String baseClassName,
        String defaultExceptionClassName)
    {
        super(srcFile, file, className, baseClassName, 
            defaultExceptionClassName, null);
    }

    public void generateModule(
        ResourceGen generator,
        ResourceDef.ResourceBundle resourceList,
        PrintWriter pw)
    {
        generateDoNotModifyHeader(pw);
        generateGeneratedByBlock(pw);

        StringBuffer ifndef = new StringBuffer();
        String fileName = getFile().getName();
        ifndef.append(fileName.substring(0, fileName.length() - 2));
        ifndef.append("_Included");
        if (resourceList.cppNamespace != null) {
            ifndef.insert(0, '_');
            ifndef.insert(0, resourceList.cppNamespace.substring(1));
            ifndef.insert(0, Character.toUpperCase(resourceList
                                                   .cppNamespace
                                                   .charAt(0)));
        }

        pw.println("#ifndef " + ifndef.toString());
        pw.println("#define " + ifndef.toString());
        pw.println();
        pw.println("#include <ctime>");
        pw.println("#include <string>");
        pw.println();
        pw.println("#include \"Locale.h\"");
        pw.println("#include \"ResourceDefinition.h\"");
        pw.println("#include \"ResourceBundle.h\"");
        pw.println();

        pw.println("// begin includes specified by " + getSrcFileForComment());
        if (resourceList.cppExceptionClassLocation != null) {
            pw.println("#include \""
                       + resourceList.cppExceptionClassLocation
                       + "\"");
        }

        for(int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            if (resource instanceof ResourceDef.Exception) {
                ResourceDef.Exception exception =
                    (ResourceDef.Exception)resource;

                if (exception.cppClassLocation != null) {
                    pw.println("#include \""
                               + exception.cppClassLocation
                               + "\"");
                }
            }
        }
        pw.println("// end includes specified by " + getSrcFileForComment());
        pw.println();
        if (resourceList.cppNamespace != null) {
            pw.println("namespace " + resourceList.cppNamespace + " {");
            pw.println();
        }

        pw.println();

        String baseClass = getBaseClassName();
        String className = getClassName();
        String bundleCacheClassName = className + "BundleCache";

        pw.println("class " + className + ";");
        pw.println("typedef map<Locale, " + className + "*> "
                   + bundleCacheClassName + ";");
        pw.println();
        pw.println("class " + className + " : " + baseClass);
        pw.println("{");
        pw.println("    protected:");
        pw.println("    explicit " + className + "(Locale locale);");
        pw.println();
        pw.println("    public:");
        pw.println("    virtual ~" + className + "() { }");
        pw.println();
        pw.println("    static const " + className + " &instance();");
        pw.println("    static const "
                   + className
                   + " &instance(const Locale &locale);");
        pw.println();

        pw.println("    static void setResourceFileLocation(const std::string &location);");
        pw.println();

        for(int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            String text = resource.text.cdata;
            String comment = ResourceGen.getComment(resource);
            String parameterList = getParameterList(text);

            // e.g. "Internal"
            final String resourceInitCap =
                ResourceGen.getResourceInitcap(resource);

            Util.generateCommentBlock(pw, resource.name, text, comment);

            pw.println("    std::string " + resource.name + "("
                       + parameterList + ") const;");

            if (resource instanceof ResourceDef.Exception) {
                ResourceDef.Exception exception =
                    (ResourceDef.Exception)resource;

                String exceptionClass = exception.cppClassName;
                if (exceptionClass == null) {
                    exceptionClass = resourceList.cppExceptionClassName;
                }

                pw.println("    " + exceptionClass
                           + "* new" + resourceInitCap + "("
                           + parameterList + ") const;");

                boolean chainExceptions =
                    (exception.cppChainExceptions != null &&
                     exception.cppChainExceptions.equalsIgnoreCase("true"));

                if (chainExceptions) {
                    if (parameterList.length() > 0) {
                        pw.println("    "
                                   + exceptionClass
                                   + "* new"
                                   + resourceInitCap
                                   + "("
                                   + parameterList
                                   + ", const "
                                   + exceptionClass
                                   + " * const prev) const;");
                    } else {
                        pw.println("  "
                                   + exceptionClass
                                   + " new"
                                   + resourceInitCap + "("
                                   + "const "
                                   + exceptionClass
                                   + " * const prev) const;");
                    }
                }
            }

            pw.println();
        }

        pw.println("    private:");
        for(int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            pw.println("    ResourceDefinition _" + resource.name + ";");
        }
        pw.println();

        pw.println("    template<class _GRB, class _BC, class _BC_ITER>");
        pw.println("        friend _GRB *makeInstance(_BC &bundleCache, const Locale &locale);");

        pw.println("};");


        if (resourceList.cppNamespace != null) {
            pw.println();
            pw.println("} // end namespace " + resourceList.cppNamespace);
        }

        pw.println();
        pw.println("#endif // " + ifndef.toString());
    }

}

// End CppHeaderGenerator.java
