/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.padlocksoftware.padlock.key;

import net.padlocksoftware.padlock.KeyManager;
import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import junit.framework.TestCase;

/**
 *
 * @author jason
 */
public class KeyManagerTest extends TestCase {
    
    public KeyManagerTest(String testName) {
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

  public void testKeyManager() throws Exception {
    System.out.println("testKeyManager");
    
    KeyPair pair = KeyManager.createKeyPair();
    File file = new File("test.key");

    KeyManager.exportKeyPair(pair, file);
    KeyPair pair2 = KeyManager.importKeyPair(file);

    PublicKey public1 = pair.getPublic();
    PublicKey public2 = pair2.getPublic();
    assertEquals(public1, public2);

    PrivateKey private1 = pair.getPrivate();
    PrivateKey private2 = pair2.getPrivate();
    assertEquals(private1, private2);
    file.delete();
  }


}
