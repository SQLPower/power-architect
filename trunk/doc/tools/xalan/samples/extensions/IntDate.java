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
import java.util.Calendar;
import java.util.Date;

public class IntDate
{
  public static Date getDate(String year, String month, String day)
    {
      // Date(int, int, int) has been deprecated, so use Calendar to
      // set the year, month, and day.
      Calendar c = Calendar.getInstance();
      // Convert each argument to int.
      c.set(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day));
      return c.getTime();
    }
}

