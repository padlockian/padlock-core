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

package net.padlocksoftware.padlock.validator;

import net.padlocksoftware.padlock.license.License;
import net.padlocksoftware.padlock.license.TestResult;

/**
 * <p>The ValidatorPlugin interface allows Padlock developers to write plugins
 * that are executed by the Validator.  These plugins can be added by
 * calling addPlugin() on the Validator instance.  Added plugins will be
 * executed after Padlock's built in checks have taken place. This means
 * that by the time a plugin is executed, the plugin can be assured that the
 * license has passed all authenticity and validity checks.  This means
 * the license signature matches and the valiation time falls within the
 * License's validity period.</p>
 *
 * <p>Each ValidatorPlugin must return a {@link TestResult} instance, containing a specific test
 * identifier (chosen by the developer) and the result of the validation attempt.  If any of Padlock's
 * built in plugins or the added plugins return a test failure, the license is considered invalid.</p>
 *
 * <p>Here's a real world example of one of Padlock's plugins, the BlacklistPlugin, which checks
 * to see if a license is on the Validator's blacklist.  Note that development code is shown, the
 * actual shipping code may be different:
 * 
 * <pre>
 * public final class BlacklistPlugin implements ValidatorPlugin {
 *
 *    private static final String NAME = "Padlock Blacklist Plugin";
 *
 *    private static final String DESCRIPTION = "Built in plugin that searches for licenses matching a" +
 *          "user defined blacklist";
 *
 *    private final Logger logger = Logger.getLogger(getClass().getName());
 *
 *    &#64;Override
 *    public TestResult validate(License license, ValidationParameters validationParameters) {
 *       boolean passed = true;
 *
 *       Set<String> blacklist = validationParameters.getBlacklist();
 *
 *       String signature = license.getLicenseSignatureString();
 *       if (blacklist.contains(signature)) {
 *          logger.fine("Found blacklisted license: " + signature);
 *          passed = false;
 *       }
 *
 *       return new TestResult(LicenseTest.BLACKLIST, passed);
 *    }
 *
 *    &#64;Override
 *    public String getName() {
 *       return NAME;
 *    }
 *
 *    &#64;Override
 *    public String getDescription() {
 *       return DESCRIPTION;
 *    }
 * }
 * </pre>
 *
 * <p>
 * Instances of each test Padlock performs can be found as static members of the
 * {@link net.padlocksoftware.padlock.license.LicenseTest} class.  This makes for easy determination
 * of which tests have passed and failed if you need fine grained resulsts.
 * </p>
 *
 * 
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public interface ValidatorPlugin {
  
 ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

 ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

 //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

 /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

 ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


  /**
   * This method performs a specific validation test on a license instance.
   *
   * @param license The license being validated.
   * @param validationParameters Informational parameters specific to the license being tested
   * and the Validator managing the validation.
   * @return A TestResult instance describing the test and the result of the test.
   */
  public TestResult validate(License license, ValidationParameters validationParameters);

  /**
   * The developer defined name for this plugin.
   */
  public String getName();

  /**
   * The developer defined description for this plugin.
   */
  public String getDescription();


 //------------------------ Implements:

 //------------------------ Overrides:

 //---------------------------- Abstract Methods -----------------------------

 //---------------------------- Utility Methods ------------------------------

 //---------------------------- Property Methods -----------------------------

}
