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
 * <p>A LicenseTest instance is the fundamental result unit of the Padlock validation process.  Each
 * phase of the validation process can be summarized with a LicenseTest instance.  As the {@link net.padlocksoftware.padlock.validator.Validator}
 * progresses, instances of LicenseTest are created and added to a {@link TestResult} instance.<p>
 *
 * <p>Padlock has a number of predefined LicenseTest types, which are all defined as public static
 * members of this class.  This is done for the convenience of Padlock customers who may want to
 * look through validation results for more details. Instances are compared for equality based
 * strictly on the 'id' field of the LicenseTest object.  The following code sample shows
 * how to check a failed validation attempt for the results of the Padlock Hardware test:
 * <pre>
 *    // Assume a ready validation instance
 *    LicenseState state = null;
 *    try {
 *       state = validator.validate();
 *       logger.info("All tests passed, enable full functionality.");
 *    } catch (ValidatorException e) {
 *       state = ex.getLicenseState();
 *       if (state.getFailedTests().contains(LicenseTest.HARDWARE)) {
 *          logger.info("Hardware test failed, just thought you should know.");
 *       }
 *    }
 * </pre>
 * Padlock only utilizes the static, immutable LicenseTest instances shown in the 'Field Summary'
 * below.  This makes for easy comparisons and inquiries into the results of Padlock's built in tests.</p>
 * <p> Developers who are creating their own {@link net.padlocksoftware.padlock.validator.ValidatorPlugin} classes need to simply
 * create instances of this class with the appropriate constructor parameters.</p>
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class LicenseTest {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * The Padlock test checking that the license is signed.  A passing result for this test
   * indicates the license has a signature, but makes no attempt to verify the signature with
   * a public key.
   */
  public static final LicenseTest SIGNED = new LicenseTest("net.padlocksoftware.signed", "Signed Test",
          "The license is signed.", "The license is not signed.");

  /**
   * The Padlock test checking for a valid license signature.  A passing result for this test
   * indicates that the license has a verifiable signature with the corresponding public/private
   * keypair used in the validation process.  When passing, this license is guaranteed to be both
   * authentic (created by the private key owner) and unmodified.  This is one of the most
   * important tests performed by Padlock.  Only when this test passes can you be assured
   * the data contained within the license hasn't been tampered with.
   */
  public static final LicenseTest SIGNATURE = new LicenseTest("net.padlocksoftware.signature",
          "Signature Test", "The license signature has been verified", 
          "The license signature can't be verified");

  /**
   * The Padlock test ensuring that the current system date is after the license start date.   A
   * passing result ensures that the license validity start period has occured.
   */
  public static final LicenseTest PRIOR = new LicenseTest("net.padlocksoftware.prior", "Prior Test",
          "The current date is after the license start date",
          "The current date is prior to the license start date");

  /**
   * The Padlock test ensuring that the current system date (as reported by the OS or determined heuristically
   * by Padlock) is before the license expiration date, if it exists.  A passing result ensures
   * that the end of the liense validity period has not yet been reached.
   */
  public static final LicenseTest EXPIRED = new LicenseTest("net.padlocksoftware.expired",
          "Expired Test", "The current date is before the license expiration date",
          "The current date is after the license expiration date");

  /**
   * The Padlock test ensuring that the license in question is not blacklisted. A passing result
   * means the license signature does not match any of the signatures on the blacklist passed to
   * the validator.
   */
  public static final LicenseTest BLACKLIST = new LicenseTest("net.padlocksoftware.blacklist",
          "Blacklist Test", "The license is not blacklisted",
          "The license has been blacklisted");

  /**
   * The Padlock test ensuring that the license in question is valid for the hardware running
   * the validation.  If the license contains hardware addresses, one of the listed addresses
   * must match at least one of the hardware addresses on the local system.
   */
  public static final LicenseTest HARDWARE = new LicenseTest("net.padlocksoftware.hardware",
          "Hardware Test", "The license is valid for this hardware",
          "The license is not valid for this hardware");


  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final String id;

  private final String name;

  private final String passedMessage;

  private final String failedMessage;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Construct an immutable LicenseTest.
   *
   * @param id The unique identifier for this test.  Typically of the form <i>com.companyDomain.testName</i>.
   * @param name The user readable name for the test, eg "Floating license check".
   * @param passedMessage The message to display if the test passes.
   * @param failedMessage The message to display if the test fails.
   */
  public LicenseTest(String id, String name, String passedMessage, String failedMessage) {
    this.id = id;
    this.name = name;
    this.passedMessage = passedMessage;
    this.failedMessage = failedMessage;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides: Object
  
  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    
    if (obj instanceof LicenseTest) {
      LicenseTest test = (LicenseTest)obj;
      return (test.id.equals(id));
    } else return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
    return hash;
  }

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------
  
  /**
   * @return The unique test Id.
   */
  public String getId() {
    return id;
  }

  /**
   * @return The user readable test name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return The passing message.
   */
  public String getPassedMessage() {
    return passedMessage;
  }

  /**
   *
   * @return The failing message.
   */
  public String getFailedMessage() {
    return failedMessage;
  }
}
