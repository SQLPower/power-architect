/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/JavaBaseGenerator.java#3 $
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
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;

/**
 * Generates a Java class for the base locale.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/JavaBaseGenerator.java#3 $
 */
class JavaBaseGenerator extends AbstractJavaGenerator
{
    protected final Set warnedClasses = new HashSet();

    JavaBaseGenerator(
        File srcFile,
        File file,
        String className,
        String baseClassName,
        ResourceDef.ResourceBundle resourceBundle)
    {
        super(srcFile, file, className, resourceBundle, baseClassName);
    }

    public void generateModule(
        ResourceGen generator,
        ResourceDef.ResourceBundle resourceList, PrintWriter pw)
    {
        generateHeader(pw);
        String className = getClassName();
        final String classNameSansPackage = Util.removePackage(className);
        pw.print("public class " + classNameSansPackage);
        final String baseClass = getBaseClassName();
        if (baseClass != null) {
            pw.print(" extends " + baseClass);
        }
        pw.println(" {");
        pw.println("    public " + classNameSansPackage + "() throws IOException {");
        pw.println("    }");
        pw.println("    private static final String baseName = " + Util.quoteForJava(getClassName()) + ";");
        pw.println("    /**");
        pw.println("     * Retrieves the singleton instance of {@link " + classNameSansPackage + "}. If");
        pw.println("     * the application has called {@link #setThreadLocale}, returns the");
        pw.println("     * resource for the thread's locale.");
        pw.println("     */");
        pw.println("    public static synchronized " + classNameSansPackage + " instance() {");
        pw.println("        return (" + classNameSansPackage + ") instance(baseName);");
        pw.println("    }");
        pw.println("    /**");
        pw.println("     * Retrieves the instance of {@link " + classNameSansPackage + "} for the given locale.");
        pw.println("     */");
        pw.println("    public static synchronized " + classNameSansPackage + " instance(Locale locale) {");
        pw.println("        return (" + classNameSansPackage + ") instance(baseName, locale);");
        pw.println("    }");
        if (resourceList.code != null) {
            pw.println("    // begin of included code");
            pw.print(resourceList.code.cdata);
            pw.println("    // end of included code");
        }

        for (int j = 0; j < resourceList.resources.length; j++) {
            ResourceDef.Resource resource = resourceList.resources[j];
            generateResource(resource, pw);
        }
        pw.println("");
        postModule(pw);
        pw.println("}");
    }

    protected void postModule(PrintWriter pw)
    {
    }

