/*
* Copyright (c) 2009-2012 Jason Nichols

* Permission is hereby granted, free of charge, to any person obtaining a copy 
* of this software and associated documentation files (the "Software"), to deal 
* in the Software without restriction, including without limitation the rights to 
* use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
* of the Software, and to permit persons to whom the Software is furnished to do 
* so, subject to the following conditions:

* The above copyright notice and this permission notice shall be included in all 
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
* SOFTWARE.
*/

package net.padlocksoftware.padlock.validator.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This class is used internally by the validator, and should not be
 * instantiated outside of the Padlock library.
 * 
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
final public class FileHistory implements History {

  Set<File> fileSet = new HashSet<File>();

  public FileHistory() {
    File f = new File(System.getProperty("user.home"), ".dir");
    fileSet.add(f);
  }

  /**
   * Make sure the string is obeys length and content restrictions
   * @param sig
   * @return
   */
  private String trim(String sig) {
    return sig.substring(0, 63);
  }

  public Date getEarliestRun(String sig) {
    sig = trim(sig);
    Date d = null;

    long earliest = Long.MAX_VALUE;

    for (File f : fileSet) {
      if (f.canRead()) {
        try {
          FileInputStream fs = new FileInputStream(f);
          Properties p = new Properties();
          p.load(fs);
          String str = p.getProperty(sig);
          if (str != null) {
            long l = Long.parseLong(str);
            if (l < earliest) {
              earliest = l;
            }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }

    if (earliest != Long.MAX_VALUE) {
      d = new Date(earliest);
    }

    return d;
  }

  public void setEarliestRun(String sig, Date date) {
    sig = trim(sig);
    for (File f : fileSet) {
      try {

        Properties p = new Properties();
        f.createNewFile();
        FileInputStream in = new FileInputStream(f);
        p.load(in);
        in.close();
        p.setProperty(sig, Long.toString(date.getTime()));
        FileOutputStream fs = new FileOutputStream(f);
        p.store(fs, null);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
