/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/CppGenerator.java#3 $
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
 * Generates a C++ class containing resource definitions.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/CppGenerator.java#3 $
 */
class CppGenerator extends AbstractGenerator
{
    private final String className;
    private final String defaultExceptionClassName;
    private final String headerFilename;
    private final String baseClassName;

    private static final String CPP_STRING = "const std::string &";
    private static final String CPP_NUMBER = "int";
    private static final String CPP_DATE_TIME = "time_t";
    private static final String[] CPP_TYPE_NAMES =
        {CPP_STRING, CPP_NUMBER, CPP_DATE_TIME, CPP_DATE_TIME};

    /**
     * Creates a C++ header generator.
     *
     * @param srcFile
     * @param file
     * @param className
     * @param baseClassName Name of base class, must not be null, typically
     * @param defaultExceptionClassName
     */
    CppGenerator(
        File srcFile,
        File file,
        String className,
        String baseClassName,
        String defaultExceptionClassName,
        String headerFilename)
    {
        super(srcFile, file);
        assert className.indexOf('.') < 0 :
                "C++ class name must not contain '.': " + className;
        this.className = className;
        this.defaultExceptionClassName = defaultExceptionClassName;
        this.headerFilename = headerFilename;
        assert baseClassName != null;
        this.baseClassName = baseClassName;
    }

    protected String getClassName()
    {
        return className;
    }

    protected String getBaseClassName()
    {
        return baseClassName;
    }

    protected String[] getArgTypes(String message) {
        return ResourceDefinition.getArgTypes(message, CPP_TYPE_NAMES);
    }

    public void generateModule(ResourceGen generator, ResourceDef.ResourceBundle resourceList, PrintWriter pw)
    {
        generateDoNotModifyHeader(pw);
        generateGeneratedByBlock(pw);

        String className = getClassName();
        String bundleCacheClassName = className + "BundleCache";
        String baseClass = getBaseClassName();

        if (resourceList.cppCommonInclude != null) {
            pw.println(
                "// begin common include specified by " 
                + getSrcFileForComment());
            pw.println("#include \"" + resourceList.cppCommonInclude + "\"");
            pw.println(
                "// end common include specified by " 
                + getSrcFileForComment());
        }

        pw.println("#include \"" + headerFilename + "\"");
        pw.println("#include \"ResourceBundle.h\"");
        pw.println("#include \"Locale.h\"");
        pw.println();
        pw.println("#include <map>");
        pw.println("#include <string>");
        pw.println();

        if (resourceList.cppNamespace != null) {
            pw.println("namespace " + resourceList.cppNamespace + " {");
            pw.println();
        }

        pw.println("using namespace std;");
        pw.println();
        pw.println("#define BASENAME (\"" + className + "\")");
        pw.println();
        pw.println("static " + bundleCacheClassName + " bundleCache;");
        pw.println("static string bundleLocation(\"\");");
        pw.println();

        pw.println("const " + className + " &" + className + "::instance()");
        pw.println("{");
        pw.println("    return " + className + "::instance(Locale::getDefault());");
        pw.println("}");
        pw.println();
        pw.println("const " + className
            + " &" + className + "::instance(const Locale &locale)");
        pw.println("{");
        pw.println("    return *makeInstance<"
            + className + ", "
            + bundleCacheClassName + ", "
            + bundleCacheClassName
            + "::iterator>(bundleCache, locale);");
        pw.println("}");
        pw.println();
        pw.println("void "
            + className
            + "::setResourceFileLocation(const string &location)");
        pw.println("{");
        pw.println("    bundleLocation = location;");
        pw.println("}");
        pw.println();

        pw.println("" + className + "::" + className + "(Locale locale)");
        pw.println("    : " + baseClass
            + "(BASENAME, locale, bundleLocation),");

        for(int i = 0; i < resourceList.resources.length; i++) {
            ResourceDef.Resource resource = resourceList.resources[i];

            pw.print("      _"
                + resource.name
                + "(this, \""
                + resource.name
                + "\")");

            if (i + 1 < resourceList.resources.length) {
                pw.println(',');
            } else {
                pw.println();
            }
        }
        pw.println("{ }");
        pw.println();

        for (int i = 0; i < resourceList.resources.length; i++) {
            generateResource(resourceList.resources[i], pw);
        }

        if (resourceList.cppNamespace != null) {
            pw.println();
            pw.println("} // end namespace " + resourceList.cppNamespace);
        }
    }

    public void generateResource(
        ResourceDef.Resource resource,
        PrintWriter pw)
    {
        String text = resource.text.cdata;
        //String comment = ResourceGen.getComment(resource);

        // e.g. "Internal"
        final String resourceInitCap =
            ResourceGen.getResourceInitcap(resource);

        String parameterList = getParameterList(text);
        String argumentList = getArgumentList(text);

        pw.println("string " + className + "::" + resource.name + "("
            + parameterList + ") const");
        pw.println("{");
        pw.println("    return _"
            + resource.name
            + ".format("
            + argumentList
            + ");");
        pw.println("}");

        if (resource instanceof ResourceDef.Exception) {
            ResourceDef.Exception exception =
                (ResourceDef.Exception)resource;

            String exceptionClass = exception.cppClassName;
            if (exceptionClass == null) {
                exceptionClass = defaultExceptionClassName;
            }

            pw.println(exceptionClass + "* "
                + className + "::new" + resourceInitCap + "("
                + parameterList + ") const");
            pw.println("{");
            pw.println("    return new "
                + exceptionClass
                + "("
                + resource.name
                + "("
                + argumentList
                + "));");
            pw.println("}");
            pw.println();

            boolean chainExceptions =
                (exception.cppChainExceptions != null &&
                exception.cppChainExceptions.equalsIgnoreCase("true"));

            if (chainExceptions) {
                if (parameterList.length() > 0) {
                    pw.println(exceptionClass
                        + "* "
                        + className
                        + "::new"
                        + resourceInitCap
                        + "("
                        + parameterList
                        + ", const "
                        + exceptionClass
                        + " * const prev) const");
                } else {
                    pw.println(exceptionClass
                        + " "
                        + className
                        + "::new"
                        + resourceInitCap
                        + "(const "
                        + exceptionClass
                        + " * const prev) const");
                }
                pw.println("{");

                pw.println("    return new "
                    + exceptionClass
                    + "("
                    + resource.name
                    + "("
                    + argumentList
                    + "), prev);");
                pw.println("}");
                pw.println();
            }
        }
    }
}

// End CppGenerator.java
