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

import java.io.File;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import net.padlocksoftware.padlock.license.License;
import net.padlocksoftware.padlock.license.LicenseImpl;
import net.padlocksoftware.padlock.license.LicenseState;
import net.padlocksoftware.padlock.license.LicenseTest;
import net.padlocksoftware.padlock.license.TestResult;
import net.padlocksoftware.padlock.validator.plugins.BlacklistPlugin;
import net.padlocksoftware.padlock.validator.plugins.ExpiredPlugin;
import net.padlocksoftware.padlock.validator.plugins.HardwarePlugin;
import net.padlocksoftware.padlock.validator.plugins.PriorPlugin;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * <p>The Validator class is used to validate license instances.  By taking a license and a public
 * key which corresponds to a private key that was used to sign the license, the validator can
 * ensure that a license is both authentic (originating from you) and unmodified.  This means
 * that the Validator can only work on signed licenses, since without a license signature neither
 * authenticity or integrity can be assured.</p>
 *
 * <p>The simplist use case involves instantiating a Validator with the License and Public Key, then
 * calling {@link #validate() validate()}.  In most cases, using the defaults provided by the
 * Validator instance will be sufficient.  For certain cases, there are two instance options
 * available that may be of interest:</p>
 *
 * <p> {@link #setCheckClockTurnback(boolean) setClockTurnback()} gives you the option of disabling
 * turnback checking.  If you're calling {@link #validate(java.util.Date) validate(Date)} it is
 * best to disable this option if you're getting the current date from an external source (an NTP
 * server, for instance).  Clock turnback checking is on by default.</p>
 *
 * <p> {@link #setIgnoreFloatTime(boolean) setIgnoreFloatTime()} allows you to turn off checking
 * the floating expiration value in a license (if present).  This is useful in scenarios where
 * you may be validating licenses as part of a customer support procedure, and don't with to
 * start the clock ticking on a license locally.  Padlock's License Validator command line tool
 * utilizes this.</p>
 *
 * <p>The Validator can also be assigned a list of License signatures which, for whatever reason,
 * should not pass validation.  {@link #addBlacklistedLicense(java.lang.String) addBlacklistedLicense()}
 * will add the license signature String, and will throw a ValidationException if the license
 * is used for validation.</P>
 *
 * <p>Finally, the validator can return the amount of valid license time remaining.  Using
 * {@link #getTimeRemaining(java.util.Date) getTimeRemaining()} and passing the current date
 * returns the number of milliseconds remaining in this license's validity period, or a negative
 * number if that period has already passed.  It's important to note that this method takes
 * the license expiration dates at face value, and does not guarantee that a license is valid.  To
 * ensure that a license is valid, run the validate() method before running getTimeRemaining()</p>
 *
 * <p>To validate a license, call {@link #validate() validate()}.  If the license cannot
 * validate, a ValidatorException will be thrown. Simply catch the exception, which will contain
 * a List of failed LicenseStatuses, each describing why the Validation failed.
 * For example:
 * </p>
 * <pre>
 *    Validator v = new Validator (license, publicKey);
 *    LicenseState licenseState = null;
 *    try {
 *       licenseState = v.validate();
 *       // Enable app functionality here
 *    } catch (ValidatorException e) {
 *       licenseState = e.getLicenseState();
 *       for (TestResult result : licenseState.getFailedTests()) {
 *          // Show all failures
 *          System.out.println("License failure for test : " + result.getTest().getName() + " - " +
 *             result.getResultDescription());
 *       }
 *    }
 * </pre
 * <p>
 * It's also important to note that while the Validator will generally continue the validation
 * process after it receives a failed LicenseTest, there's two cases
 * where it will stop the validation immediately.  If the license is not signed or the Crypto (RSA/DSA) verification
 * fails (meaning the license has been modified), the Validator will not continue
 * as none of the data in the license can be assured to be valid.  Allowing the
 * validation to continue could open up avenues of attack on plugins using specially
 * crafted data.
 * </p>
 *
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class Validator {

  private interface InternalValidator {

    public TestResult validate(License l, PublicKey key);
  }

  private class RsaValidator implements InternalValidator {

    private boolean matches(byte[] sig1, byte[] sig2) {
      boolean matches = true;

      if (sig1.length != sig2.length) {
        return false;
      }
      for (int x = 0; x < sig1.length; x++) {
        if (sig1[x] != sig2[x]) {
          return false;
        }
      }

      return matches;
    }

    /**
     * Return a SHA1 hash of the license properties
     * @return The SHA1 has of all properties.
     */
    public byte[] hash(License l) throws NoSuchAlgorithmException {
      byte[] hash = null;

      String hashString = ((LicenseImpl) l).concatenate();

      //
      // Hash the hashString
      //
      MessageDigest d = MessageDigest.getInstance("SHA1");
      hash = d.digest(hashString.getBytes());

      return hash;
    }

    public TestResult validate(License l, PublicKey key){
      
      try {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] ls = l.getLicenseSignature();
        cipher.update(ls);
        byte[] hash = cipher.doFinal();
        byte[] ourHash = hash(l);
        if (!matches(hash, ourHash)) {
          logger.fine("License signature does not match");
          return new TestResult(LicenseTest.SIGNATURE, false);
        }
      } catch (Exception ex) {
        logger.log(Level.SEVERE, null, ex);
        return new TestResult(LicenseTest.SIGNATURE, false);
      }
        return new TestResult(LicenseTest.SIGNATURE, true);
    }
  }

  private class DsaValidator implements InternalValidator {

    public TestResult validate(License l, PublicKey key) {

      // Check license integrity
      try {
        Signature sig = Signature.getInstance(dsaAlg);
        sig.initVerify(key);
        byte[] concat = ((LicenseImpl) l).concatenate().getBytes("UTF-8");
        sig.update(concat);
        if (!sig.verify(l.getLicenseSignature())) {
          logger.fine("License signature does not match");
          return new TestResult(LicenseTest.SIGNATURE, false);
        }
      } catch (Exception ex) {
        logger.log(Level.SEVERE, null, ex);
        return new TestResult(LicenseTest.SIGNATURE, false);
      }

      return new TestResult(LicenseTest.SIGNATURE, true);
    }
  }

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final String dsaAlg = "SHA1withDSA";

  //
  // Search all files in these folders, and look for the one with the latest
  // modification date.
  //
  private static final Set<File> modifiedSearchSet = new HashSet<File>();

  static {
    // This folder can grow huge!  Ignore for now.
    String str = System.getProperty("java.io.tmpdir");
    modifiedSearchSet.add(new File(str));

    str = System.getProperty("user.home");
    modifiedSearchSet.add(new File(str));
  }

  private static final int FILE_LIMIT = 50;

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final Logger logger;
  private final License license;
  private PublicKey publicKey;
  private final List<ValidatorPlugin> plugins;
  private final Set<String> blacklist;
  private boolean isRSA = false;
  private final InternalValidator sigValidator;
  private boolean ignoreFloatTime;
  private boolean checkClockTurnback;

  private ExpiredPlugin expiredPlugin;


  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Create a new empty Validator instance with the License and PublicKey set.  These are not
   * changeable once created, so every License/KeyPair combination will need its own Validator instance.
   * When created, the Validator contains only the build in Padlock plugins, which are fixed.  New
   * custom plugins can be added by calling {@link #addPlugin(net.padlocksoftware.padlock.validator.ValidatorPlugin) addPlugin()}.
   *
   * @param license The license instance to validate.
   *
   * @param publicKey The PublicKey corresponding to the PrivateKey used to sign the license.
   */
  public Validator(License license, String publicKey) {
    logger = Logger.getLogger(getClass().getName());
    this.license = license;

    // Try DSA first
    this.publicKey = convertPublicKey(publicKey, false);

    if (this.publicKey == null) {
      this.publicKey = convertPublicKey(publicKey, true);
      isRSA = true;
    }

    logger.fine("Using RSA Key: " + isRSA);

    sigValidator = isRSA ? new RsaValidator() : new DsaValidator();

    // If the public Key is still null, throw a runtime exception
    if (this.publicKey == null) {
      throw new RuntimeException("Cannot create PublicKey object");
    }

    plugins = new CopyOnWriteArrayList<ValidatorPlugin>();

    // Default plugins
    expiredPlugin = new ExpiredPlugin();
    
    plugins.add(expiredPlugin);
    plugins.add(new PriorPlugin());
    plugins.add(new BlacklistPlugin());
    plugins.add(new HardwarePlugin());

    blacklist = new CopyOnWriteArraySet<String>();

    ignoreFloatTime = false;
    checkClockTurnback = true;
  }

  public Validator (License l, byte[] publicKey) {
    this(l, new String(Hex.encodeHex(publicKey)));
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Validate the license using the current date as determined by Padlock.

   * @throws ValidatorException If the license does not validate.  If thrown, the license should
   * not be considered valid.
   */
  public LicenseState validate() throws ValidatorException {
    return validate(new Date());
  }

  /**
   * Validate the license using the supplied date.  When supplying a date, it's suggested that
   * checkClockTurnback be set to false.  This will prevent Padlock from overriding the passed
   * date.
   *
   * @throws ValidatorException If the license does not validate.  If thrown, the license should
   * not be considered valid.
   */
  public LicenseState validate(Date currentDate) throws ValidatorException {

    // All collected results should go into this set.
    List<TestResult> results = new ArrayList<TestResult>();

    // If the license hasn't been signed, throw the exception - we can't validate license date
    // if we can't be sure it hasn't been tampered with.
    TestResult result = new TestResult(LicenseTest.SIGNED, license.getLicenseSignatureString() != null);
    results.add(result);
    
    if (!result.passed()) {
      throw new ValidatorException(new LicenseState(results));
    }


    // Now verify that the signature can be verified
    result = sigValidator.validate(license, publicKey);
    results.add(result);

    // If the signature can't be verified throw the exception.  Same reasoning as above.
    if (!result.passed()) {
      throw new ValidatorException(new LicenseState(results));
    }

    // Verify the date, if desired
    if (checkClockTurnback && !isGae()) {
      currentDate = getLatestModifiedDate(currentDate);
    }
    
    // Check all other plugins
    ValidationParameters params = ValidationParameters.createParameters(ignoreFloatTime,
            currentDate, Collections.unmodifiableSet(blacklist));

    for (ValidatorPlugin plugin : plugins) {
      results.add(plugin.validate(license, params));
    }

    LicenseState state = new LicenseState(results);
    
    if (!state.isValid()) {
      throw new ValidatorException(state);
    }

    return state;
  }

  /**
   * Get the time remaining in this license's validity period.  This method looks at the hardcoded
   * expiration date and/or a floating expiration period and takes the earlier of the two expiration
   * dates.  Note that when computing the time remaining, the license validity it not taken into
   * account, so to ensure license validity {@link #validate() validate()} should be called prior to using the return
   * value from this method.
   * 
   * @param currentDate The current system date.
   * @return The number of milliseconds remaining in the license validity period, or null if this
   * license does not expire.
   */
  public Long getTimeRemaining(Date currentDate) {
    return expiredPlugin.getTimeRemaining(currentDate, license);
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private boolean isGae() {
    String appEngineVersion = System.getProperty("com.google.appengine.runtime.version");
    return appEngineVersion != null;
  }

  private boolean validateFolder(File folder) {
    return folder.isDirectory() && folder.exists() && folder.canRead();
  }

  private Set<File> getFolderSubset(File folder) {
    Set<File> set = new HashSet<File>();

    File[] children = folder.listFiles();
    int childLength = children.length;
    if (childLength <= FILE_LIMIT) {
      set.addAll(Arrays.asList(children));
    } else {
      // Pick FILE_LIMIT random indices between 0 - children.length-1 and
      // use those files
      Random r = new Random();
      for (int x = 0 ; x < FILE_LIMIT ; x++) {
        set.add(children[r.nextInt(childLength)]);
      }
    }

    return set;
  }
  
  /**
   * Search through all files listed within the modifiedSearchSet, looking
   * for the one with the latest modification date.  Compare that one with
   * the date provided and return the later of the two.
   */
  private Date getLatestModifiedDate(Date d) {
    Date latest = d;

    for (Iterator<File> iter = modifiedSearchSet.iterator(); iter.hasNext();) {

      File folder = iter.next();

      if (!validateFolder(folder)) {
        logger.finer("Folder " + folder + " does not appear readable, skipping");
        continue;
      }

      Set<File> children = getFolderSubset(folder);

      for (File file : children) {
        if (file.lastModified() > latest.getTime()) {
          latest = new Date(file.lastModified());
        }
      }
    }
    if (latest.getTime() != d.getTime()) {
      logger.finer("Changing validation date from " + d + " to " + latest);
    }
    return latest;

  }

  private PublicKey convertPublicKey(String publicKey, boolean isRSA) {
    PublicKey pub = null;
    try {
      byte[] pubKey = Hex.decodeHex(publicKey.toCharArray());
      X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubKey);
      KeyFactory keyFactory = KeyFactory.getInstance(isRSA ? "RSA" : "DSA");
      if (isRSA) {
        pub = (RSAPublicKey) keyFactory.generatePublic(pubSpec);
      } else {
        pub = (DSAPublicKey) keyFactory.generatePublic(pubSpec);
      }
    } catch (InvalidKeySpecException ex) {
      logger.log(Level.FINE, null, ex);
    } catch (NoSuchAlgorithmException ex) {
      logger.log(Level.FINE, null, ex);
    } catch (DecoderException ex) {
      logger.log(Level.FINE, null, ex);
    } catch (ClassCastException ex) {
      logger.log(Level.FINE, null, ex);
    }

    return pub;
  }

  //---------------------------- Property Methods -----------------------------

  /**
   * Add a validator plugin to the validation routine.  Any plugins are executed
   * after Padlock's internal license checks are completed.  This means that when a
   * plugin executes, it can be assured that the license is cryptographically valid
   * and within the valid dates.
   *
   * @param plugin The ValidatorPlugin implementation to add.
   */
  public synchronized void addPlugin(ValidatorPlugin plugin) {
    if (plugin != null && !plugins.contains(plugin)) {
      plugins.add(plugin);
    }
  }

  /**
   * Remove a particular plugin from the validation list.
   * @param plugin The plugin to be removed.
   */
  public synchronized void removePlugin(ValidatorPlugin plugin) {
    plugins.remove(plugin);
  }

  /**
   * Add a license signature to the validation blacklist.  Signatures
   * which are in the validator blacklist will fail validation.
   * @param signature The signature String to add to the blacklist.
   */
  public synchronized void addBlacklistedLicense(String signature) {
    if (signature != null) {
      blacklist.add(signature);
    }
  }

  /**
   * Remove a license signature String from the validator blacklist.
   * @param signature The signature to be removed.
   */
  public synchronized void removeBlacklistedLicense(String signature) {
    blacklist.remove(signature);
  }

  /**
   * Return a copy of the Validator's internal license blacklist.
   * @return A blacklist copy.
   */
  public synchronized Set<String> getBlacklistedLicenses() {
    return new HashSet<String>(blacklist);
  }

  /**
   * Set the Validator to use or ignore the License's floating expiration period when performing
   * a validation.
   * @param ignoreFloatTime If true, the Validator will not include a check for the license floating
   * expiration date.
   */
  public synchronized void setIgnoreFloatTime(boolean ignoreFloatTime) {
    this.ignoreFloatTime = ignoreFloatTime;
  }

  /**
   * Get the status of the IgnoreFloatTime flag.  See {@link #setIgnoreFloatTime(boolean) setIgnoreFloatTime()}.
   * @return True if the Validator will be ignoring the float time value, or false otherwise.
   */
  public synchronized boolean getIgnoreFloatTime() {
    return ignoreFloatTime;
  }

  /**
   * Set the Validator to use or ignore the clock turnback checking.  When ignored, the Validator will
   * assume the system time or the time passed to the validate() method is correct.
   *
   * @param checkClockTurnback Set to true to ignore clock turnback checking.
   */
  public synchronized void setCheckClockTurnback(boolean checkClockTurnback) {
    this.checkClockTurnback = checkClockTurnback;
  }

  /**
   * Get the Validator's checkClockTurnback status.  See {@link #getCheckClockTurnback() getCheckClockTurnback()}.
   * @return The status of the checkClockTurnback feature.
   */
  public synchronized boolean getCheckClockTurnback() {
    return checkClockTurnback;
  }
}
