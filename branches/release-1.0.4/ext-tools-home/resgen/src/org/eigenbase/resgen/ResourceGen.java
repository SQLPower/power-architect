/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/ResourceGen.java#7 $
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
import java.io.IOException;

import org.eigenbase.xom.DOMWrapper;

/**
 * <code>ResourceGen</code> parses an XML file containing error messages, and
 * generates .java file to access the errors. Usage:<blockquote>
 *
 * <pre>ResourceGen xmlFile</pre>
 *
 * </blockquote>For example,<blockquote>
 *
 * <pre>java org.eigenbase.resgen.ResourceGen MyResource_en.xml</pre>
 *
 * </blockquote></p>
 *
 * <p>This will create class <code>MyResource</code>, with a
 * function corresponding to each error message in
 * <code>MyResource_en.xml</code>.</p>
 *
 * <p>See also the ANT Task, {@link ResourceGenTask}.</p>
 *
 * @author jhyde
 * @since 3 December, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/ResourceGen.java#7 $
 */
public class ResourceGen
{

    public static void main(String [] args) throws IOException
    {
        ResourceGenTask rootArgs = parse(args);
        new ResourceGen().run(rootArgs);
    }

    static ResourceGenTask parse(String[] args)
    {
        ResourceGenTask rootArgs = new ResourceGenTask();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-mode") && i + 1 < args.length) {
                rootArgs.setMode(args[++i]);
            } else if (arg.equals("-srcdir") && i + 1 < args.length) {
                rootArgs.setSrcdir(new File(args[++i]));
            } else if (arg.equals("-destdir") && i + 1 < args.length) {
                rootArgs.setDestdir(new File(args[++i]));
            } else if (arg.equals("-resdir") && i + 1 < args.length) {
                rootArgs.setResdir(new File(args[++i]));
            } else if (arg.equals("-locales") && i + 1 < args.length) {
                rootArgs.setLocales(args[++i]);
            } else if (arg.equals("-style") && i + 1 < args.length) {
                rootArgs.setStyle(args[++i]);
            } else if (arg.equals("-force")) {
                rootArgs.setForce(true);
            } else if (arg.equals("-commentstyle")) {
                rootArgs.setCommentStyle(args[++i]);
            } else {
                ResourceGenTask.Include resourceArgs =
                        new ResourceGenTask.Include();
                rootArgs.addInclude(resourceArgs);
                resourceArgs.setName(arg);
            }
        }
        if (rootArgs.getIncludes().length == 0) {
            throw new java.lang.Error("No input file specified.");
        }
        if (rootArgs.getDestdir() == null) {
            rootArgs.setDestdir(rootArgs.getSrcdir());
        }
        return rootArgs;
    }

    void run(ResourceGenTask rootArgs) throws IOException {
        rootArgs.validate();
        final ResourceGenTask.Include[] includes = rootArgs.getIncludes();
        for (int i = 0; i < includes.length; i++) {
            includes[i].process(this);
        }
    }

    /**
     * Prints a message to the output stream.
     */
    void comment(String message)
    {
        System.out.println(message);
    }

    /**
     * Returns the name of the resource with the first letter capitalized,
     * suitable for use in method names. For example, "MyErrorMessage".
     */
    static String getResourceInitcap(ResourceDef.Resource resource)
    {
        String name = resource.name;
        if (name.equals(name.toUpperCase())) {
            return "_" + name;
        } else {
            return name.substring(0,1).toUpperCase() + name.substring(1);
        }
    }

    /**
     * Returns any comment relating to the message.
     */
    static String getComment(ResourceDef.Resource resource)
    {
        DOMWrapper[] children = resource.getDef().getChildren();
        for (int i = 0; i < children.length; i++) {
            DOMWrapper child = children[i];
            if (child.getType() == DOMWrapper.COMMENT) {
                return child.getText(); // first comment only
            }
        }
        return null; // no comment
    }

    FileTask createXmlTask(
            ResourceGenTask.Include include, String fileName, String className,
            String baseClassName, boolean outputJava, String cppClassName,
            String cppBaseClassName, boolean outputCpp)
    {
        return new XmlFileTask(
            include, fileName, className, baseClassName,
            outputJava, cppClassName, cppBaseClassName,
            outputCpp);
    }

    FileTask createPropertiesTask(
            ResourceGenTask.Include include, String fileName) {
        return new PropertiesFileTask(include, fileName);
    }

}

// End ResourceGen.java
