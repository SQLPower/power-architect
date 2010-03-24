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

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper class around InputStream to prevent it from closing.
 * A separate method {@link #forceClose()} can be called to close
 * the input stream explicitly.
 *
 */
public class UnclosableInputStream extends InputStream {

    private final InputStream in;

    public UnclosableInputStream(InputStream in) {
        super();
        this.in = in;
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        // no!
    }
    
    /**
     * Forces the InputStream to close.  Using this method, we can close
     * the InputStream when we want rather than parse methods doing it
     * for us.
     * 
     * @throws IOException
     */
    public void forceClose() throws IOException {
        in.close();
    }

    public boolean equals(Object obj) {
        return in.equals(obj);
    }

    public int hashCode() {
        return in.hashCode();
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    public boolean markSupported() {
        return in.markSupported();
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    public void reset() throws IOException {
        in.reset();
    }

    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    public String toString() {
        return in.toString();
    }
    
    
}
