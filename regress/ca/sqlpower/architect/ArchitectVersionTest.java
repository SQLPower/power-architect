/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect;

import junit.framework.TestCase;
import ca.sqlpower.util.VersionParseException;

public class ArchitectVersionTest extends TestCase {
    
    private ArchitectVersion version;
    
    public void testParseWithSuffix() {
        version = new ArchitectVersion("1.2.3.4-alpha-2");
        
        assertEquals(5, version.getParts().length);
        
        assertEquals(1, version.getParts()[0]);
        assertEquals(2, version.getParts()[1]);
        assertEquals(3, version.getParts()[2]);
        assertEquals(4, version.getParts()[3]);
        assertEquals("-alpha-2", version.getParts()[4]);
    }

    public void testParseWithoutSuffix() {
        version = new ArchitectVersion("1.2.3.4");

        assertEquals(4, version.getParts().length);

        assertEquals(1, version.getParts()[0]);
        assertEquals(2, version.getParts()[1]);
        assertEquals(3, version.getParts()[2]);
        assertEquals(4, version.getParts()[3]);
    }
    
    public void testBadPatternWithDotBeforeSuffix() {
        try {
            version = new ArchitectVersion("1.2.3.-alpha");
            fail("Bad version format was accepted");
        } catch (VersionParseException ex) {
            // good
        }
    }
    
    public void testCompareFullOnlySuffixesDiffer() {
        ArchitectVersion ancient = new ArchitectVersion("1.2.3-20080808");
        ArchitectVersion older = new ArchitectVersion("1.2.3-cow");
        ArchitectVersion olderToo = new ArchitectVersion("1.2.3-cow");
        ArchitectVersion newer = new ArchitectVersion("1.2.3-moo");
        ArchitectVersion singleDigitSuffix = new ArchitectVersion("1.2.3-cow-2");
        ArchitectVersion doubleDigitSuffix = new ArchitectVersion("1.2.3-cow-12");
        
        assertTrue(older.compareTo(olderToo) == 0);
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
        assertTrue(ancient.compareTo(older) < 0);
        assertTrue(singleDigitSuffix.compareTo(doubleDigitSuffix) < 0);
        assertTrue(doubleDigitSuffix.compareTo(singleDigitSuffix) > 0);
    }

    public void testCompareFullNoSuffixes() {
        ArchitectVersion older = new ArchitectVersion("1.2.2");
        ArchitectVersion olderToo = new ArchitectVersion("1.2.2");
        ArchitectVersion newer = new ArchitectVersion("1.2.3");
        
        assertTrue(older.compareTo(olderToo) == 0);
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }

    public void testCompareDifferentLengths() {
        ArchitectVersion older = new ArchitectVersion("1.2");
        ArchitectVersion newer = new ArchitectVersion("1.2.3");
        
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }
    
    public void testNoSuffixNewerThanSuffix() {
        ArchitectVersion older = new ArchitectVersion("1.2-cow");
        ArchitectVersion newer = new ArchitectVersion("1.2");
        
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }

    public void testCompareSameLengthsOnlyOneHavingSuffix() {
        ArchitectVersion older = new ArchitectVersion("1.2-cow");
        ArchitectVersion newer = new ArchitectVersion("1.2.3");
        
        assertTrue(older.compareTo(newer) < 0);
        assertTrue(newer.compareTo(older) > 0);
    }
    
    public void testToString() {
        ArchitectVersion v = new ArchitectVersion("1.2.3-a");
        assertEquals("1.2.3-a", v.toString());

        v = new ArchitectVersion("1.2.3");
        assertEquals("1.2.3", v.toString());

        v = new ArchitectVersion("1-suffix");
        assertEquals("1-suffix", v.toString());

        v = new ArchitectVersion("1");
        assertEquals("1", v.toString());
    }
}