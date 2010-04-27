/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.enterprise;

/**
 * A class that holds JSON and other response values for a response from a
 * server.
 */
public class JSONMessage {
    private final String message;
    
    /**
     * The status code of the response.
     */
    private final int statusCode;    

    /**
     * @param message
     *            The body of the response from the server. This may be but is
     *            not limited to: an error message, exception, or JSON of
     *            persist calls.
     * @param statusCode
     *            The code that was returned in the response.
     */
    public JSONMessage(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
    
    public String getBody() {
        return message;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * Returns true if the status code is in the 200s.
     */
    public boolean isSuccessful() {
        return (statusCode >= 200 && statusCode < 300);
    }
    
    public String toString() {
        return "Message (" + statusCode + "): " + message;
    }
}
