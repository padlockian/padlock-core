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

package net.padlocksoftware.padlock;

import java.util.Date;

/**
 * This class stores the build information for the Padlock core libraries.  
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class VersionInfo {
 ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final int majorVersion = 2;

  private static final int minorVersion = 2;

  private static final int pointVersion = 0;
  
  private static final long buildDate = 1303218429081L;

 ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Returns the major version number of the core Padlock library.
   */
  public static int getMajorVersion() {
    return majorVersion;
  }

  /**
   * Returns the build date for the core Padlock library.
   */
  public static Date getBuildDate() {
    return new Date(buildDate);
  }

  /**
   * Returns the minor version number of the core Padlock library.
   */
  public static int getMinorVersion() {
    return minorVersion;
  }


  /**
   * Returns the incremental release version of the core Padlock library.
   */
  public static int getPointVersion() {
    return pointVersion;
  }

  public static String getVersionString()
  {
    return majorVersion + "." + minorVersion + "." + pointVersion;
  }

 //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

 /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 
  private VersionInfo(){}

 ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

 //------------------------ Implements:

 //------------------------ Overrides:

 //---------------------------- Abstract Methods -----------------------------

 //----------------------------- Native Methods ------------------------------

 //---------------------------- Utility Methods ------------------------------

 //---------------------------- Property Methods -----------------------------

 //------------------------ Constructor Utility Methods ----------------------

}
