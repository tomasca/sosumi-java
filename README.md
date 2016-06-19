sosumi-java
===========

A Java client for Apple's Find My iPhone service. This allows you to programmatically retrieve your devices's current location and push messages (and an optional alarm) to the remote device.

This is essentially a port of Tyler Hall's [PHP Sosumi Class](http://github.com/tylerhall/sosumi).

## Examples


    public static void main(String[] args) throws Exception {
            Sosumi sosumi = new Sosumi("https://fmipmobile.icloud.com", "APPLE_ID", "PASSWORD");
            
            // Listing my devices
            Collection<DeviceInfo> devs = sosumi.getDevices();
			for ( DeviceInfo info : devs ) {
				System.out.println(info.toString());
			}
            
            // Getting a device location
            DeviceLocation loc = sosumi.locateDevice("My Device Name", 60);
            System.out.println("Located: " + loc);
            
            // Sending a message
            sosumi.sendMessage( "My Device Name", "Loreipsum", "my subject", true, true );
            
            // Playing a sound with a title
            sosumi.playSound( "My Device Name", "Please call me afap!" );
            
            // Lock a device
            sosumi.lock( "My Device Name" );
            
            // Start Lost Mode
            sosumi.startLostMode( "My Device Name", "This iPhone has been lost. Please call me.", "(XX) XXXX-XXXXX", "1234", true, true );
            
            // Stop Lost Mode
            sosumi.startLostMode( "My Device Name" );
    }

