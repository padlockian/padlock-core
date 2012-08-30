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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>The LicenseState class represents the results of a Validation attempt on a license.  Each time
 * Validator.validate() is called, an Instance of LicenseState provides fine grained test results.
 * This is returned either explicity through the Validator.validate() method or can be grabbed as a member
 * object of the ValidatorException instance thrown during a failed validation attempt.</p>
 *
 *<p>A LicenseState instance contains an immutable collection of {@link TestResult}, with each
 * TestResult item containing the results of a specific test performed by the Validator.  See both
 * the Validator and TestResult pages for details on their usages.</p>
 *
 * <p>LicenseState instances should only be created internally by the Padlock library.  There are
 * no known use cases where third party developers would need to explicitly instantiate a LicenseState object.</p>
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class LicenseState {
  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /** All tests performed by a Validation instance */
  private final List<TestResult> tests;

  /** Read only (public) version of the above list */
  private final List<TestResult> staticTests;

  /** All passed tests */
  private final List<TestResult> passedTests;

  /** All failed tests */
  private final List<TestResult> failedTests;

  private final boolean isValid;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  public LicenseState(TestResult result) {
    this(Collections.singletonList(result));
  }

  public LicenseState(List<TestResult> tests) {
    if (tests == null) throw new NullPointerException("Tests cannot be null");

    this.tests = new ArrayList<TestResult>(tests);
    staticTests = Collections.unmodifiableList(this.tests);

    List<TestResult> tempPassedSet = new ArrayList<TestResult>();
    List<TestResult> tempFailedSet = new ArrayList<TestResult>();

    for (TestResult result : this.tests) {
      if (result.passed()) {
        tempPassedSet.add(result);
      } else {
        tempFailedSet.add(result);
      }
    }

    passedTests = Collections.unmodifiableList(tempPassedSet);
    failedTests = Collections.unmodifiableList(tempFailedSet);

    isValid = failedTests.size() == 0;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Returns the result of the Validation process.  A license is only considered valid if all tests
   * have passed.  Even a single test failure renders the license invalid.
   * 
   * @return True if all TestResult instances have passed, or false otherwise.
   */
  public boolean isValid() {
    return isValid;
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  /**
   * Get a list of all performed tests (including both those that passed and failed).
   * @return An immutable list of LicenseTest results.
   */
  public List<TestResult> getTests() {
    return staticTests;
  }

  /**
   * Get a list of all passing tests.
   * @return An immutable list of LicenseTest results that are passing, or an empty list if
   * all tests failed.
   */
  public List<TestResult> getPassedTests() {
    return passedTests;
  }

  /**
   * Get a list of all failed tests.
   * @return An immutable list of LicenseTest results that are failing, or an empty list if
   * all tests passed.
   */
  public List<TestResult> getFailedTests() {
    return failedTests;
  }

  /**
   * Look up a TestResult by test ID.
   * @param testId The unique String identifier for a test
   * @return The instance of the TestResult, or null if no such test exists.
   */
  public TestResult findTest(String testId) {
    TestResult result = null;

    for (TestResult testResult : staticTests) {
      if (testResult.getTest().getId().equals(testId)) {
        result = testResult;
        break;
      }
    }
    return result;
  }
}
