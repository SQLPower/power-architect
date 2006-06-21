/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
/*****************************************************************************************************
 *
 * Wrapper for exceptions occurring during apply XSL processing.  
 * Allows for exceptions to be returned with an associated HTTP Status Code.
 *
 * @author Spencer Shepard (sshepard@us.ibm.com)
 * @author R. Adam King (rak@us.ibm.com)
 * @author Tom Rowe (trowe@us.ibm.com)
 *
 *****************************************************************************************************/
package servlet;

public class ApplyXSLTException extends Exception {

    /**
      * Exception Message.
      * @serial
      */ 
    private String myMessage = "";

    /**
      * HTTP Status Code. Default= internal server error.
      * @serial
      */
    private int  myHttpStatusCode = javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR; 

    /**
      * Wrapped exception
      * @serial
      */
    private Exception myException = null;

    /**
      * Constructor for exception with no additional detail.
      */
    public ApplyXSLTException() 
    { 
        super(); 
    }

    /**
      * Constructor for exception with message.
      * @param s Exception message
      */
    public ApplyXSLTException(String s) 
    { 
        super(); 
	myMessage = s;
    }

    /**
      * Constructor for exception with HTTP status code.
      * @param hsc Valid status code from javax.servlet.http.HttpServletResponse
      */
    public ApplyXSLTException(int hsc) 
    {
	super();
	myHttpStatusCode = hsc;
    }

    /**
      * Constructor for exception with message and HTTP status code.
      * @param s Exception message
      * @param hsc Valid status code from javax.servlet.http.HttpServletResponse
      */
    public ApplyXSLTException(String s, int hsc)
    {
	super();
	myHttpStatusCode = hsc;
    }

    /**
      * Constructor for exception.
      * @param e Exception to be wrapped.
      */
    public ApplyXSLTException(Exception e)
    {
	super();
	myMessage = e.getMessage();
	myException = e;
    }

    /**
      * Constructor for passed exception with message.
      * @param s Exception message
      * @param e Exception to be wrapped.
      */
    public ApplyXSLTException (String s, Exception e)
    {
	super();
	myMessage = s;
	myException = e;
    }

    /**
      * Constructor for passed exception with HTTP status code.
      * @param e Exception to be wrapped.
      * @param hsc Valid status code from javax.servlet.http.HttpServletResponse
      */
    public ApplyXSLTException(Exception e, int hsc)
    {
	super();
	myMessage = e.getMessage();
	myException = e;
	myHttpStatusCode = hsc;
    }

    /**
      * Constructor for passed exception with HTTP status code and message.
      * @param s Exception message
      * @param e Exception to be wrapped.
      * @param hsc Valid status code from javax.servlet.http.HttpServletResponse
      */
    public ApplyXSLTException(String s, Exception e, int hsc)
    {
	super();
	myMessage = s;
	myException = e;
	myHttpStatusCode = hsc;
    }

    /**
      * Returns exception message.
      * @return exception message
      */
    public String getMessage()
    {
	return myMessage;
    }

    /**
      * Appends string to exception message.
      * @param s String to be added to message
      */
    public void appendMessage(String s)
    {
	myMessage += s;
    }

    /**
      * Returns the wrapped exception.
      * @return Wrapped exception
      */
    public Exception getException()
    {
	return myException;
    }

    /**
      * Returns the HTTP status code associated with the exception.
      * @return Valid status code from javax.servlet.http.HttpServletResponse
      */
    public int getStatusCode()
    {
	return myHttpStatusCode;
    }
}

