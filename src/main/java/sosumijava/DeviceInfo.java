/*
 * sosumi-java
 * Copyright 2014 Tomas Carlfalk. All rights reserved.
 */
package sosumijava;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author tomasca
 */
public class DeviceInfo {

    public static DeviceInfo fromJson(String json) {

        JsonElement je = new JsonParser().parse(json);
        JsonObject diObject = je.getAsJsonObject();

        DeviceInfo di = new DeviceInfo();
        di.deviceName = diObject.get("name").getAsString();
        di.deviceClass = diObject.get("deviceClass").getAsString();
        di.batteryLevel = diObject.get("batteryLevel").getAsDouble();
        di.batteryStatus = diObject.get("batteryStatus").getAsString();
        JsonElement loc = diObject.get("location");
        if (loc != null && !loc.isJsonNull()) {
            di.deviceLocation = DeviceLocation.fromJson(loc.toString());
        }
        return di;
    }
    private String deviceName;
    private String deviceClass;
    private double batteryLevel;
    private String batteryStatus;
    private DeviceLocation deviceLocation;

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceClass() {
        return deviceClass;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public DeviceLocation getDeviceLocation() {
        return deviceLocation;
    }

    public boolean isLocationFinished() {
        return deviceLocation != null ? deviceLocation.isLocationFinished() : false;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" + "deviceName=" + deviceName + ", deviceClass=" + deviceClass + ", batteryLevel=" + batteryLevel + ", batteryStatus=" + batteryStatus + ", deviceLocation=" + deviceLocation + '}';
    }
}
