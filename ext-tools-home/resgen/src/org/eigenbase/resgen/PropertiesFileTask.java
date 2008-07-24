/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/PropertiesFileTask.java#4 $
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
import java.net.URL;
import java.util.Locale;

/**
 * Ant task which processes a properties file and generates a C++ or Java class
 * from the resources in it.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/PropertiesFileTask.java#4 $
 */
class PropertiesFileTask extends FileTask
{
    final Locale locale;

    PropertiesFileTask(ResourceGenTask.Include include, String fileName) {
        this.include = include;
        this.fileName = fileName;
        this.className = Util.fileNameToClassName(fileName, ".properties");
        this.locale = Util.fileNameToLocale(fileName, ".properties");
    }

    /**
     * Given an existing properties file such as
     * <code>happy/Birthday_fr_FR.properties</code>, generates the
     * corresponding Java class happy.Birthday_fr_FR.java</code>.
     *
     * <p>todo: Validate.
     */
    void process(ResourceGen generator) throws IOException
    {
        // e.g. happy/Birthday_fr_FR.properties
        String s = Util.fileNameSansLocale(fileName, ".properties");
        File file = new File(include.root.src, s + ".xml");
        URL url = Util.convertPathToURL(file);
        ResourceDef.ResourceBundle resourceList = Util.load(url);

        if (outputJava) {
            generateJava(generator, resourceList, locale);
        }
        if (outputCpp) {
            // We don't generate any C++ code from .properties file -- yet.
        }
    }
}

// End PropertiesFileTask.java

