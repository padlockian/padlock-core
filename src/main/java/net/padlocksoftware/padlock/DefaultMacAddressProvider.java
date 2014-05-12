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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Jason Nichols (jason@padlocksoftware.net)
 * @since 2.1
 */
public class DefaultMacAddressProvider implements MacAddressProvider {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Logger logger = Logger.getLogger(DefaultMacAddressProvider.class.getName());

  private static final Map<String, byte[]> DEFAULT_VIRTUAL_ADDRESSES = new
          ConcurrentHashMap<String, byte[]>();

  static {

    // VMWare Host
    byte[] vmwareHost = {0x00, 0x50, 0x56};
    DEFAULT_VIRTUAL_ADDRESSES.put("VmWare Host", vmwareHost);

    // VMWare Client
    byte[] vmwareClient = {0x00, 0x0C, 0x29};
    DEFAULT_VIRTUAL_ADDRESSES.put("VmWare Client", vmwareClient);

    // VirtualBox host
    byte[] virtualBoxHost = {0x0a, 0x00, 0x27, 0x00, 0x00, 0x00};
    DEFAULT_VIRTUAL_ADDRESSES.put("VirtualBox Host", virtualBoxHost);
  }
  
  static {
	  // Use the Java API to get the Mac address on Java > 1.5 (< 1.5 is unsupported anyway)
	  String javaVersion = System.getProperty("java.version");
	  FORCE_MACADDR_SHELL = javaVersion != null && javaVersion.startsWith("1.5");
  }
  
  // do we force using the shell command?
  private static final boolean FORCE_MACADDR_SHELL;

  private enum Platform {

    Unknown(null),
    Windows("ipconfig /all"),
    Linux("/sbin/ifconfig"),
    MacOS("ifconfig"),
    Solaris("/usr/sbin/arp"),
    Bsd("ifconfig");
    String command;

    private Platform(String command) {
      this.command = command;
    }
  }

  static final Platform PLATFORM;

  static {
    String os = System.getProperty("os.name").trim().toLowerCase();
    if (os.indexOf("windows") >= 0) {
      PLATFORM = Platform.Windows;
    } else if (os.indexOf("linux") >= 0) {
      PLATFORM = Platform.Linux;
    } else if (os.indexOf("mac os x") >= 0 || os.indexOf("macosx") >= 0) {
      PLATFORM = Platform.MacOS;
    } else if (os.indexOf("solaris") >= 0 || os.indexOf("sunos") >= 0) {
      PLATFORM = Platform.Solaris; // untested
    } else if (os.indexOf("bsd") >= 0) {
      PLATFORM = Platform.Bsd; // untested
    } else {
      PLATFORM = Platform.Unknown;
    }
  }

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final Object lock = new Object();

  private final Map<String, byte[]> virtualAddresses;

  // Flag to permit likely VM addresses as real.
  private volatile boolean permitVMAddresses = false;
  
  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public DefaultMacAddressProvider() {
    virtualAddresses = new ConcurrentHashMap<String, byte[]>();
    for (Entry<String, byte[]> entry : DEFAULT_VIRTUAL_ADDRESSES.entrySet()) {
      byte[] addr = new byte[entry.getValue().length];
      for (int x = 0 ; x < entry.getValue().length ; x++) addr[x] = entry.getValue()[x];
      virtualAddresses.put(entry.getKey(), addr);
    }
    
  }
  
  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements: MacAddressProvider

  public Set<String> getSystemMacAddresses() {
    synchronized(lock) {
      return getMACAddresses();
    }
  }

  public String getName() {
    return this.getClass().getName();
  }

