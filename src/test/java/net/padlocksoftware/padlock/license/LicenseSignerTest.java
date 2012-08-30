/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.padlocksoftware.padlock.license;

import java.io.File;
import java.security.KeyPair;
import java.security.interfaces.DSAPrivateKey;
import junit.framework.TestCase;
import net.padlocksoftware.padlock.KeyManager;

/**
 *
 * @author Jason
 */
public class LicenseSignerTest extends TestCase {
    
    public LicenseSignerTest(String testName) {
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

  /**
   * Test of sign method, of class LicenseSigner.
   */
  public void testSign() throws Exception {
    System.out.println("sign");
    License license = LicenseFactory.createLicense();
    license.addProperty("Name", "Jason Nichols");
    license.addProperty("Email", "jason@padlocksoftware.net");

    KeyPair pair = KeyManager.createKeyPair();

    LicenseSigner signer = LicenseSigner.createLicenseSigner((DSAPrivateKey)pair.getPrivate());
    signer.sign(license);
    LicenseIO.exportLicense(license, new File("testSigned.lic"));
    assertTrue(license.isSigned());

  }

}
