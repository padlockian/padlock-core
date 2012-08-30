/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.padlocksoftware.padlock.license;

import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import junit.framework.TestCase;
import net.padlocksoftware.padlock.MacAddresses;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Jason
 */
public class LicenseImplTest extends TestCase {
    
    public LicenseImplTest(String testName) {
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
     * Test of getCreationDate method, of class LicenseImpl.
     */
    public void testGetCreationDate() {
        System.out.println("getCreationDate");
        LicenseImpl instance = new LicenseImpl();
        Date result = instance.getCreationDate();
        assertNotNull(result);
    }

    /**
     * Test of getExpirationDate method, of class LicenseImpl.
     */
    public void testGetExpirationDate() {
        System.out.println("getExpirationDate");
        LicenseImpl instance = new LicenseImpl();
        Date expResult = null;
        Date result = instance.getExpirationDate();
        assertEquals(expResult, result);

        expResult = new Date();
        instance.setExpirationDate(expResult);
        result = instance.getExpirationDate();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStartDate method, of class LicenseImpl.
     */
    public void testGetStartDate() {
        System.out.println("getStartDate");
        Date startDate = new Date();
        LicenseImpl instance = new LicenseImpl(startDate);
        
        Date result = instance.getStartDate();
        assertEquals(startDate, result);
    }

    /**
     * Test of getFloatingExpirationPeriod method, of class LicenseImpl.
     */
    public void testGetFloatingExpirationPeriod() {
        System.out.println("getFloatingExpirationPeriod");
        LicenseImpl instance = new LicenseImpl();
        Long expResult = null;
        Long result = instance.getFloatingExpirationPeriod();
        assertEquals(expResult, result);

        expResult = 10000L;
        instance.setFloatingExpirationPeriod(expResult);
        result = instance.getFloatingExpirationPeriod();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getProperties method, of class LicenseImpl.
     */
    public void testGetProperties() {
        System.out.println("getProperties");
        LicenseImpl instance = new LicenseImpl();
        Properties result = instance.getProperties();
        assertEquals(0, result.size());

        Map<String,String> propertyMap = new HashMap<String,String>();
        propertyMap.put("property1", "value1");
        propertyMap.put("property2", "value2");
        propertyMap.put("property3", "value3");

        for(String key : propertyMap.keySet()) {
            instance.addProperty(key, propertyMap.get(key));
        }

        result = instance.getProperties();
        assertEquals(propertyMap.size(), result.size());

        for (String property : LicenseImpl.propertyNames(result)) {
            String expectedValue = propertyMap.get(property);
            String resultValue = result.getProperty(property);
            assertEquals(expectedValue, resultValue);
        }
    }

    /**
     * Test of getProperty method, of class LicenseImpl.
     */
    public void testGetProperty_String() {
        System.out.println("getProperty");

        LicenseImpl instance = new LicenseImpl();
        Map<String,String> propertyMap = new HashMap<String,String>();
        propertyMap.put("property1", "value1");
        propertyMap.put("property2", "value2");
        propertyMap.put("property3", "value3");

        for(String key : propertyMap.keySet()) {
            instance.addProperty(key, propertyMap.get(key));
        }

        for(String key : propertyMap.keySet()) {
            String expectedValue = propertyMap.get(key);
            String resultValue = instance.getProperty(key);
            assertEquals(expectedValue, resultValue);
        }    }

    /**
     * Test of getProperty method, of class LicenseImpl.
     */
    public void testGetProperty_String_String() {
        System.out.println("getProperty");
        LicenseImpl instance = new LicenseImpl();
        String expectedResult = "defaultResult";
        String result = instance.getProperty("asfdb", expectedResult);
        assertEquals(expectedResult, result);
    }

    /**
     * Test of getLicenseSignatureString method, of class LicenseImpl.
     */
    public void testGetLicenseSignatureString() {
        System.out.println("getLicenseSignatureString");
        LicenseImpl instance = new LicenseImpl();
        String result = instance.getLicenseSignatureString();
        assertNull(result);
    }

    /**
     * Test of getLicenseSignature method, of class LicenseImpl.
     */
    public void testGetLicenseSignature() {
        System.out.println("getLicenseSignature");
        LicenseImpl instance = new LicenseImpl();
        byte[] result = instance.getLicenseSignature();
        assertNull(result);
    }

    /**
     * Test of isSigned method, of class LicenseImpl.
     */
    public void testIsSigned() {
        System.out.println("isSigned");
        LicenseImpl instance = new LicenseImpl();
        boolean expResult = false;
        boolean result = instance.isSigned();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLicenseVersion method, of class LicenseImpl.
     */
    public void testGetLicenseVersion() {
        System.out.println("getLicenseVersion");
        LicenseImpl instance = new LicenseImpl();
        int expResult = 2;
        int result = instance.getLicenseVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHardwareAddresses method, of class LicenseImpl.
     */
    public void testGetHardwareAddresses() {
        System.out.println("getHardwareAddresses");
        LicenseImpl instance = new LicenseImpl();
        Set<String> testSet = new HashSet<String>();
        testSet.add("001234567891");
        testSet.add("009876543210");
        testSet.add("345678901234");

        for(String str : testSet) {
            instance.addHardwareAddress(str);
        }

        Set<String> result = instance.getHardwareAddresses();

        assertEquals(testSet.size(), result.size());
        for (String testString : result) {
          assertTrue(result.contains(testString));
        }
    }

    /**
     * Test of setStartDate method, of class LicenseImpl.
     */
    public void testSetStartDate() {
        System.out.println("setStartDate");
        Date d = new Date(System.currentTimeMillis() - 1000000L);
        LicenseImpl instance = new LicenseImpl();
        instance.setStartDate(d);
        assertEquals(d.getTime(), instance.getStartDate().getTime());
    }

    /**
     * Test of setExpirationDate method, of class LicenseImpl.
     */
    public void testSetExpirationDate() {
        System.out.println("setExpirationDate");
        Date d = new Date(System.currentTimeMillis() + 50000L);
        LicenseImpl instance = new LicenseImpl();
        instance.setExpirationDate(d);
        assertEquals(d.getTime(), instance.getExpirationDate().getTime());
    }

    /**
     * Test of setFloatingExpirationPeriod method, of class LicenseImpl.
     */
    public void testSetFloatingExpirationDate() {
        System.out.println("setFloatingExpirationDate");
        Long period = 1000000L;
        LicenseImpl instance = new LicenseImpl();
        instance.setFloatingExpirationPeriod(period);

        assertEquals(instance.getFloatingExpirationPeriod(), period);
    }

    public void testAddHardwareAddress_String() throws Exception {
        System.out.println("addHardwareAddress - String");
        LicenseImpl instance = new LicenseImpl();

        Set<String> addresses = MacAddresses.getSystemMacAddresses();
        for (String address : addresses) {
          instance.addHardwareAddress(address);
        }

        Set<String> interfaceStrings = instance.getHardwareAddresses();
        assertEquals(addresses.size(), interfaceStrings.size());

        for (String address : addresses) {
            assertTrue(interfaceStrings.contains(address));
        }
    }

    /**
     * Test of addHardwareAddress method, of class LicenseImpl.
     */
//    public void testAddHardwareAddress_byteArr() throws Exception {
//        System.out.println("addHardwareAddress - byte[]");
//        LicenseImpl instance = new LicenseImpl();
//
//        Set<NetworkInterface> interfaceSet = new HashSet<NetworkInterface>();
//
//        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//        while (interfaces.hasMoreElements()) {
//            NetworkInterface interfce = interfaces.nextElement();
//            if (!interfce.isLoopback() && !interfce.isVirtual()
//                    && !interfce.isPointToPoint() && interfce.getHardwareAddress() != null) {
//                interfaceSet.add(interfce);
//                instance.addHardwareAddress(interfce.getHardwareAddress());
//            }
//        }
//        Set<String> interfaceStrings = instance.getHardwareAddresses();
//        assertEquals(interfaceSet.size(), interfaceStrings.size());
//
//        for (NetworkInterface interfce : interfaceSet) {
//            String interfaceString = new String(Hex.encodeHex(interfce.getHardwareAddress()));
//            assertTrue(interfaceStrings.contains(interfaceString));
//        }
//    }

//    /**
//     * Test of removeHardwareAddress method, of class LicenseImpl.
//     */
//    public void testRemoveHardwareAddress_byteArr() throws Exception {
//        System.out.println("removeHardwareAddress - byte[]");
//        LicenseImpl instance = new LicenseImpl();
//
//        Set<NetworkInterface> interfaceSet = new HashSet<NetworkInterface>();
//        byte[] removedAddress = null;
//        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//        while (interfaces.hasMoreElements()) {
//            NetworkInterface interfce = interfaces.nextElement();
//            if (!interfce.isLoopback() && !interfce.isVirtual()
//                    && !interfce.isPointToPoint() && interfce.getHardwareAddress() != null) {
//                interfaceSet.add(interfce);
//                instance.addHardwareAddress(interfce.getHardwareAddress());
//                //System.out.println("Adding interface:" + interfce);
//                byte[] mac = interfce.getHardwareAddress();
//                if (removedAddress == null) {
//                    removedAddress = mac;
//                }
//            }
//        }
//
//        instance.removeHardwareAddress(removedAddress);
//
//        assertTrue(interfaceSet.size() - 1 == instance.getHardwareAddresses().size());
//        String removedString = new String(Hex.encodeHex(removedAddress));
//        assertFalse(instance.getHardwareAddresses().contains(removedString));
//    }

}
