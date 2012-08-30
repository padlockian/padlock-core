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

import java.util.Set;

/**
 * The MacAddressProvider interface is intended for those with a need for
 * overriding Padlock's DefaultMacAddressProvider implementation.
 * 
 * @author Jason Nichols (jason@padlocksoftware.net)
 * @since 2.1
 */
public interface MacAddressProvider {
  
  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  /**
   * The meat of the interface, this method should search the local system
   * for all network interfaces and return an unordered Set of MAC address
   * Strings.
   * @return An unordered Set of MAC address Strings.
   */
  public Set<String> getSystemMacAddresses();

  /**
   * A human readable name for this provider.
   */
  public String getName();

  /**
   * A human readable version String for this provider.
   */
  public String getVersion();
}
