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
public class DeviceLocation {

    static DeviceLocation fromJson(String json) {

        JsonElement je = new JsonParser().parse(json);
        JsonObject jobj = je.getAsJsonObject();

        DeviceLocation dl = new DeviceLocation();
        dl.latitude = jobj.get("latitude").getAsDouble();
        dl.longitude = jobj.get("longitude").getAsDouble();
        dl.timestamp = jobj.get("timeStamp").getAsLong();
        dl.horizontalAccuracy = jobj.get("horizontalAccuracy").getAsFloat();
        dl.locationFinished = jobj.get("locationFinished").getAsBoolean();

        return dl;
    }
    private double latitude;
    private double longitude;
    private float horizontalAccuracy;
    private long timestamp;
    private boolean locationFinished;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isLocationFinished() {
        return locationFinished;
    }

    @Override
    public String toString() {
        return "DeviceLocation{" + "latitude=" + latitude + ", longitude=" + longitude + ", horizontalAccuracy=" + horizontalAccuracy + ", timestamp=" + timestamp + ", locationFinished=" + locationFinished + '}';
    }
}
