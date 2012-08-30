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
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.padlocksoftware.padlock.validator.Validator;
import net.padlocksoftware.padlock.validator.ValidatorException;
import net.padlocksoftware.padlock.validator.plugins.PadlockPlugin;

/**
 * This class is used internally by Padlock and has no public use scenarios.  No further
 * documentation will be provided.
 * 
 * @author Jason Nichols (jason@padlocksoftware.net)
 * @since 2.0
 */
public final class PadlockState {
  
  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  // Public key owned by Padlock Software
  private static final String pubKeyV1 =
          "30820122300d06092a864886f70d01010105000382010" +
          "f003082010a0282010100abc3c8885e8f316b8bf85237" +
          "c19c62d5f8de599047244432ec39c593e3d8a7d02b0c0" +
          "8d9022b0eae4fd126e9323ce7805a998abf0ced9dfadc" +
          "70f6a86d536feb1e949354730db47d232cde879ffd57a" +
          "f8ade19be70d3d91823847727945551e7a46951a71bc6" +
          "3e821260806b292bcfa433273a2fcf2e4274238314f87" +
          "35ab650085a08caeb138e5a6f3aa08dbcc55ab38a458d" +
          "0008b617ffe262469554c63415e744d5faecb44e3fcdb" +
          "b63122940bf87f428de3214ab178dbefe50e8fe83ddb9" +
          "0c8d49b2d4f51cdeb72b954a9884a1f5c1b39e76c6b19" +
          "64b0edc3221f403d4cca3f2f1e1b2a3a73fda0a7e8cc2" +
          "0cac3a667a42d70c2563f5816e51d14ab7650d0203010" +
          "001";

	 private static final String pubKeyV2 =
		"308201b73082012c06072a8648ce3804013082011f02818100fd7f53811d" +
		"75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d40" +
		"2251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b" +
		"9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04" +
		"fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd1" +
		"4801c70215009760508f15230bccb292b982a2eb840bf0581cf502818100" +
		"f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d07" +
		"82675159578ebad4594fe67107108180b449167123e84c281613b7cf0932" +
		"8cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1" +
		"fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b5525" +
		"64014c3bfecf492a038184000281801324955992c6d648cc750935dfc867" +
		"e2c59eba301ab21f35ca372d476abeac1acd67fc150018df9792b5fd9a34" +
		"86fe164bae04a83758f0bd01706f4499de7031fab6ff2c14497f48bcc25e" +
		"99750849db348b3b79de96f91f93869ac36e7afc573d9ea5ce864872972e" +
		"b6830858ff6bc2510cb9fc4de3f2943fa25b91a3b80799";

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final Logger logger;

  private License license;

  private final boolean isValid;

  private final String description;

  private File licenseFile;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public PadlockState(License license) {

    licenseFile = null;

    logger = Logger.getLogger(getClass().getName());
    logger.fine("Initializing PadlockState using license: " + license);
    if (license == null) {
      File padlockFolder = getPadlockFolder();
      if (padlockFolder == null) {
        logger.fine("Cannot determine Padlock folder, falling back to Demo mode");
        isValid = false;
        description = "Cannot determine Padlock folder.  Please contact Padlock support.";
        return;
      }

      File padlockFile = new File(padlockFolder, "Padlock.lic");
      if (!padlockFile.exists() || !padlockFile.canRead()) {
        logger.fine("Cannot read Padlock.lic, falling back to Demo mode");
        isValid = false;
        description = "No Padlock license file found.";
        return;
      }

      try {
        licenseFile = padlockFile;
        this.license = LicenseIO.importLicense(padlockFile);
      } catch (Exception ex) {
        isValid = false;
        description = "Cannot import Padlock.lic";
        logger.log(Level.WARNING, null, ex);
        return;
      }
    } else {
      this.license = license;
    }

    // At this point we've got a valid license reference.
    if (this.license != null) {
      logger.fine("License has version " + this.license.getLicenseVersion());
    } else logger.fine("No license instance, assuming V2 for validation");

    
    String publicKey = (this.license != null && this.license.getLicenseVersion() == 1 ? pubKeyV1 : pubKeyV2);
    Validator validator = new Validator(this.license, publicKey);
    validator.addPlugin(new PadlockPlugin());

    LicenseState state;
    try {
      state = validator.validate();
//      validator.validate();  <-- What was this doing here?
    } catch (ValidatorException ex) {
      state = ex.getLicenseState();
      logger.fine("Could not validate Padlock license file: " + ex.getMessage() + "\nFalling back to " +
              "demo mode");
    }

    isValid = state.isValid();
    if (isValid()) {
      description = "Padlock license is valid and current";
    } else {
      description = state.getFailedTests().get(0).getResultDescription();
    }
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  /**
   * Used internally by Padlock applications.  Returns a reference to the
   * folder containing the Padlock jar file.
   */
  private static File getPadlockFolder() {
    File f = null;

    try {
      URL url = net.padlocksoftware.padlock.license.LicenseSigner.class.getProtectionDomain().getCodeSource().getLocation();
      f = new File(url.getFile()).getParentFile();
    } catch (Exception ex) {
      Logger.getLogger(License.class.getName()).log(Level.SEVERE, null, ex);
    }

    return f;
  }

  //---------------------------- Property Methods -----------------------------

  public File getPadlockFile() {
    return licenseFile;
  }

  public License getLicense() {
    synchronized(this) {
      return license;
    }
  }
  
  public boolean isValid() {
    return isValid;
  }

  public String getStateDescription() {
    return description;
  }

  public String getName() {

    if (license == null) return "";

    if (license.getLicenseVersion() == 1) {
      return license.getProperty("Name");
    } else {
      return license.getProperty("name");
    }
  }

  public String getCompanyName() {

    if (license == null) return "";

    if (license.getLicenseVersion() == 1) {
      return license.getProperty("Company");
    } else {
      return license.getProperty("company");
    }
  }

  public String getEmail() {

    if (license == null) return "";
    
    if (license.getLicenseVersion() == 1) {
      return license.getProperty("Email");
    } else {
      return license.getProperty("email");
    }
  }

  public int getSupportLength() {

    if (license == null) return 0;
    
    int length = 0;

    try {
      if (license.getLicenseVersion() == 1) {
        length = Integer.parseInt(license.getProperty("supportLength"));
      } else {
        length = Integer.parseInt(license.getProperty("product_padlock_supportLength"));
      }
    } catch (Exception e) {}

    return length;
  }

  public int getMajorVersion() {

    if (license == null) return 0;

    int version = 2;

    try {
      if (license.getLicenseVersion() == 1) {
        version = Integer.parseInt(license.getProperty("majorVersion"));
      } else {
        version = Integer.parseInt(license.getProperty("product_padlock_majorVersion"));
      }
    } catch (Exception e) {}


    return version;
  }
}