  public String getVersion() {
    return VersionInfo.getVersionString();
  }

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private boolean isVirtualAddress(byte[] address) {

    // Loop through the virtual mac database to determine if this is a physical
    // interface address.
    for (Entry<String, byte[]> entry : virtualAddresses.entrySet()) {
      String name = entry.getKey();
      byte[] data = entry.getValue();

      int matchCount = 0;

      for (int x = 0 ; x < Math.min(data.length, address.length) ; x++) {
        if (data[x] == address[x]) {
          matchCount++;
        }
      }

      if (matchCount == data.length) {
        logger.log(Level.FINE, "Address {0} matches {1}, ignoring", new Object[]{new String(Hex.encodeHex(address)), name});
        return true;
      }
    }

    return false;
  }

  private Set<String> getMACAddresses() {
    Set<String> addresses = new HashSet<String>();

    if (!FORCE_MACADDR_SHELL) {
      try {
        Method method = NetworkInterface.class.getMethod("getHardwareAddress", (Class[]) null);

        Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
        if (ifs != null) {
          while (ifs.hasMoreElements()) {
            NetworkInterface iface = ifs.nextElement();
            byte[] hardware = (byte[]) method.invoke(iface);
            if (hardware != null && hardware.length == 6 && hardware[1] != (byte) 0xff) {
              if (permitVMAddresses || !isVirtualAddress(hardware)) {
                addresses.add(new String(Hex.encodeHex(hardware)));
              }
            }
          }
        }
      } catch (Exception ex) {
        logger.log(Level.FINE, null, ex);
      }
    }

    Set<String> addressesFromShell = useShellToFindAddresses();
    if (addressesFromShell != null) {
      addresses.addAll(addressesFromShell);
    }
    
    return addresses;
  }

  private Set<String> useShellToFindAddresses() {
    logger.fine("Platform: " + PLATFORM);

    if (PLATFORM == Platform.Unknown) {
      logger.fine("Could not determine host platform, no Network Interfaces found.");
      return null;
    }
    BufferedReader reader = null;
    try {
      Process conf = Runtime.getRuntime().exec(PLATFORM.command);
      reader = new BufferedReader(new InputStreamReader(conf.getInputStream()));

      Set<String> macs = new HashSet<String>(4);
      String regex = "[a-f0-9]{2}([:-][a-f0-9]{2}){5}";
      Pattern pattern = Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
      String line = reader.readLine();

      do {
        logger.finer("Found line: " + line);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
          String address = matcher.group(0);
          address = address.trim().replaceAll("[:-]", "");
          logger.fine("Found address: " + address);
          if (address.length() == 11) address = "0" + address;
          if (address.length() == 12) {
            byte[] hex = Hex.decodeHex(address.toCharArray());
            if (permitVMAddresses || !isVirtualAddress(hex)) {
              macs.add(address);
            } else logger.fine("Found blacklisted address: " + address);
          } else logger.fine("Found bogus address: " + address);
        }
        line = reader.readLine();
      } while (line != null);
      return Collections.unmodifiableSet(macs);
    } catch (Throwable t) {
      logger.log(Level.FINE, null, t);
      return Collections.emptySet();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          logger.log(Level.FINE, null, e);
        }
      }
    }
  }

  //---------------------------- Property Methods -----------------------------

  /**
   * Enable or disable the use of known VM MAC addresses.
   * 
   * @param permit If true, addresses that match the VM regexes will be
   * counted as a hardware MAC.
   */
  public void setPermitVMAddresses(boolean permit) {
    permitVMAddresses = permit;
  }

  /**
   * Returns the status of the permitVMAddressesflag.
   * @return True if VM addresses are permitted as valid hardware addresses,
   * or false if not.
   */
  public boolean permitVMAddresses() {
    return permitVMAddresses;
  }

  /**
   * Get the underlying Map used to store the VM address table.  The key of the
   * Map is a string description of the value (aka "VirtualBox default").  The
   * byte array is a variable length set of values that match an address group.
   * For example {0x00, 0x50, 0x56} is the default array value for a VMWare
   * host MAC address.
   * @return
   */
  public Map<String, byte[]> getVirtualAddressesMap() {
    return virtualAddresses;
  }
}
