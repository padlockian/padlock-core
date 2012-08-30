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

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * A collection of various parameters to be used by ValidationPlugins when validating a license.  This
 * is an immutable class created by a Validator instance to give supplamental information that
 * may help plugins determine whether a license is valid or not.
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class ValidationParameters {
  
  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  
  protected static ValidationParameters createParameters(boolean ignoreFloatTime, Date validationDate,
          Set<String> blacklist) {
    return new ValidationParameters(ignoreFloatTime, validationDate, blacklist);
  }

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final boolean ignoreFloatTime;

  private final Date validationDate;

  private final Set<String> blacklist;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  
  private ValidationParameters(boolean ignoreFloatTime, Date validationDate,
          Set<String> blacklist) {
    if (blacklist == null) {
      this.blacklist = Collections.emptySet();
    } else this.blacklist = Collections.unmodifiableSet(blacklist);
    this.ignoreFloatTime = ignoreFloatTime;
    this.validationDate = validationDate;
  }
  
  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  
  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  /**
   * Get the status of the IgnoreFloatTime flag.  When true, the validator will not attempt
   * to validate the floating expiration field on a license. This is useful for company side
   * validation routines, which may want to verify a customer license.
   */
  public boolean getIgnoreFloatTime() {
    return ignoreFloatTime;
  }

  /**
   * Get the date that the validation process was initiated.
   */
  public Date getValidationDate() {
    return validationDate;
  }

  /**
   * Get the list of blacklisted license signatures to compare the current license against.
   * 
   * @return The list of blacklisted license signatures, or an empty list if this Validator has not
   * been given a blacklist.
   */
  public Set<String> getBlacklist() {
    return blacklist;
  }
}
