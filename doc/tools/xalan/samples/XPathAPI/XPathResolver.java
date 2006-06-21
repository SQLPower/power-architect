/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.xml.namespace.*;
import javax.xml.xpath.*;
import java.util.Iterator;

/**
 * JAXP 1.3 XPath API sample.
 * 
 * This sample demonstrates the use of NamespaceContext, XPathFunctionResolver
 * and XPathVariableResolver. The evaluated XPath expression ("ex:addFunc(2, 3) + $xyz")
 * contains an extension function and a variable reference. The extension function
 * and variable are evaluated using our customized XPathFunctionResolver and
 * XPathVariableResolver respectively. A NamespaceContext implementation is also
 * needed to map the prefix in the extension function to a valid namespace uri. 
 */
public class XPathResolver
{
    /**
     * The XPath expression to evaluate, which contains an extension
     * function and a variable reference.
     */
    private static final String EXPR = "ex:addFunc(2, 3) + $xyz";
    
    /**
     * The NamespaceContext implementation is used to map the prefix "ex"
     * to the namespace uri "http://ex.com".
     */
    public static class MyNamespaceContext implements NamespaceContext
    {
        public String getNamespaceURI(String prefix)
        {
            if (prefix == null)
              throw new IllegalArgumentException("The prefix cannot be null.");
            
            if (prefix.equals("ex"))
                return "http://ex.com";
            else
                return null;
        }
        
        public String getPrefix(String namespace)
        {
            if (namespace == null)
              throw new IllegalArgumentException("The namespace uri cannot be null.");
            if (namespace.equals("http://ex.com"))
              return "ex";
            else
              return null;
        }

        public Iterator getPrefixes(String namespace)
        {
            return null;
        }
    }
    
    /**
     * The XPathFunctionResolver implementation is used to evaluate
     * the extension function "ex:addFunc(2, 3)".
     */
    public static class MyFunctionResolver implements XPathFunctionResolver
    {
    	/**
    	 * This method returns a customized XPathFunction implementation
    	 * for the extension function "ex:addFunc()".
    	 */
    	public XPathFunction resolveFunction(QName fname, int arity)
    	{
    	  if (fname == null)
    	    throw new NullPointerException("The function name cannot be null.");
    	  
    	  // We only recognize one function, i.e. ex:addFunc().
    	  if (fname.equals(new QName("http://ex.com", "addFunc", "ex")))
    	    /** 
    	     * Return a customized implementation of XPathFunction. We need
    	     * to implement the evaluate(List) method.
    	     */
    	    return new XPathFunction() {
    	      /**
    	       * The actual implementation of the extension function.
    	       * Just cast two arguments to Double and add them together.
    	       */
    	      public Object evaluate(java.util.List args) {
    	        if (args.size() == 2) {
    	          Double arg1 = (Double)args.get(0);
    	          Double arg2 = (Double)args.get(1);
    	          return new Double(arg1.doubleValue() + arg2.doubleValue());
    	        }
    	        else
    	          return null;
    	      }
    	    };
    	  else
    	    return null;
    	}
    }
    
    /**
     * Our own implementation of XPathVariableResolver, which resolves the 
     * variable "xyz" to a double value 4.0.
     */
    public static class MyVariableResolver implements XPathVariableResolver
    {
      public Object resolveVariable(QName varName)
      {
        if (varName == null)
          throw new NullPointerException("The variable name cannot be null.");
        
        if (varName.equals(new QName("", "xyz")))
          return new Double(4.0);
        else
          return null;        	
      }
    }

    
    public static void main(String[] args)
    {    	        
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        // Set the NamespaceContext
        xpath.setNamespaceContext(new MyNamespaceContext());
        // Set the function resolver
        xpath.setXPathFunctionResolver(new MyFunctionResolver());
        // Set the variable resolver
        xpath.setXPathVariableResolver(new MyVariableResolver());
        
        Object result = null;
        try {
          result = xpath.evaluate(EXPR, (Object)null, XPathConstants.NUMBER);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        // The evaluation result is 9.0.
        System.out.println("The evaluation result: " + result);
    }
    
}