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

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

/**
 * <p>Instances of the License interface are the primary means of customizing
 * licenses.  These can be created through the LicenseFactory class and
 * imported/exported through the LicenseIO class.</p>
 *
 * <p>All licenses must have a creation date and a start date.  If neither are
 * specified at the time of license instantation, the current date will be
 * used for both.  Licenses may also be given an optional expiration date. Licences may also be set
 * with a floating expiration date, which is the
 * period of time a license is valid after the first time it's run through the
 * validator.  This is the easiest method of creating a time limited day demo.</p>
 *
 * <p>Licenses can be given any number of customizable properties.  The contents
 * of these properties are of no concern to the validator, they exist strictly for
 * customer defined plugins and the customer application.  Padlock does guarantee
 * the validity of these properties, however.  If any property key or value is changed,
 * added, or removed the license will fail to validate.</p>
 * 
 * <p>Licenses are freely modifiable until they've been signed with a private key,
 * at which point they act (and should be treated as) an immutable class.  Any
 * attempt to modify a signed license will cause an exception to be thrown.  See
 * the various method docs for details.</p>
 * 
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public interface License {

  /**
   * Return a new License instance based off the current instance.  The new license is identical
   * to the original, with the exceptions that the new license is not signed, and that the 
   * creation date will be the current system time.  
   * @return A copy of the current license instance, minus the signature.
   */
  public License cloneLicense();

  ////////////////////////////////////////////////////////////////////////////
  // Get Methods
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Get the license creation date.  This is may differ from the license start
   * date.
   * @return The creation date of the license.
   */
  public Date getCreationDate();

  /**
   * Get the license expiration date.  This is the date at which the license is
   * no longer valid.  This is not related to the floating expiration period, but
   * may be used in conjunction with it.
   * @return The expiration date of the license, or null if the license has no
   * expiration date.
   */
  public Date getExpirationDate();

  /**
   * Returns the start date of the license validity period.  
   * @return The license start date.
   */
  public Date getStartDate();

  /**
   * Returns the period of time remaining before the floating expiration period
   * is reached.  This is distinct from a hard coded expiration date in that the
   * float period doesn't start until after the first time a license is
   * validated.
   * 
   * @return The floating expiration period for the license, or null if none
   * has been set.
   */
  public Long getFloatingExpirationPeriod();

  /**
   * Get all custom properties defined for this license.
   * @return All custom properties, or an empty Properties object if none
   * have been added.
   */
  public Properties getProperties();

  /**
   * Get a specific custom property for this license.
   * @param propertyName The name of the property to return.
   * @return The value for the given property, or null if it has not been set.
   */
  public String getProperty(String propertyName);

  /**
   * Get a specific custom property for this license.
   * @param propertyName The name of the property to return.
   * @param defaultValue The default value to return if the property does not exist
   * @return Either the property's value in the license, or the default value.
   */
  public String getProperty(String propertyName, String defaultValue);

  /**
   * Gets the license signature string.  The signature string is the cryptographic
   * signature resulting from the DSA signing process. When a license is
   * validated, this signature string should match the one computed by the
   * Validator.
   *
   * @return The license signature String, or null if the license has not yet
   * been signed.
   */
  public String getLicenseSignatureString();

  /**
   * Returns a byte coded version of getLicenseSignatureString()
   *
   * @return The license signature, or null if the license has not yet been
   * signed.
   */
  public byte[] getLicenseSignature();

  /**
   * Returns the license signature status
   * @return True if the license has been signed, or false otherwise.
   */
  public boolean isSigned();

  /**
   * Returns the license version.  The 1.x series of Padlock is known as version 1,
   * while the 2.x branch is known as version 2.
   *
   * @return The license version number.
   */
  public int getLicenseVersion();

  /**
   * Returns the set of MAC addresses that this license is locked to.
   * @return The address set, or an empty set if this license isn't locked to any
   * addresses.
   */
  public Set<String> getHardwareAddresses();

  ////////////////////////////////////////////////////////////////////////////
  // Set Methods
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Set the start date for this license.  This can be any date in the past,
   * present or future.  The only restriction is that the start date must not be
   * after the license expiration date, if one is set.
   *
   * @param d The license start date.
   */
  public void setStartDate(Date d);

  /**
   * Set the expiration date for this license.  This can be any date in the past,
   * present or future.  The only restriction is that the expiration date must not be
   * after the license start date.  
   *
   * @param d The license start date, or null to unset the expiration date.
   */
  public void setExpirationDate(Date d);

  /**
   * Sets the licenses floating expiration period, which is the number of milliseconds
   * after the initial validation when the license will expire.
   *
   * @param period The length of the license validity period, in milliseconds.
   */
  public void setFloatingExpirationPeriod(Long period);

  /**
   * Add a custom property to the license.  This can be retreived using
   * getProperty().
   *
   * @param propertyName The unique property name;
   * @param value The property value, or null to remove this property from the license.
   */
  public void addProperty(String propertyName, String value);

  /**
   * Add the given byte array to the list of MAC addresses this license it
   * restricted to.  The byte array is expected to be 6 bytes long.
   * @param address The byte values of the hardware address to add,
   */
  public void addHardwareAddress(byte[] address);

  /**
   * Same as {@link #addHardwareAddress(byte[]) addHardwareAddress}, only
   * using a string encoded address with or without colons.
   *
   * @param address The mac address in the form of <i>123456789abc</i> or <i>12:34:56:78:90:ab</i>.
   */
  public void addHardwareAddress(String address);

  /**
   * Remove the given hardware address from the license hardware list.
   * @param networkInterface The byte encoded interface to remove.
   */
  public void removeHardwareAddress(byte[] networkInterface);

  /**
   * Remove the given hardware address from the license hardware list.
   * @param address The hardware address to remove, in String form.
   */
  public void removeHardwareAddress(String address);
}
