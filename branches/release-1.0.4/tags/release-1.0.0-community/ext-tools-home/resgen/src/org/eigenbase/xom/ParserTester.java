/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/ParserTester.java#3 $
// Package org.eigenbase.xom is an XML Object Mapper.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
// Portions Copyright (C) 2000-2005 Kana Software, Inc. and others.
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
// KLO, 22 July, 2001
*/

package org.eigenbase.xom;
import java.io.*;

/**
 * Test the MSParser
 */
public class ParserTester {

    // parser determines the type of parser to use.  Currently we support
    // MSXML and XERCES.
    private int parserType;
    private static final int MSXML = 1;
    private static final int XERCES = 2;

    // These members contain the actual parsers to use.  Only one will
    // ever be set.
    private Parser parser;

    // This member contain the model document type
    String modelDocType;

    // This member contain the URL for the DTD file
    String dtdUrl;

    //Read the XML file
    public ParserTester(String dtdFile,
                        int parserType)
        throws XOMException, IOException
    {
        this.parserType = parserType;

        parser = null;

        File dtdPath = new File(dtdFile);
        this.modelDocType = dtdFile.substring(0, dtdFile.indexOf("."));
        this.dtdUrl = "file:" + dtdPath.getAbsolutePath();

        switch (parserType) {
//      case MSXML:
//          parser = org.eigenbase.xom.wrappers.MSXMLWrapper.createParser(
//              modelDocType, dtdPath);
//          break;
//        case XERCES:
//            parser = new org.eigenbase.xom.wrappers.XercesDOMParser(true);
//            break;
        default:
            throw new XOMException("Unknown parser type: " + parserType);
        }
    }


    public void testFile(String testFile)
        throws XOMException
    {
        // parsing directly from an input stream (rather than a reader).
        String xmlString = null;
        try {
            StringWriter sWriter = new StringWriter();
            FileReader reader = new FileReader(testFile);

            if(parserType != MSXML) {
                PrintWriter out = new PrintWriter(sWriter);
                out.println("<?xml version=\"1.0\" ?>");
                if(modelDocType != null)
                    out.println("<!DOCTYPE " + modelDocType
                                + " SYSTEM \"" + dtdUrl + "\">");
                out.flush();
            }

            readerToWriter(reader, sWriter);
            reader.close();
            xmlString = sWriter.toString();
        } catch (IOException ex) {
            throw new XOMException("Unable to read input test "
                                      + testFile + ": " + ex.getMessage());
        }

        parser.parse(xmlString);

        System.out.println("Parsing document succeeded.");
    }

    /**
     * Helper function to copy from a reader to a writer
     */
    private static void readerToWriter(Reader reader, Writer writer)
        throws IOException
    {
        int numChars;
        final int bufferSize = 16384;
        char[] buffer = new char[bufferSize];
        while((numChars = reader.read(buffer)) != -1) {
            if(numChars > 0)
                writer.write(buffer, 0, numChars);
        }
    }

    /**
     * The ParserTester tests MSXML parser and Xerces Parser. Arguments:
     * <ol>
     * <li> The DTD file of the XML file
     * <li> The XML file for this DTD file
     * </ol>
     * </p>
     */
    public static void main(String args[])
        throws XOMException, IOException
    {
        int firstArg = 0;
        if(args.length > 0 && args[0].equals("-debug")){
            System.err.println("parserTest pausing for debugging. "
                               + "Attach your debugger "
                               + "and press return.");
            try {
                System.in.read();
                firstArg++;
            }
            catch(IOException ex) {
                // Do nothing
            }
        }

        int parserType = MSXML;
        if (firstArg < args.length && args[firstArg].equals("-msxml")) {
            parserType = MSXML;
            firstArg++;
        }
        else if (firstArg < args.length && args[firstArg].equals("-xerces")) {
            parserType = XERCES;
            firstArg++;
        }

        if(args.length < firstArg+2) {
            System.err.println(
                "Usage: java ParserTester [-debug] [-msxml | -xerces] "
                + "<DTD file> <XML file>");
            System.exit(-1);
        }

        ParserTester parserTester = new ParserTester(args[firstArg], parserType);
        firstArg++;

        parserTester.testFile(args[firstArg]);
    }

}


// End ParserTester.java
