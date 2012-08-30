/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.padlocksoftware.padlock.validator;

import java.io.File;
import java.net.NetworkInterface;
import java.security.KeyPair;
import java.security.interfaces.DSAPrivateKey;
import java.util.Date;
import java.util.Enumeration;
import junit.framework.TestCase;
import net.padlocksoftware.padlock.KeyManager;
import net.padlocksoftware.padlock.license.License;
import net.padlocksoftware.padlock.license.LicenseFactory;
import net.padlocksoftware.padlock.license.LicenseIO;
import net.padlocksoftware.padlock.license.LicenseSigner;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Jason
 */
public class ValidatorTest extends TestCase {
  
  private static final String pubKey =
      "30819f300d06092a864886f70d010101050003818d003"
      +"081890281810089ccb3d72a67931355c52dd93c5d9c3e"
      +"b5e9696c2be399c0d4776065703c555456bcc229294e6"
      +"472f7d956b61b7a47bd757ed6ad5a3186e6561d5e5d3c"
      +"916057214100741fe518b05b21bddf471f92975a276ad"
      +"9f53510f565988501f74d84f9bc9185b7ef73267f2073"
      +"14612264b5e9f660eb4fb3d440a80d2dec539a85de110"
      +"203010001";

  public ValidatorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

  public void testLegacyValidate() throws Exception {
    System.out.println("validate - Legacy");
    File licenseFile = new File("testing", "LegacyTest.lic");
    License license = LicenseIO.importLicense(licenseFile);
    Validator validator = new Validator(license, pubKey);
    validator.validate();
  }

  /**
   * Test of validate method, of class Validator.
   */
  public void testValidate() throws Exception {
    System.out.println("validate - Simple");

    KeyPair pair = KeyManager.createKeyPair();
    License license = LicenseFactory.createLicense();
    license.addProperty("Name", "Jason Nichols");
    license.addProperty("Email", "jason@padlocksoftware.net");
    license.addProperty("Gibberish", "qwertyasdfg");

    LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
    signer.sign(license);

    String key = new String(Hex.encodeHex(pair.getPublic().getEncoded()));
    Validator validator = new Validator(license, key);
    validator.validate();
  }


//  public void testHardware() throws Exception {
//    System.out.println("validate - Hardware");
//
//    KeyPair pair = KeyManager.createKeyPair();
//    License license = LicenseFactory.createLicense();
//    license.addProperty("Name", "Jason Nichols");
//    license.addProperty("Email", "jason@padlocksoftware.net");
//    license.addProperty("Gibberish", "qwertyasdfg");
//
//    Enumeration <NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//    while (interfaces.hasMoreElements()) {
//      NetworkInterface iFace = interfaces.nextElement();
//      if (iFace.getHardwareAddress() != null && !iFace.isLoopback() && !iFace.isVirtual()) {
//        license.addHardwareAddress(Hex.encodeHex(iFace.));
//      }
//    }
//
//    LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
//    signer.sign(license);
//
//    String key = new String(Hex.encodeHex(pair.getPublic().getEncoded()));
//    Validator validator = new Validator(license, key);
//
//    validator.validate();
//
//    System.out.println("validate - Hardware missing");
//    license = LicenseFactory.createLicense();
//    license.addProperty("Name", "Jason Nichols");
//    license.addProperty("Email", "jason@padlocksoftware.net");
//    license.addProperty("Gibberish", "qwertyasdfg");
//    license.addHardwareAddress("001234567890");
//
//    signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
//    signer.sign(license);
//
//    validator = new Validator(license, key);
//
//    boolean thrown = false;
//    try {
//      validator.validate();
//    } catch (Exception e) {
//      System.out.println("\t" + e.getMessage());
//      thrown = true;
//    }
//
//    assertTrue(thrown);
//
//  }

  public void testBlacklist() throws Exception {
    System.out.println("validate - Blacklist");

    KeyPair pair = KeyManager.createKeyPair();
    License license = LicenseFactory.createLicense();
    license.addProperty("Name", "Jason Nichols");
    license.addProperty("Email", "jason@padlocksoftware.net");
    license.addProperty("Gibberish", "qwertyasdfg");

    LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
    signer.sign(license);

    String key = new String(Hex.encodeHex(pair.getPublic().getEncoded()));
    Validator validator = new Validator(license, key);
    validator.addBlacklistedLicense(license.getLicenseSignatureString());
    boolean ex = false;
    try {
      validator.validate();
    } catch (ValidatorException e) {
      System.out.println("\t" + e.getMessage());
      ex = true;
    }
    assertTrue(ex);
  }

  public void testPrior() throws Exception {
    System.out.println("validate - Prior");

    KeyPair pair = KeyManager.createKeyPair();
    License license = LicenseFactory.createLicense();
    license.setStartDate(new Date(System.currentTimeMillis() + 20000L));

    LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
    signer.sign(license);

    String key = new String(Hex.encodeHex(pair.getPublic().getEncoded()));
    Validator validator = new Validator(license, key);
    boolean ex = false;
    try {
      validator.validate();
    } catch (ValidatorException e) {
      System.out.println("\t" + e.getMessage());
      ex = true;
    }
    assertTrue(ex);
  }

  public void testExpired() throws Exception {
    System.out.println("validate - Expired");

    KeyPair pair = KeyManager.createKeyPair();
    License license = LicenseFactory.createLicense();
    license.setStartDate(new Date(100));
    LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
    signer.sign(license);

    String key = new String(Hex.encodeHex(pair.getPublic().getEncoded()));
    Validator validator = new Validator(license, key);
    boolean ex = false;
    try {
      validator.validate();
    } catch (ValidatorException e) {
      System.out.println("\t" + e.getMessage());
      ex = true;
    }
    assertTrue(ex);
  }

  public void testExpiredFloat() throws Exception {
    System.out.println("validate - Expired Float");

    KeyPair pair = KeyManager.createKeyPair();
    License license = LicenseFactory.createLicense();
    license.setFloatingExpirationPeriod(1L);
    LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
    signer.sign(license);

    String key = new String(Hex.encodeHex(pair.getPublic().getEncoded()));
    Validator validator = new Validator(license, key);

    validator.validate();

    boolean ex = false;
    try {
      validator.validate();
    } catch (ValidatorException e) {
      System.out.println("\t" + e.getMessage());
      ex = true;
    }
    assertTrue(ex);
  }

}
