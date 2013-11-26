/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/Resource.java#4 $
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

import java.io.IOException;
import java.util.Locale;

/**
 * A <code>Resource</code> is a collection of messages for a particular
 * software component and locale. It is loaded from an XML file whose root
 * element is <code>&lt;BaflResourceList&gt;</code>.
 *
 * <p>Given such an XML file, {@link ResourceGen} can generate Java a wrapper
 * class which implements this interface, and also has a method to create an
 * error for each message.</p>
 *
 * @author jhyde
 * @since 3 December, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/Resource.java#4 $
 **/
public interface Resource {
    /**
     * Populates this <code>Resource</code> from a URL.
     *
     * @param url The URL of the XML file containing the error messages
     * @param locale The ISO locale code (e.g. <code>"en"</code>, or
     *    <code>"en_US"</code>, or <code>"en_US_WIN"</code>) of the messages
     * @throws IOException if <code>url</code> cannot be opened, or if the
     *    format of its contents are invalid
     **/
    void init(java.net.URL url, Locale locale) throws java.io.IOException;

    /**
     * Populates this <code>Resource</code> from an XML document.
     *
     * @param resourceList The URL of the XML file containing the error messages
     * @param locale The ISO locale code (e.g. <code>"en"</code>, or
     *    <code>"en_US"</code>, or <code>"en_US_WIN"</code>) of the messages
     **/
    void init(ResourceDef.ResourceBundle resourceList, Locale locale);

    /**
     * Returns the locale of the messages.
     **/
    Locale getLocale();

    /**
     * Formats the message corresponding to <code>code</code> with the given
     * arguments. If an argument is not supplied, the tokens remain in the
     * returned message string.
     **/
    String formatError(int code, Object[] args);

    /**
     * Returns the severity of this message.
     **/
    int getSeverity(int code);
    int SEVERITY_INFO = 0;
    int SEVERITY_ERR  = 1;
    int SEVERITY_WARN = 2;
    int SEVERITY_NON_FATAL_ERR = 3;
}

// End Resource.java
