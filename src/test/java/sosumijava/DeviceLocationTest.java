package sosumijava;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author tomasca
 */
public class DeviceLocationTest {

    @Test
    public void testWithFullObject() {
        DeviceLocation dl = DeviceLocation.fromJson("{\"timeStamp\":1392159139870,\"locationType\":null,\"positionType\":\"GPS\",\"horizontalAccuracy\":10.0,\"locationFinished\":true,\"isInaccurate\":false,\"longitude\":12.09229723730405,\"latitude\":63.11914771365112,\"isOld\":false}");

        assertEquals("timestamp", 1392159139870l, dl.getTimestamp());
        assertEquals("horizontalAccuracy", 10.0, dl.getHorizontalAccuracy(), 0.0);
        assertEquals("latitude", 63.11914771365112, dl.getLatitude(), 0.0);
        assertEquals("longitude", 12.09229723730405, dl.getLongitude(), 0.0);
        assertEquals("locationFinished", true, dl.isLocationFinished());

    }
}
