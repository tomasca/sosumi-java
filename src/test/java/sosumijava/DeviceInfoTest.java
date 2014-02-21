/*
 * sosumi-java
 * Copyright 2014 Tomas Carlfalk. All rights reserved.
 */
package sosumijava;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author tomasca
 */
public class DeviceInfoTest {

    
    @Test
    public void testWithLocation() {
        DeviceInfo di = DeviceInfo.fromJson("{\"canWipeAfterLock\":true,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":{\"timeStamp\":1392159139870,\"locationType\":null,\"positionType\":\"GPS\",\"horizontalAccuracy\":10.0,\"locationFinished\":true,\"isInaccurate\":false,\"longitude\":11.06229723730405,\"latitude\":59.45914771365112,\"isOld\":false},\"deviceModel\":\"FifthGen\",\"remoteLock\":null,\"activationLocked\":true,\"locationEnabled\":true,\"rawDeviceModel\":\"iPhone4,1\",\"modelDisplayName\":\"iPhone\",\"lostModeCapable\":true,\"id\":\"a0a0a0a0a0a0a0a0a0a0a0a0aa0a0a0a0\",\"deviceDisplayName\":\"iPhone 4s\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":160,\"name\":\"MyPhone\",\"batteryLevel\":0.1939275,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":true,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":true,\"KEY\":false,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":false,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":true,\"LKL\":false,\"LKM\":false,\"PIN\":false,\"KPD\":false},\"deviceClass\":\"iPhone\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":false,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"Charging\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null}");
        
        assertEquals("batteryLevel", 0.1939275, di.getBatteryLevel(), 0.0);
        assertEquals("batteryStatus", "Charging", di.getBatteryStatus());
        assertEquals("deviceClass", "iPhone", di.getDeviceClass());
        assertEquals("deviceName", "MyPhone", di.getDeviceName());
        assertEquals("isLocationFinished", true, di.isLocationFinished());
        
        assertEquals("timestamp", 1392159139870l, di.getDeviceLocation().getTimestamp());
        assertEquals("horizontalAccuracy", 10.0, di.getDeviceLocation().getHorizontalAccuracy(), 0.0);
        assertEquals("latitude", 59.45914771365112, di.getDeviceLocation().getLatitude(), 0.0);
        assertEquals("longitude", 11.06229723730405, di.getDeviceLocation().getLongitude(), 0.0);
        
    }
}
