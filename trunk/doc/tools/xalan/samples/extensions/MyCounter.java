/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
/*
 * $Id$
 */
import java.util.Hashtable;

public class MyCounter {
  static Hashtable counters = new Hashtable ();


  public void init(org.apache.xalan.extensions.XSLProcessorContext context, 
                   org.w3c.dom.Element elem) 
  {
    String name = elem.getAttribute("name");
    String value = elem.getAttribute("value");
    int val;
    try 
    {
      val = Integer.parseInt (value);
    } 
    catch (NumberFormatException e) 
    {
      e.printStackTrace ();
      val = 0;
    }
    counters.put (name, new Integer (val));
  }

  public int read(String name) 
  { 
    Integer cval = (Integer)counters.get(name);
    return (cval == null) ? 0 : cval.intValue();
  }

  public void incr(org.apache.xalan.extensions.XSLProcessorContext context,  
                   org.w3c.dom.Element elem) {
    String name = elem.getAttribute("name");
    Integer cval = (Integer) counters.get(name);
    int nval = (cval == null) ? 0 : (cval.intValue () + 1);
    counters.put (name, new Integer (nval));
  }
}
