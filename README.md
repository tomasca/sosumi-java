sosumi-java
===========

A Java client for Apple's Find My iPhone service. This allows you to programmatically retrieve your devices's current location and push messages (and an optional alarm) to the remote device.

This is essentially a port of Tyler Hall's [PHP Sosumi Class](http://github.com/tylerhall/sosumi).

## Examples


import sosumijava.Sosumi;
import sosumijava.DeviceLocation;

public class SosumiJavaTester {

    public static void main(String[] args) throws Exception {
            Sosumi sosumi = new Sosumi("https://fmipmobile.icloud.com", "APPLE_ID", "PASSWORD");
            sosumi.refresh();
            DeviceLocation loc = sosumi.locateDevice("My iPhone", 60);
            System.out.println("Located: " + loc);
    }
}

