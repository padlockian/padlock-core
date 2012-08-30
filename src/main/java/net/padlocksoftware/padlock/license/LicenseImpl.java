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

import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public final class LicenseImpl implements License {

  @SuppressWarnings("ProtectedMemberInFinalClass")
  protected static Set<String> propertyNames(Properties p) {
    Set<String> set = new HashSet<String>();

    Enumeration<Object> e = p.keys();
    while (e.hasMoreElements()) {
      set.add(e.nextElement().toString());
    }

    return set;
  }

  private static final String prependString = "property_";


  private final Logger logger;

  Properties props;

  protected LicenseImpl() {
    this(new Date());
  }

  protected LicenseImpl(Date startDate) {
    logger = Logger.getLogger(getClass().getName());
    props = new Properties();
    setCreationDate(new Date());
    setStartDate(startDate);
    setLicenseVersion(2);
  }

  protected LicenseImpl(Properties properties) {
    logger = Logger.getLogger(getClass().getName());
    props = new Properties();
    for (String key : propertyNames(properties)) {
      props.put(key, properties.get(key));
    }

    // Check for mandatory fields

    // Creation Date must exist
    Date creationDate = getCreationDate();
    if (creationDate == null) {
      throw new ImportException("Creation date is invalid");
    }

    // Start date must exist
    Date startDate = getStartDate();
    if (startDate == null) {
      throw new ImportException("Start date is invalid");
    }

    // Manually get the expiration date, since the public method may
    // return null.  We need to differentiate a missing date from an
    // invalid date.
    String expString = props.getProperty("expirationDate");
    if (expString != null) {
      try {
        Date expirationDate = new Date(Long.parseLong(expString));
      } catch (NumberFormatException e) {
        throw new ImportException("Expiration date is invalid");
      }
    }

    // If the license has a version entry, it must be valid
    String version = props.getProperty("version");
    if (version != null) {
      try {
        Integer.parseInt(version);
      } catch (Exception e) {
        throw new ImportException("License version is invalid");
      }
    }
  }

  /**
   * Concatenate all keys and values into a single String
   */
  public String concatenate() {
    StringBuffer buffer = new StringBuffer();

    TreeSet<String> keys = new TreeSet<String>();
    keys.addAll(propertyNames(props));

    for (String key : keys) {
      String value = props.getProperty(key);
      if (value != null && !"signature".equals(key)) {
        buffer.append(key);
        buffer.append(value);
      }
    }

    return buffer.toString();
  }
  
  protected void setLicenseVersion(int version) {
    if (isSigned()) {
      throwSignedException();
    }

    props.setProperty("version", Integer.toString(version));
  }

  protected void setCreationDate(Date creationDate) {
    if (isSigned()) {
      throwSignedException();
    }

    props.setProperty("creationDate", Long.toString(creationDate.getTime()));
  }

  private void throwSignedException() {
    throw new IllegalStateException("Cannot modify a signed license");
  }

  ////////////////////////////////////////////////////////////////////////////
  // Utility Methods
  ////////////////////////////////////////////////////////////////////////////
  
  private String convertInterfaceBytesToString(byte[] interfaceBytes) {
    return new String(Hex.encodeHex(interfaceBytes));
  }

  private SortedSet<String> extractHardwareAddresses(String addresses) {
    TreeSet<String> set = new TreeSet<String>();

    // Addresses are a space separated String of hex characters
    for (String address : addresses.split(" ")) {
      address = address.trim();
      if (address.length() > 0) {
        set.add(address);
      }
    }

    return set;
  }

  private String combineHardwareAddresses(Set<String> set) {
    String addresses = "";

    for (String address : set) {
      addresses += address + " ";
    }

    return addresses.trim();
  }

  ////////////////////////////////////////////////////////////////////////////
  // Interface Methods
  ////////////////////////////////////////////////////////////////////////////
  
  public synchronized Date getCreationDate() {
    String str = props.getProperty("creationDate");
    long time = Long.parseLong(str);
    return new Date(time);
  }

  public synchronized Date getExpirationDate() {
    Date expirationDate = null;
    String str = props.getProperty("expirationDate");
    if (str != null) {
      long time = Long.parseLong(str);
      expirationDate = new Date(time);
    }

    return expirationDate;
  }

  public synchronized Date getStartDate() {
    String str = props.getProperty("startDate");
    long time = Long.parseLong(str);
    return new Date(time);
  }

  public synchronized Long getFloatingExpirationPeriod() {
    Long period = null;

    String str = props.getProperty("floatingExpiration");
    if (str != null) {
      try {
        period = Long.parseLong(str);
      } catch (Exception e) {
      }
    }
    return period;

  }

  public synchronized Properties getProperties() {
    Properties properties = new Properties();
    for (String property : propertyNames(props)) {
      String value = props.getProperty(property);
      if (property.startsWith(prependString)) {
        properties.setProperty(property.substring(prependString.length()), value);
      }
    }

    return properties;
  }

  public synchronized String getProperty(String propertyName) {
    return getProperty(propertyName, null);
  }

  public synchronized String getProperty(String propertyName, String defaultValue) {
    String value = defaultValue;
    propertyName = prependString + propertyName;
    String v = props.getProperty(propertyName);
    if (v != null) {
      value = v;
    }
    return value;
  }

  public synchronized String getLicenseSignatureString() {
    return props.getProperty("signature");

  }

  public synchronized byte[] getLicenseSignature() {
    byte[] sig = null;

    String str = props.getProperty("signature");
    if (str != null) {
      try {
        sig = Hex.decodeHex(str.toCharArray());
      } catch (DecoderException ex) {
        logger.log(Level.SEVERE, null, ex);
      }
    }

    return sig;
  }

  public synchronized boolean isSigned() {
    return getLicenseSignatureString() != null;
  }

  public synchronized int getLicenseVersion() {
    String version = props.getProperty("version");
    if (version != null) {
      return Integer.parseInt(version);
    } else return 1;
  }

  public synchronized Set<String> getHardwareAddresses() {
    return extractHardwareAddresses(props.getProperty("hardwareAddresses", ""));
  }

  public synchronized void setStartDate(Date d) {
    if (isSigned()) {
      throwSignedException();
    }

    props.setProperty("startDate", Long.toString(d.getTime()));
  }

  public synchronized void setExpirationDate(Date d) {
    if (isSigned()) {
      throwSignedException();
    }

    if (d != null) {
      props.setProperty("expirationDate", Long.toString(d.getTime()));
    } else {
      props.remove("expirationDate");
    }
  }

  public synchronized void setFloatingExpirationPeriod(Long period) {
    if (isSigned()) {
      throwSignedException();
    }

    if (period == null) {
      props.remove("floatingExpiration");
    } else if (period < 0) {
      throw new IllegalArgumentException("Period cannot be less than zero");
    } else {
      props.setProperty("floatingExpiration", period.toString());
    }
  }

  public synchronized void addProperty(String propertyName, String value) {
    if (isSigned()) {
      throwSignedException();
    }

    if (value == null) {
      props.remove(prependString + propertyName);
    } else {
      props.setProperty(prependString + propertyName, value);
    }
  }

  public synchronized void addHardwareAddress(byte[] networkInterface) {
    addHardwareAddress(convertInterfaceBytesToString(networkInterface));
  }

  public synchronized void addHardwareAddress(String address) {
    if (isSigned()) {
      throwSignedException();
    }

    if (getLicenseVersion() < 2) {
      throw new UnsupportedOperationException("This license version does not support hardware locking");
    }
    
    address = address.toLowerCase();
    address = address.replaceAll("[^a-f0-9]", "").trim();
    
    // Make sure we don't have a duplicate
    String addresses = props.getProperty("hardwareAddresses");
    if (addresses == null) {
      addresses = "";
    }

    if (!addresses.contains(address)) {
      addresses = addresses + " " + address;
    }

    props.setProperty("hardwareAddresses", addresses.trim());
  }

  public synchronized void removeHardwareAddress(byte[] networkInterface) {
    removeHardwareAddress(convertInterfaceBytesToString(networkInterface));
  }

  public synchronized void removeHardwareAddress(String address) {
    if (isSigned()) {
      throwSignedException();
    }

    address = address.toLowerCase();
    address = address.replaceAll("[^a-f0-9]", "").trim();
    
    Set<String> addresses = this.extractHardwareAddresses(props.getProperty("hardwareAddresses", ""));
    for (Iterator<String> iter = addresses.iterator(); iter.hasNext();) {
      if (iter.next().equals(address)) {
        iter.remove();
      }
    }

    String values = combineHardwareAddresses(addresses);
    props.setProperty("hardwareAddresses", values);
  }

  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj instanceof LicenseImpl) {
      LicenseImpl other = (LicenseImpl) obj;
      Properties properties = other.props;
      if (props.size() != properties.size()) return false;
      for (String key : propertyNames(props)) {
        String value = props.getProperty(key);
        if (!value.equals(properties.getProperty(key))) {
          return false;
        }
      }

      return true;

    } else return false;
  }

  @Override
  public String toString() {
   StringBuilder propString = new StringBuilder("[");
   
   for (String key : propertyNames(props)) {
    propString.append( key + "=" + props.get(key) + ",");
   }
   
   propString.append("]");
   
   return propString.toString();
  }

  @Override
  public int hashCode() {
    int hash = 11;
    for (String key : propertyNames(props)) {
      hash = hash + key.hashCode() + props.getProperty(key).hashCode();
    }
    return hash;
  }

  public License cloneLicense() {
    LicenseImpl l = new LicenseImpl(props);

    // Remove the signature, if any
    l.props.remove("signature");

    // Set creation date to now
    l.setCreationDate(new Date());

    return l;
  }

  /**
   * Get a copy of the underlying Properties object that makes up this
   * license.
   */
  public synchronized Properties getRawProperties() {
    Properties p = new Properties();

    for (String key : propertyNames(props)) {
      p.setProperty(key, props.getProperty(key));
    }

    return p;
  }
}
