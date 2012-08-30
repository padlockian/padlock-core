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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * This class provides static methods for license I/O.  The LicenseIO class attempts to provide
 * the ultimate flexibility in importing/exporting license instances by offering Input and Output
 * streams as method paramters.  The other methods exist for backward compatability.
 * @author Jason Nichols(jason@padlocksoftware.net)
 */
public final class LicenseIO {

    private LicenseIO() {}

    /**
     * Import a License object from a properly formatted license file.  The
     * license may be either signed or unsigned. A successful import doesn't
     * imply the license file is valid, but that the required fields are
     * present. Validation is required to determine license validity.
     * @param file The license file to import
     * @return The License object stored in the file.
     * @throws java.io.IOException For any file related exceptions
     * @throws net.padlocksoftware.license.ImportException If any required
     * properties are missing or invalid.
     */
    public static  License importLicense(File file) throws IOException,
            ImportException {
        FileInputStream stream = new FileInputStream(file);
        return importLicense(stream);
    }

    /**
     * Import a license from the classpath, with the License file located
     * by the path parameter.
     * @param classLoader The classloader to use to locate the license
     * @param path The path of the License file
     * @return The License object stored in the file.
     * @throws java.io.IOException For any stream related exceptions
     * @throws net.padlocksoftware.license.ImportException If any required
     * properties are missing or invalid.
     * @since 1.1
     */
    public static License importLicense(ClassLoader classLoader,String path)
            throws IOException, ImportException {
        License license = null;
        Properties props = new Properties();
        InputStream is = classLoader.getResourceAsStream(path);
        props.load(is);
        is.close();

        license = new LicenseImpl(props);

        return license;
    }

    /**
     * Import a license from a single String object.  The entire license
     * should be represented in the typical Properties format.
     * @param licenseString The string containing the license data
     * @return The License object retrieved from the String.
     * @throws java.io.IOException For any stream related exceptions
     * @throws net.padlocksoftware.license.ImportException If any required
     * properties are missing or invalid.
     * @since 1.1
     */
    public static License importLicense(String licenseString) throws IOException,
            ImportException {
        License license = null;
        Properties props = new Properties();
        ByteArrayInputStream bis = new ByteArrayInputStream(licenseString.getBytes("UTF-8"));

        props.load(bis);
        bis.close();

        license = new LicenseImpl(props);

        return license;
    }

    /**
     * Imports a license from any valid InputStream.
     * @param stream The stream to import the license from. The stream will be
     * closed after the license is imported.
     * @return The License object retrieved from the stream.
     * @throws java.io.IOException For any stream related exceptions
     * @throws net.padlocksoftware.license.ImportException If any required
     * properties are missing or invalid.
     * @since 1.1
     */
    public static License importLicense(InputStream stream) throws IOException,
            ImportException {

        License license = null;
        Properties props = new Properties();
        props.load(stream);
        stream.close();

        license = new LicenseImpl(props);

        return license;
    }

    /**
     * 
     * @param props
     * @return The newly imported License instance
     * @throws ImportException for any IO license creation/formatting errors.
     */
    public static License importLicense(Properties props) throws ImportException {
      return new LicenseImpl(props);
    }

   /**
     * Export a License object to an output stream.  The License may be either
     * signed or unsigned.
     * @param license The license object to be exported.
     * @param stream The stream to write the license object to.
     * @throws java.io.IOException For any output stream related errors
     * @throws java.lang.IllegalArgumentException If either parameter is null
     * @since 1.1
     */
    public static void exportLicense(License license, OutputStream stream)
            throws IOException {
        Properties props = ((LicenseImpl)license).props;
        props.store(stream, null);
    }


    /**
     * Export a License object to an output file.  The License may be either
     * signed or unsigned.
     * @param license The license object to be exported.
     * @param file The file to write the license object to.  If this already
     * exists it will be overwritten.
     * @throws java.io.IOException For any output file related errors
     * @throws java.lang.IllegalArgumentException If either parameter is null
     */
    public static void exportLicense(License license, File file)
        throws IOException {
        if (license == null) {
            throw new IllegalArgumentException("License may not be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }

        //
        // The base of our license file.
        //
        Properties props = ((LicenseImpl)license).props;
        FileOutputStream fos = new FileOutputStream(file);
        props.store(fos, null);
        fos.close();
    }
}
