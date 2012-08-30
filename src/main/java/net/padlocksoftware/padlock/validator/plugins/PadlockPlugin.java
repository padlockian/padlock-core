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

package net.padlocksoftware.padlock.validator.plugins;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.padlocksoftware.padlock.VersionInfo;
import net.padlocksoftware.padlock.license.License;
import net.padlocksoftware.padlock.license.LicenseTest;
import net.padlocksoftware.padlock.license.TestResult;
import net.padlocksoftware.padlock.validator.ValidationParameters;
import net.padlocksoftware.padlock.validator.ValidatorPlugin;

/**
 *
 * @author Jason
 * @since 2.0
 */
public final class PadlockPlugin implements ValidatorPlugin {
  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  
  private static final String NAME = "Padlock License Plugin";

  private static final String DESCRIPTION = "This plugin is used to determine if a Padlock license is " +
          "valid for the intended use.";

  private static final long ONE_YEAR_IN_MS = 1000L * 60L * 60L * 24L * 366L;



  // Schema names

  private final String PRODUCT_NAME = "Padlock License Manager";
  
  private final String CURRENT_NAME_KEY = "product_padlock";
  private final String LEGACY_NAME_KEY = "Product";

  private final String CURRENT_SUPPORT_KEY = "product_padlock_supportLength";
  private final String LEGACY_SUPPORT_KEY = "supportLength";

  private final String CURRENT_VERSION_KEY = "product_padlock_majorVersion";
  private final String LEGACY_VERSION_KEY = "majorVersion";

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final Logger logger = Logger.getLogger(getClass().getName());

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements: ValidatorPlugin

  public TestResult validate(License license, ValidationParameters validationParameters) {

    boolean passed = true;
    String testFailureReason = "The supplied license is not valid for this version of Padlock";
    try {
      // Validate this license only if we're still within the support period or the major version
      // is the same.
      if (!verifyProductName(license)) {
        logger.fine("The supplied license is not for the Padlock License Manager");
        testFailureReason = "The supplied license is not for the Padlock License Manager";
        passed = false;
      } else if (!verifyMajorVersion(license) && !verifySupportContract(license)) {
        logger.fine("The supplied license is not valid for this version of Padlock");
        passed = false;
      }
    } catch (Throwable t) {
      logger.log(Level.WARNING, null, t);
      passed = false;
    }
    
    LicenseTest test = new LicenseTest("net.padlocksoftware.padlock", "Padlock License Test",
            "The Padlock license is valid for this version",
            testFailureReason);

    return new TestResult(test, passed);
  }

  public String getName() {
    return NAME;
  }

  public String getDescription() {
    return DESCRIPTION;
  }

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private boolean verifyProductName(License license) {
    String productName = license.getProperty(CURRENT_NAME_KEY);
    if (productName == null) {
      productName = license.getProperty(LEGACY_NAME_KEY);
    }

    return PRODUCT_NAME.equals(productName);
  }
  
  private boolean verifyMajorVersion(License license) {

    // Attempt to verify using current schema, followed by legacy.
    String majorVersion = license.getProperty(CURRENT_VERSION_KEY);
    if (majorVersion == null) {
      majorVersion = license.getProperty(LEGACY_VERSION_KEY);
    }

    int version = Integer.parseInt(majorVersion);

    boolean matches = VersionInfo.getMajorVersion() == version;
    logger.finer("License major version match: " + matches);
    return matches;
  }

  private boolean verifySupportContract(License license) {

    // Attempt to verify using current schema, followed by legacy.
    String supportLength = license.getProperty(CURRENT_SUPPORT_KEY);
    if (supportLength == null) {
      supportLength = license.getProperty(LEGACY_SUPPORT_KEY);
    }

    // Convert our support length to milliseconds
    long length = Long.parseLong(supportLength);
    length = length * ONE_YEAR_IN_MS;

    Date supportEndDate = new Date(license.getStartDate().getTime() + length);

    boolean matches = VersionInfo.getBuildDate().before(supportEndDate);
    logger.fine("Under support contract: " + matches);
    return matches;
  }

  //---------------------------- Property Methods -----------------------------
}
