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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import net.padlocksoftware.padlock.license.License;
import net.padlocksoftware.padlock.license.LicenseTest;
import net.padlocksoftware.padlock.license.TestResult;
import net.padlocksoftware.padlock.validator.ValidationParameters;
import net.padlocksoftware.padlock.validator.ValidatorPlugin;
import net.padlocksoftware.padlock.validator.history.FileHistory;
import net.padlocksoftware.padlock.validator.history.History;
import net.padlocksoftware.padlock.validator.history.PreferenceHistory;

/**
 *
 * @author Jason Nichols
 */
public final class ExpiredPlugin implements ValidatorPlugin {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final String NAME = "Padlock Prior Plugin";

  private static final String DESCRIPTION = "Built in plugin which validates that the current " +
          "date is after the license start date.";

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
  private final Logger logger;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public ExpiredPlugin() {
    logger = Logger.getLogger(getClass().getName());
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  
  //------------------------ Implements: ValidatorPlugin

  public TestResult validate(License license, ValidationParameters validationParameters) {

    TestResult failedResult = new TestResult(LicenseTest.EXPIRED, false);
    TestResult passedResult = new TestResult(LicenseTest.EXPIRED, true);

    Date currentDate = validationParameters.getValidationDate();

    // Check that the currentDate < expirationDate
    Date expirationDate = license.getExpirationDate();
    if (expirationDate != null && currentDate.after(expirationDate)) {
      logger.fine("Current date is after the license expiration date");
      return failedResult;
    }

    // Check for floating expiration date, if desired
    Long floatPeriod = license.getFloatingExpirationPeriod();

    if (validationParameters.getIgnoreFloatTime() || floatPeriod == null) {
      return passedResult;
    }

    Set<History> historySet = new CopyOnWriteArraySet<History>();
    historySet.add(new PreferenceHistory());
    historySet.add(new FileHistory());

    Date firstRun = getFirstRun(currentDate, license, historySet);

    expirationDate = new Date(firstRun.getTime() + floatPeriod);

    if (currentDate.after(expirationDate)) {
      logger.fine("Current date is after the license floating expiration date");
      return failedResult;
    }

    return passedResult;

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
  public Long getTimeRemaining(Date currentDate, License license) {

    Long maxRemaining = null;

    //
    // Start with floatPeriod.
    //
    Long floatPeriod = license.getFloatingExpirationPeriod();

    if (floatPeriod != null) {
      //
      // Search preferences
      //
      String sig = license.getLicenseSignatureString().replace("=", "");

      Date earliestDate = new Date();

      Set<History> historySet = new CopyOnWriteArraySet<History>();
      historySet.add(new PreferenceHistory());
      historySet.add(new FileHistory());

      for (History history : historySet) {
        Date d = history.getEarliestRun(sig);
        if (d != null && d.before(earliestDate)) {
          earliestDate = d;
        }
      }

      //
      // Have all histories save this earliest date
      //
      for (History history : historySet) {
        history.setEarliestRun(sig, earliestDate);
      }

      //
      // Remaining time is the difference between the expire time and the
      // current time. This will be negative if the license has already
      // expired.
      //
      Date expireDate = new Date(earliestDate.getTime() + floatPeriod);
      maxRemaining = expireDate.getTime() - currentDate.getTime();
    }

    //
    // Now check the hard coded expiration date
    //
    Date expirationDate = license.getExpirationDate();
    if (expirationDate != null) {
      maxRemaining = Math.min(maxRemaining != null ? maxRemaining : Long.MAX_VALUE,
              expirationDate.getTime() - currentDate.getTime());
    }
    return maxRemaining;
  }

  private Date getFirstRun(Date currentDate, License license, Set<History> historySet) {
    Date earliestDate = currentDate;

    for (History history : historySet) {
      Date d = history.getEarliestRun(license.getLicenseSignatureString());
      if (d != null && d.before(earliestDate)) {
        earliestDate = d;
      }
    }

    //
    // Have all histories save this earliest date
    //
    for (History history : historySet) {
      history.setEarliestRun(license.getLicenseSignatureString(), earliestDate);
    }

    return earliestDate;
  }
  
  //---------------------------- Property Methods -----------------------------

}