    public void generateResource(ResourceDef.Resource resource, PrintWriter pw)
    {
        if (resource.text == null) {
            throw new BuildException(
                    "Resource '" + resource.name + "' has no message");
        }
        String text = resource.text.cdata;
        String comment = ResourceGen.getComment(resource);
        final String resourceInitcap = ResourceGen.getResourceInitcap(resource);// e.g. "Internal"

        String definitionClass = "org.eigenbase.resgen.ResourceDefinition";
        String parameterList = getParameterList(text);
        String argumentList = getArgumentList(text); // e.g. "p0, p1"
        String argumentArray = argumentList.equals("") ?
            "emptyObjectArray" :
            "new Object[] {" + argumentList + "}"; // e.g. "new Object[] {p0, p1}"

        pw.println();
        Util.generateCommentBlock(pw, resource.name, text, comment);

        pw.println("    public static final " + definitionClass + " " + resourceInitcap + " = new " + definitionClass + "(\"" + resourceInitcap + "\", " + Util.quoteForJava(text) + ");");
        pw.println("    public String get" + resourceInitcap + "(" + parameterList + ") {");
        pw.println("        return " + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + ").toString();");
        pw.println("    }");
        if (resource instanceof ResourceDef.Exception) {
            ResourceDef.Exception exception = (ResourceDef.Exception) resource;
            String errorClassName = getErrorClass(exception);
            final ExceptionDescription ed = new ExceptionDescription(errorClassName);
            if (ed.hasInstCon) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(" + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + "));");
                pw.println("    }");
            } else if (ed.hasInstThrowCon) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(" + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + "), null);");
                pw.println("    }");
            } else if (ed.hasStringCon) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(get" + resourceInitcap + "(" + argumentList + "));");
                pw.println("    }");
            } else if (ed.hasStringThrowCon) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + parameterList + ") {");
                pw.println("        return new " + errorClassName + "(get" + resourceInitcap + "(" + argumentList + "), null);");
                pw.println("    }");
            }
            if (ed.hasInstThrowCon) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + addLists(parameterList, "Throwable err") + ") {");
                pw.println("        return new " + errorClassName + "(" + resourceInitcap + ".instantiate(" + addLists("this", argumentArray) + "), err);");
                pw.println("    }");
            } else if (ed.hasStringThrowCon) {
                pw.println("    public " + errorClassName + " new" + resourceInitcap + "(" + addLists(parameterList, "Throwable err") + ") {");
                pw.println("        return new " + errorClassName + "(get" + resourceInitcap + "(" + argumentList + "), err);");
                pw.println("    }");
            }
        }
    }

    /**
     * Description of the constructs that an exception class has.
     */
    class ExceptionDescription {
        boolean hasInstCon;
        boolean hasInstThrowCon;
        boolean hasStringCon;
        boolean hasStringThrowCon;

        /**
         * Figures out what constructors the exception class has. We'd
         * prefer to use
         * <code>init(ResourceDefinition rd)</code> or
         * <code>init(ResourceDefinition rd, Throwable e)</code>
         * if it has them, but we can use
         * <code>init(String s)</code> and
         * <code>init(String s, Throwable e)</code>
         * as a fall-back.
         *
         * Prints a warming message if the class cannot be loaded.
         *
         * @param errorClassName Name of exception class
         */
        ExceptionDescription(String errorClassName)
        {
            hasInstCon = false;
            hasInstThrowCon = false;
            hasStringCon = false;
            hasStringThrowCon = false;
            try {
                Class errorClass;
                try {
                    errorClass = Class.forName(errorClassName);
                } catch (ClassNotFoundException e) {
                    // Might be in the java.lang package, for which we
                    // allow them to omit the package name.
                    errorClass = Class.forName("java.lang." + errorClassName);
                }
                Constructor[] constructors = errorClass.getConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Constructor constructor = constructors[i];
                    Class[] types = constructor.getParameterTypes();
                    if (types.length == 1 &&
                        ResourceInstance.class.isAssignableFrom(types[0])) {
                        hasInstCon = true;
                    }
                    if (types.length == 1 &&
                        String.class.isAssignableFrom(types[0])) {
                        hasStringCon = true;
                    }
                    if (types.length == 2 &&
                        ResourceInstance.class.isAssignableFrom(types[0]) &&
                        Throwable.class.isAssignableFrom(types[1])) {
                        hasInstThrowCon = true;
                    }
                    if (types.length == 2 &&
                        String.class.isAssignableFrom(types[0]) &&
                        Throwable.class.isAssignableFrom(types[1])) {
                        hasStringThrowCon = true;
                    }
                }
            } catch (ClassNotFoundException e) {
                if (warnedClasses.add(errorClassName)) {
                    System.out.println("Warning: Could not find exception " +
                        "class '" + errorClassName + "' on classpath. " +
                        "Exception factory methods will not be generated.");
                }
            }
        }
    }

    // helper
    protected static String addLists(String x, String y) {
        if (x == null || x.equals("")) {
            if (y == null || y.equals("")) {
                return "";
            } else {
                return y;
            }
        } else if (y == null || y.equals("")) {
            return x;
        } else {
            return x + ", " + y;
        }
    }

    protected static String addLists(String x, String y, String z) {
        return addLists(x, addLists(y, z));
    }
}

// End JavaBaseGenerator.java
