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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAPrivateKey;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;

/**
 * Public factory class for signing Licenses.  The LicenseSigner is aware of whether the
 * Padlock library is running with a valid license, and if not, it will automatically add
 * (or override as required) the expiration date of the license to be signed. To determine this,
 * the LicenseSigner looks in the same folder as the Padlock jar file for a file named "Padlock.lic"
 * and attempts to validate itself using that.
 * 
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class LicenseSigner {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final long TWO_WEEKS_IN_MS = 1000L * 3600L * 24L * 14L;
  
  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Create a new LicenseSigner object. This instance can sign any number of licenses using a
   * single PrivateKey.  This method assumes that the Padlock license file resides in the same
   * folder as the Padlock jar file.
   * @param privateKey The DSA (Padlock 2.x) Private Key to sign the License objects with.
   * @return A new LicenseSigner instance.
   */
  public static LicenseSigner createLicenseSigner(DSAPrivateKey privateKey) {
    return createLicenseSigner(privateKey, (License)null);
  }

  /**
   * Create a new LicenseSigner object. This instance can sign any number of licenses using a
   * single PrivateKey.
   * @param privateKey The DSA (Padlock 2.x) Private Key to sign the License objects with.
   * @param padlockLicense The instance of the Padlock License,  purchased from Padlock
   * Software LLC.
   * @return A new LicenseSigner instance.
   * @since 2.0.2
   */
  public static LicenseSigner createLicenseSigner(DSAPrivateKey privateKey, License padlockLicense) {
    if (privateKey == null) {
      throw new InvalidParameterException("privateKey cannot be null");
    }

    return new LicenseSigner(privateKey, padlockLicense);
  }

  /**
   * Create a new LicenseSigner object. This instance can sign any number of licenses using a
   * single PrivateKey.
   * @param privateKey The DSA (Padlock 2.x) Private Key to sign the License objects with.
   * @param padlockLicenseFile The instance of the Padlock License file,  purchased from Padlock
   * Software LLC.
   * @return A new LicenseSigner instance.
   * @throws IOException If any file related errors occur.
   * @throws ImportException If the license is malformed or missing required data.
   * @since 2.0.2
   */
  public static LicenseSigner createLicenseSigner(DSAPrivateKey privateKey, File padlockLicenseFile)
    throws IOException {
    License padlockLicense = LicenseIO.importLicense(padlockLicenseFile);
    return createLicenseSigner(privateKey, padlockLicense);

  }

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final Logger logger;

  private final DSAPrivateKey privateKey;

  private final License padlockLicense;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private LicenseSigner(DSAPrivateKey privateKey, License padlockLicense) {
    logger = Logger.getLogger(getClass().getName());

    this.padlockLicense = padlockLicense;
    this.privateKey = privateKey;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * Sign the supplied license using the PrivateKey given at the LicenseSigner instantation.
   * @param license The license to sign.  Once signed, a license cannot be modified.
   * @throws IllegalStateException If the license has already been signed.
   */
  public void sign(License license) {
    synchronized (license) {

      if (license.isSigned()) {
        throw new IllegalStateException("License is already signed");
      }

      // If we're in demo mode, hard code a license expiration date
      PadlockState state = new PadlockState(padlockLicense);

      if (!state.isValid()) {
        Date startDate = license.getStartDate();
        logger.fine("Demo mode - overriding the License expiration date");
        license.setExpirationDate(new Date(startDate.getTime() + TWO_WEEKS_IN_MS));
      }
      
      String licenseString = ((LicenseImpl) license).concatenate();
      try {
        Signature signature = Signature.getInstance("SHA1withDSA");
        signature.initSign(privateKey);
        signature.update(licenseString.getBytes("UTF-8"));
        byte[] sig = signature.sign();
        String sigString = new String(Hex.encodeHex(sig));
        ((LicenseImpl) license).props.setProperty("signature", sigString);
      } catch (SignatureException ex) {
        logger.log(Level.SEVERE, "Signature failure.  Please verify a DSA security provider is available.", ex);
      } catch (InvalidKeyException ex) {
        logger.log(Level.SEVERE, "Key failure.  Please verify a DSA security provider is available.", ex);
      } catch (NoSuchAlgorithmException ex) {
        logger.log(Level.SEVERE, "Signature creation failure.  Please verify a DSA security provider is available.", ex);
      } catch (UnsupportedEncodingException ex) {
        logger.log(Level.SEVERE, "Encoding Exception.  Please report to support@padlocksoftware.net", ex);
      }

    }

  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------
  
  //---------------------------- Property Methods -----------------------------

}
