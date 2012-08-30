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

package net.padlocksoftware.padlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;

/**
 * This class provides static methods for DSA KeyPair generation plus file and stream I/O.
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
final public class KeyManager {

    /**
     * Create a 1024 bit DSA KeyPair.
     * @return A newly created DSA KeyPair.
     */
    public static KeyPair createKeyPair(){
        KeyPair pair = null;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            keyGen.initialize(1024);
            pair = keyGen.genKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KeyManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return pair;
    }

    /**
     * Export the supplied Keypair to an output Stream.
     *
     * @param pair The KeyPair to export.  KeyPairs should only be pairs
     * created with the createKeyPair(int) method.
     *
     * @param stream The stream to write the KeyPair to.  Key streams contain both the
     * public and private keys and should be secured.
     *
     * @throws java.io.IOException For any Stream IO related exceptions
     * @throws java.lang.NullPointerException If either parameter is null
     * @since 2.0
     */
    public static void exportKeyPair(KeyPair pair, OutputStream stream) throws IOException{
        if (pair == null) {
            throw new IllegalArgumentException("KeyPair may not be null");
        }

        if (stream == null) {
            throw new IllegalArgumentException("Stream may not be null");
        }

        //
        // Turn the keypair into properties
        //
        Properties p = new Properties();

        String pri = new String(Hex.encodeHex(pair.getPrivate().getEncoded()));
        String pub = new String(Hex.encodeHex((pair.getPublic().getEncoded())));
        p.setProperty("public", pub);
        p.setProperty("private", pri);

        p.store(stream, null);
        stream.flush();
        stream.close();

    }
    
    /**
     * Export the supplied Keypair to an output File.
     * @param pair The KeyPair to export.  KeyPairs should only be pairs
     * created with the createKeyPair(int) method.
     * @param file The file to save the KeyPair to.  Key files contain both the
     * public and private keys and should be secured.
     * @throws java.io.IOException For any File IO related exceptions
     * @throws java.lang.NullPointerException If either parameter is null
     */
    public static void exportKeyPair(KeyPair pair, File file)
        throws IOException{
        if (pair == null) {
            throw new IllegalArgumentException("KeyPair may not be null");
        }

        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }
        exportKeyPair(pair, new FileOutputStream(file));
    }

    /**
     * Import a Padlock 2.x (DSA based) KeyPair from an InputStream.  The stream is
     * assumed to have been previously exported in a supported format using the
     * exportKeyPair methods.
     * @param stream The KeyPair stream to import.
     * @return The DSA KeyPair contained in the specified file.
     * @throws java.io.IOException If file is missing or contain invalid data.
     * @since 2.0
     */
    public static KeyPair importKeyPair(InputStream stream) throws IOException {
      if (stream == null)
        throw new IllegalArgumentException("Stream cannot be null");

        KeyPair pair = null;
        Properties p = new Properties();
        p.load(stream);
        stream.close();
        String pri = p.getProperty("private");
        String pub = p.getProperty("public");

        if (pri == null || pub ==  null) {
            throw  new IOException("Stream data is invalid");
        }

        // Load the keys
        try {

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Hex.decodeHex(pri.toCharArray()));
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Hex.decodeHex(pub.toCharArray()));
            PublicKey publicKey = keyFactory.generatePublic(pubSpec);

            pair = new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Invalid stream: " + e.getMessage());
        }

        return pair;
    }

    /**
     * Import a Padlock 2.x (DSA based) KeyPair from a file.  The file is assumed to have been
     * previously exported in a supported format using the
     * exportKeyPair(KeyPair, File) method.
     * @param file The DSA KeyPair file to import.
     * @return The KeyPair contained in the specified file.
     * @throws java.io.IOException If file is missing or contain invalid data.
     */
    public static KeyPair importKeyPair(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }

        return importKeyPair(new FileInputStream(file));
    }
}
