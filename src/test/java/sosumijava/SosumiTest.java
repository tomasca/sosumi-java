/*
 * sosumi-java
 * Copyright 2014 Tomas Carlfalk. All rights reserved.
 */
package sosumijava;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author tomasca
 */
public class SosumiTest {

    private LocalTestServer server = null;
    private HttpRequestHandler handler;
    private Sosumi impl;
    private String primaryServiceAddress;
    private String secondaryServiceAddress;
    private String scope = String.valueOf(System.currentTimeMillis());

    @Before
    public void setUp() throws Exception {

        handler = mock(HttpRequestHandler.class);

        server = new LocalTestServer(null, null);
        server.register("/*", handler);
        server.start();

        // report how to access the server
        System.out.println("LocalTestServer available at " + server.getServiceAddress());
        primaryServiceAddress = server.getServiceAddress().getHostName() + ":" + server.getServiceAddress().getPort();
        secondaryServiceAddress = server.getServiceAddress().getAddress().getHostAddress() + ":" + server.getServiceAddress().getPort();

        impl = new Sosumi("http://" + primaryServiceAddress, "user", "pass");

    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testLocateDevice() throws Exception {
        doAnswer(new Answer() {
            // First response is a 330 redirect 
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                HttpRequest req = (HttpRequest) args[0];
                HttpResponse resp = (HttpResponse) args[1];
                assertEquals("should use new hostname", primaryServiceAddress, req.getFirstHeader("Host").getValue());
                assertTrue("should be initClient command", req.getRequestLine().getUri().endsWith("/initClient"));
                resp.addHeader("X-Responding-Instance", "fmipservice-12100402-st13p21ic-ztav122446");
                resp.addHeader("X-Request-UUID", "3-4db3-481e-afc9-69e7a5c5c583-ini");
                resp.addHeader("X-Responding-Server", "st13p21ic-ztav122446_002");
                resp.addHeader("X-Apple-MMe-Host", secondaryServiceAddress);
                resp.addHeader("X-Responding-Partition", "p21");
                resp.addHeader("X-Apple-MMe-Scope", scope);
                resp.setStatusCode(330);
                return null;
            }
        }).doAnswer(new Answer() {
            // Response is a 200 OK containing a json payload - without location
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                HttpRequest req = (HttpRequest) args[0];
                HttpResponse resp = (HttpResponse) args[1];
                assertEquals("should use new hostname", secondaryServiceAddress, req.getFirstHeader("Host").getValue());
                assertTrue("should be initClient command", req.getRequestLine().getUri().endsWith("/initClient"));
                resp.setEntity(new StringEntity(exampleJsonResponse));
                resp.setStatusCode(200);
                return null;
            }
        }).doAnswer(new Answer() {
            // Response is a 200 OK containing a json payload - without location
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                HttpRequest req = (HttpRequest) args[0];
                HttpResponse resp = (HttpResponse) args[1];
                assertEquals("should use new hostname", secondaryServiceAddress, req.getFirstHeader("Host").getValue());
                assertTrue("should be initClient command", req.getRequestLine().getUri().endsWith("/initClient"));
                resp.setEntity(new StringEntity(exampleJsonResponseWithLocationForMyiPad));
                resp.setStatusCode(200);
                return null;
            }
        }).when(handler).handle((HttpRequest) anyObject(), (HttpResponse) anyObject(), (HttpContext) anyObject());

        impl.setLocateRefreshInterval(200L);
        DeviceLocation loc = impl.locateDevice("MyiPad", 10);
        assertEquals("lat", 55.18714771365112, loc.getLatitude(), 0.0);
        assertEquals("lon", 18.01929723730405, loc.getLongitude(), 0.0);
    }

    @Test
    public void testSendMessage() throws Exception {
        doAnswer(new Answer() {
            // First response is a 330 redirect 
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                HttpRequest req = (HttpRequest) args[0];
                HttpResponse resp = (HttpResponse) args[1];
                assertEquals("should use new hostname", primaryServiceAddress, req.getFirstHeader("Host").getValue());
                assertTrue("should be initClient command", req.getRequestLine().getUri().endsWith("/initClient"));
                resp.addHeader("X-Responding-Instance", "fmipservice-12100402-st13p21ic-ztav122446");
                resp.addHeader("X-Request-UUID", "3-4db3-481e-afc9-69e7a5c5c583-ini");
                resp.addHeader("X-Responding-Server", "st13p21ic-ztav122446_002");
                resp.addHeader("X-Apple-MMe-Host", secondaryServiceAddress);
                resp.addHeader("X-Responding-Partition", "p21");
                resp.addHeader("X-Apple-MMe-Scope", scope);
                resp.setStatusCode(330);
                return null;
            }
        }).doAnswer(new Answer() {
            // Response is a 200 OK containing a json payload - without location
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                HttpRequest req = (HttpRequest) args[0];
                HttpResponse resp = (HttpResponse) args[1];
                assertEquals("should use new hostname", secondaryServiceAddress, req.getFirstHeader("Host").getValue());
                assertTrue("should be initClient command", req.getRequestLine().getUri().endsWith("/initClient"));
                resp.setEntity(new StringEntity(exampleJsonResponse));
                resp.setStatusCode(200);
                return null;
            }
        }).doAnswer(new Answer() {
            // Response is a 200 OK containing a json payload - without location
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                HttpRequest req = (HttpRequest) args[0];
                HttpResponse resp = (HttpResponse) args[1];
                assertEquals("should use new hostname", secondaryServiceAddress, req.getFirstHeader("Host").getValue());
                assertTrue("should be sendMessage command", req.getRequestLine().getUri().endsWith("/sendMessage"));
                resp.setEntity(new StringEntity(exampleJsonResponse));
                resp.setStatusCode(200);
                return null;
            }
        }).when(handler).handle((HttpRequest) anyObject(), (HttpResponse) anyObject(), (HttpContext) anyObject());

        impl.sendMessage("MyiPad", "A text message", "A subject", false);
        
    }

    
    private static String exampleJsonResponse = "{\"statusCode\":\"200\",\"content\":[{\"canWipeAfterLock\":true,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":{\"timeStamp\":1392159139870,\"locationType\":null,\"positionType\":\"GPS\",\"horizontalAccuracy\":10.0,\"locationFinished\":true,\"isInaccurate\":false,\"longitude\":12.01929723730405,\"latitude\":60.18714771365112,\"isOld\":false},\"deviceModel\":\"FifthGen\",\"remoteLock\":null,\"activationLocked\":true,\"locationEnabled\":true,\"rawDeviceModel\":\"iPhone4,1\",\"modelDisplayName\":\"iPhone\",\"lostModeCapable\":true,\"id\":\"KOODsac0121e/ASDcascasSDAS210121\",\"deviceDisplayName\":\"iPhone 4s\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":160,\"name\":\"MyiPhone\",\"batteryLevel\":0.1939275,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":true,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":true,\"KEY\":false,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":false,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":true,\"LKL\":false,\"LKM\":false,\"PIN\":false,\"KPD\":false},\"deviceClass\":\"iPhone\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":false,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"Charging\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null},{\"canWipeAfterLock\":true,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":null,\"deviceModel\":\"ThirdGen-4G\",\"remoteLock\":null,\"activationLocked\":true,\"locationEnabled\":true,\"rawDeviceModel\":\"iPad3,3\",\"modelDisplayName\":\"iPad\",\"lostModeCapable\":true,\"id\":\"+dwqdij29823WEF021ds/ASDKASDJcasdasd2\",\"deviceDisplayName\":\"iPad\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":160,\"name\":\"MyiPad\",\"batteryLevel\":0.0,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":true,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":true,\"KEY\":false,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":false,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":true,\"LKL\":true,\"LKM\":false,\"PIN\":false,\"KPD\":false},\"deviceClass\":\"iPad\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":false,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"Unknown\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null},{\"canWipeAfterLock\":false,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":null,\"deviceModel\":\"iMac12_1\",\"remoteLock\":null,\"activationLocked\":false,\"locationEnabled\":true,\"rawDeviceModel\":\"iMac12,1\",\"modelDisplayName\":\"iMac\",\"lostModeCapable\":false,\"id\":\"FDSFwqeqwe012/djasodij2131easASD/dqwid1\",\"deviceDisplayName\":\"iMac 21.5\\\"\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":500,\"name\":\"MyiMac\",\"batteryLevel\":0.0,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":false,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":false,\"KEY\":true,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":true,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":false,\"LKL\":true,\"LKM\":true,\"PIN\":true,\"KPD\":true},\"deviceClass\":\"iMac\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":true,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"Unknown\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null},{\"canWipeAfterLock\":true,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":null,\"deviceModel\":\"iPad2_5\",\"remoteLock\":null,\"activationLocked\":true,\"locationEnabled\":true,\"rawDeviceModel\":\"iPad2,5\",\"modelDisplayName\":\"iPad\",\"lostModeCapable\":true,\"id\":\"dbdbdbd/DSFOJ14/wqdojsd982e321DAWD124\",\"deviceDisplayName\":\"iPad mini\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":160,\"name\":\"MyiPadMini\",\"batteryLevel\":0.7233946,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":true,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":true,\"KEY\":true,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":false,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":true,\"LKL\":true,\"LKM\":false,\"PIN\":false,\"KPD\":true},\"deviceClass\":\"iPad\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":false,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"NotCharging\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null}],\"userInfo\":{\"lastName\":\"Bar\",\"firstName\":\"Foo\"},\"userPreferences\":{\"touchPrefs\":{},\"activationProhibitedDevices\":{\"0000000000000000000000000000000000000000000000000\":\"111111111111111111\"},\"activationUpgradeEmailSent\":true,\"lastUpdatedTime\":1384881308761,\"builder\":null},\"serverContext\":{\"minTrackLocThresholdInMts\":100,\"prefsUpdateTime\":1384881308761,\"maxDeviceLoadTime\":60000,\"authToken\":\"ABABABABABABAB\",\"classicUser\":false,\"sessionLifespan\":900000,\"serverTimestamp\":1392159219259,\"enableMapStats\":true,\"imageBaseUrl\":\"https://statici.icloud.com\",\"deviceLoadStatus\":\"200\",\"preferredLanguage\":\"sv-se\",\"clientId\":\"0101010101010101010101011\",\"lastSessionExtensionTime\":null,\"trackInfoCacheDurationInSecs\":86400,\"isHSA\":false,\"timezone\":{\"tzCurrentName\":\"Pacific Standard Time\",\"previousTransition\":1383469199999,\"previousOffset\":-25200000,\"currentOffset\":-28800000,\"tzName\":\"America/Los_Angeles\"},\"callbackIntervalInMS\":2000,\"cloudUser\":true,\"validRegion\":true,\"maxLocatingTime\":90000,\"prsId\":121212121212,\"macCount\":0}}";
    private static String exampleJsonResponseWithLocationForMyiPad = "{\"statusCode\":\"200\",\"content\":[{\"canWipeAfterLock\":true,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":{\"timeStamp\":1392159139870,\"locationType\":null,\"positionType\":\"GPS\",\"horizontalAccuracy\":10.0,\"locationFinished\":true,\"isInaccurate\":false,\"longitude\":12.01929723730405,\"latitude\":60.18714771365112,\"isOld\":false},\"deviceModel\":\"FifthGen\",\"remoteLock\":null,\"activationLocked\":true,\"locationEnabled\":true,\"rawDeviceModel\":\"iPhone4,1\",\"modelDisplayName\":\"iPhone\",\"lostModeCapable\":true,\"id\":\"KOODsac0121e/ASDcascasSDAS210121\",\"deviceDisplayName\":\"iPhone 4s\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":160,\"name\":\"MyiPhone\",\"batteryLevel\":0.1939275,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":true,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":true,\"KEY\":false,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":false,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":true,\"LKL\":false,\"LKM\":false,\"PIN\":false,\"KPD\":false},\"deviceClass\":\"iPhone\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":false,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"Charging\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null},{\"canWipeAfterLock\":true,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":{\"timeStamp\":1392159139870,\"locationType\":null,\"positionType\":\"GPS\",\"horizontalAccuracy\":10.0,\"locationFinished\":true,\"isInaccurate\":false,\"longitude\":18.01929723730405,\"latitude\":55.18714771365112,\"isOld\":false},\"deviceModel\":\"ThirdGen-4G\",\"remoteLock\":null,\"activationLocked\":true,\"locationEnabled\":true,\"rawDeviceModel\":\"iPad3,3\",\"modelDisplayName\":\"iPad\",\"lostModeCapable\":true,\"id\":\"+dwqdij29823WEF021ds/ASDKASDJcasdasd2\",\"deviceDisplayName\":\"iPad\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":160,\"name\":\"MyiPad\",\"batteryLevel\":0.0,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":true,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":true,\"KEY\":false,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":false,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":true,\"LKL\":true,\"LKM\":false,\"PIN\":false,\"KPD\":false},\"deviceClass\":\"iPad\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":false,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"Unknown\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null},{\"canWipeAfterLock\":false,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":null,\"deviceModel\":\"iMac12_1\",\"remoteLock\":null,\"activationLocked\":false,\"locationEnabled\":true,\"rawDeviceModel\":\"iMac12,1\",\"modelDisplayName\":\"iMac\",\"lostModeCapable\":false,\"id\":\"FDSFwqeqwe012/djasodij2131easASD/dqwid1\",\"deviceDisplayName\":\"iMac 21.5\\\"\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":500,\"name\":\"MyiMac\",\"batteryLevel\":0.0,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":false,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":false,\"KEY\":true,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":true,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":false,\"LKL\":true,\"LKM\":true,\"PIN\":true,\"KPD\":true},\"deviceClass\":\"iMac\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":true,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"Unknown\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null},{\"canWipeAfterLock\":true,\"remoteWipe\":null,\"locFoundEnabled\":false,\"location\":null,\"deviceModel\":\"iPad2_5\",\"remoteLock\":null,\"activationLocked\":true,\"locationEnabled\":true,\"rawDeviceModel\":\"iPad2,5\",\"modelDisplayName\":\"iPad\",\"lostModeCapable\":true,\"id\":\"dbdbdbd/DSFOJ14/wqdojsd982e321DAWD124\",\"deviceDisplayName\":\"iPad mini\",\"darkWake\":false,\"locationCapable\":true,\"maxMsgChar\":160,\"name\":\"MyiPadMini\",\"batteryLevel\":0.7233946,\"features\":{\"CLT\":false,\"CWP\":false,\"WMG\":true,\"XRM\":false,\"CLK\":false,\"SND\":true,\"LST\":true,\"KEY\":true,\"WIP\":true,\"LOC\":true,\"LLC\":false,\"MSG\":true,\"LMG\":false,\"LCK\":true,\"REM\":false,\"SVP\":false,\"TEU\":true,\"LKL\":true,\"LKM\":false,\"PIN\":false,\"KPD\":true},\"deviceClass\":\"iPad\",\"wipeInProgress\":false,\"passcodeLength\":4,\"mesg\":null,\"isMac\":false,\"snd\":null,\"isLocating\":true,\"trackingInfo\":null,\"deviceColor\":null,\"batteryStatus\":\"NotCharging\",\"deviceStatus\":\"203\",\"wipedTimestamp\":null,\"lockedTimestamp\":null,\"msg\":null,\"lostTimestamp\":\"\",\"lostModeEnabled\":false,\"thisDevice\":false,\"lostDevice\":null}],\"userInfo\":{\"lastName\":\"Bar\",\"firstName\":\"Foo\"},\"userPreferences\":{\"touchPrefs\":{},\"activationProhibitedDevices\":{\"0000000000000000000000000000000000000000000000000\":\"111111111111111111\"},\"activationUpgradeEmailSent\":true,\"lastUpdatedTime\":1384881308761,\"builder\":null},\"serverContext\":{\"minTrackLocThresholdInMts\":100,\"prefsUpdateTime\":1384881308761,\"maxDeviceLoadTime\":60000,\"authToken\":\"ABABABABABABAB\",\"classicUser\":false,\"sessionLifespan\":900000,\"serverTimestamp\":1392159219259,\"enableMapStats\":true,\"imageBaseUrl\":\"https://statici.icloud.com\",\"deviceLoadStatus\":\"200\",\"preferredLanguage\":\"sv-se\",\"clientId\":\"0101010101010101010101011\",\"lastSessionExtensionTime\":null,\"trackInfoCacheDurationInSecs\":86400,\"isHSA\":false,\"timezone\":{\"tzCurrentName\":\"Pacific Standard Time\",\"previousTransition\":1383469199999,\"previousOffset\":-25200000,\"currentOffset\":-28800000,\"tzName\":\"America/Los_Angeles\"},\"callbackIntervalInMS\":2000,\"cloudUser\":true,\"validRegion\":true,\"maxLocatingTime\":90000,\"prsId\":121212121212,\"macCount\":0}}";
}
