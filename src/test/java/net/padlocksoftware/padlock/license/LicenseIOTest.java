/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.padlocksoftware.padlock.license;

import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author jason
 */
public class LicenseIOTest extends TestCase {
    
    public LicenseIOTest(String testName) {
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

    public void testLicenseIO() throws Exception {
      System.out.println("testLicenseIO");

      License license = LicenseFactory.createLicense();
      license.addProperty("name", "Jason Nichols");
      license.addProperty("email", "jason@padlocksoftware.net");
      license.setFloatingExpirationPeriod(200000L);

      File file = new File("test.lic");

      LicenseIO.exportLicense(license, file);

      License license2 = LicenseIO.importLicense(file);

      assertTrue(license.equals(license2));

      file.delete();
    }
}
