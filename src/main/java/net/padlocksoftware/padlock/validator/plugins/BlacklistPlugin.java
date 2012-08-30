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

import java.util.Set;
import java.util.logging.Logger;
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
public final class BlacklistPlugin implements ValidatorPlugin {
  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final String NAME = "Padlock Blacklist Plugin";

  private static final String DESCRIPTION = "Built in plugin that searches for licenses matching a" +
          "user defined blacklist";

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final Logger logger = Logger.getLogger(getClass().getName());

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements: ValidatorPlugin

  public TestResult validate(License license, ValidationParameters validationParameters) {
    boolean passed = true;

    Set<String> blacklist = validationParameters.getBlacklist();

    String signature = license.getLicenseSignatureString();
    if (blacklist.contains(signature)) {
      logger.fine("Found blacklisted license: " + signature);
      passed = false;
    }

    return new TestResult(LicenseTest.BLACKLIST, passed);
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

  //---------------------------- Property Methods -----------------------------
}
