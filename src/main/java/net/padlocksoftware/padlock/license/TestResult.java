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

package net.padlocksoftware.padlock.license;

/**
 * The TestResult class is a wrapper for a {@link LicenseTest} instance and a simple boolean
 * flag indicating whether the test passed or failed.  The Validator's validation results provide
 * a List of TestResult instances giving results for each test performed on a license.
 *
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class TestResult {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final LicenseTest test;

  private final boolean passed;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Create a new TestResult instance.
   * @param test The LicenseTest that was performed.
   * @param passed The result of the performed test.
   */
  public TestResult(LicenseTest test, boolean passed) {

    if (test == null) throw new NullPointerException("Test cannot be null");

    this.test = test;

    this.passed = passed;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  /**
   * Returns the simple result of the LicenseTest.
   */
  public boolean passed() {
    return passed;
  }

  /**
   * Return an instance of the LicenseTest.
   */
  public LicenseTest getTest() {
    return test;
  }

  /**
   * Convenience method for returning the appropriate result description for the LicenseTest
   * owned by this test result instace.
   * @return The appropriate result message (either passing or failing) for the LicenseTest object.
   */
  public String getResultDescription() {

    return (passed ? test.getPassedMessage() : test.getFailedMessage());

  }

}
