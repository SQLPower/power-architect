/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/ShadowResourceBundle.java#4 $
// Package org.eigenbase.resgen is an i18n resource generator.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
// Portions Copyright (C) 2002-2005 Kana Software, Inc. and others.
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
//
// jhyde, 19 September, 2002
*/

package org.eigenbase.resgen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * <code>ShadowResourceBundle</code> is an abstract base class for
 * {@link ResourceBundle} classes which are backed by a properties file. When
 * the class is created, it loads a properties file with the same name as the
 * class.
 *
 * <p> In the standard scheme (see {@link ResourceBundle}),
 * if you call <code>{@link ResourceBundle#getBundle}("foo.MyResource")</code>,
 * it first looks for a class called <code>foo.MyResource</code>, then
 * looks for a file called <code>foo/MyResource.properties</code>. If it finds
 * the file, it creates a {@link PropertyResourceBundle} and loads the class.
 * The problem is if you want to load the <code>.properties</code> file
 * into a dedicated class; <code>ShadowResourceBundle</code> helps with this
 * case.
 *
 * <p> You should create a class as follows:<blockquote>
 *
 * <pre>package foo;
 *class MyResource extends org.eigenbase.resgen.ShadowResourceBundle {
 *    public MyResource() throws java.io.IOException {
 *    }
 *}</pre>
 *
 * </blockquote> Then when you call
 * {@link ResourceBundle#getBundle ResourceBundle.getBundle("foo.MyResource")},
 * it will find the class before the properties file, but still automatically
 * load the properties file based upon the name of the class.
 */
public abstract class ShadowResourceBundle extends ResourceBundle {
    private PropertyResourceBundle bundle;
    private static final HashMap mapThreadToLocale = new HashMap();
    protected static final Object[] emptyObjectArray = new Object[0];

    /**
     * Creates a <code>ShadowResourceBundle</code>, and reads resources from
     * a <code>.properties</code> file with the same name as the current class.
     * For example, if the class is called <code>foo.MyResource_en_US</code>,
     * reads from <code>foo/MyResource_en_US.properties</code>, then
     * <code>foo/MyResource_en.properties</code>, then
     * <code>foo/MyResource.properties</code>.
     */
    protected ShadowResourceBundle() throws IOException {
        super();
        Class clazz = getClass();
        InputStream stream = openPropertiesFile(clazz);
        if (stream == null) {
            throw new IOException("could not open properties file for " + getClass());
        }
        MyPropertyResourceBundle previousBundle =
                new MyPropertyResourceBundle(stream);
        bundle = previousBundle;
        stream.close();
        // Now load properties files for parent locales, which we deduce from
        // the names of our super-class, and its super-class.
        while (true) {
            clazz = clazz.getSuperclass();
            if (clazz == null ||
                    clazz == ShadowResourceBundle.class ||
                    !ResourceBundle.class.isAssignableFrom(clazz)) {
                break;
            }
            stream = openPropertiesFile(clazz);
            if (stream == null) {
                continue;
            }
            MyPropertyResourceBundle newBundle =
                    new MyPropertyResourceBundle(stream);
            stream.close();
            if (previousBundle != null) {
                previousBundle.setParentTrojan(newBundle);
            } else {
                bundle = newBundle;
            }
            previousBundle = newBundle;
        }
    }

    static class MyPropertyResourceBundle extends PropertyResourceBundle {
        public MyPropertyResourceBundle(InputStream stream) throws IOException {
            super(stream);
        }

        void setParentTrojan(ResourceBundle parent) {
            super.setParent(parent);
        }
    }

    /**
     * Opens the properties file corresponding to a given class. The code is
     * copied from {@link ResourceBundle}.
     */
    private static InputStream openPropertiesFile(Class clazz) {
        final ClassLoader loader = clazz.getClassLoader();
        final String resName = clazz.getName().replace('.', '/') + ".properties";
        return (InputStream)java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    if (loader != null) {
                        return loader.getResourceAsStream(resName);
                    } else {
                        return ClassLoader.getSystemResourceAsStream(resName);
                    }
                }
            }
        );
    }

    public Enumeration getKeys() {
        return bundle.getKeys();
    }

    protected Object handleGetObject(String key)
            throws MissingResourceException {
        return bundle.getObject(key);
    }

    /**
     * Returns the instance of the <code>baseName</code> resource bundle for
     * the current thread's locale. For example, if called with
     * "mondrian.olap.MondrianResource", from a thread which has called {@link
     * #setThreadLocale}({@link Locale#FRENCH}), will get an instance of
     * "mondrian.olap.MondrianResource_FR" from the cache.
     *
     * <p> This method should be called from a derived class, with the proper
     * casting:<blockquote>
     *
     * <pre>class MyResource extends ShadowResourceBundle {
     *    ...
     *    /&#42;&#42;
     *      &#42; Retrieves the instance of {&#64;link MyResource} appropriate
     *      &#42; to the current locale. If this thread has specified a locale
     *      &#42; by calling {&#64;link #setThreadLocale}, this locale is used,
     *      &#42; otherwise the default locale is used.
     *      &#42;&#42;/
     *    public static MyResource instance() {
     *       return (MyResource) instance(MyResource.class.getName());
     *    }
     *    ...
     * }</pre></blockquote>
     */
    protected static ResourceBundle instance(String baseName) {
        return instance(baseName, getThreadLocale());
    }
    /**
     * Returns the instance of the <code>baseName</code> resource bundle
     * for the given locale.
     *
     * <p> This method should be called from a derived class, with the proper
     * casting:<blockquote>
     *
     * <pre>class MyResource extends ShadowResourceBundle {
     *    ...
     *
     *    /&#42;&#42;
     *      &#42; Retrieves the instance of {&#64;link MyResource} appropriate
     *      &#42; to the given locale.
     *      &#42;&#42;/
     *    public static MyResource instance(Locale locale) {
     *       return (MyResource) instance(MyResource.class.getName(), locale);
     *    }
     *    ...
     * }</pre></blockquote>
     */
    protected static ShadowResourceBundle instance(
            String baseName, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
        if (bundle instanceof PropertyResourceBundle) {
            throw new ClassCastException(
                    "ShadowResourceBundle.instance('" + baseName + "','" +
                    locale + "') found " +
                    baseName + "_" + locale + ".properties but not " +
                    baseName + "_" + locale + ".class");
        }
        return (ShadowResourceBundle) bundle;
    }

    /** Sets the locale for the current thread. Used by {@link
     * #instance(String,Locale)}. **/
    public static void setThreadLocale(Locale locale) {
        mapThreadToLocale.put(Thread.currentThread(), locale);
    }

    /** Returns the preferred locale of the current thread, or null if the
     * thread has not called {@link #setThreadLocale}. **/
    public static Locale getThreadLocale() {
        return (Locale) mapThreadToLocale.get(Thread.currentThread());
    }
}

// End ShadowResourceBundle.java
