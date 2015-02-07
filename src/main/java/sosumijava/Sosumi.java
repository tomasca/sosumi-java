/*
 * sosumi-java
 * Copyright 2014 Tomas Carlfalk. All rights reserved.
 */
package sosumijava;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author tomasca
 */
public class Sosumi {

    private static final Logger LOG = Logger.getLogger(Sosumi.class);
    private URL fmipHost;
    private URL partition;
    private String scope;
    private CloseableHttpClient httpclient;
    private static final int MAX_REDIRECTS = 4;
    private long locateRefreshInterval = 5000L;
    private String password;
    private String username;
    private HashMap<String, DeviceInfo> devices;
    private static final String CLIENT_CONTEXT = "\"clientContext\":{\"appName\":\"FindMyiPhone\",\"appVersion\":\"3.0\",\"buildVersion\":\"376\",\"clientTimestamp\":0,\"deviceUDID\":null,\"inactiveTime\":1,\"osVersion\":\"7.0.3\",\"productType\":\"iPhone6,1\",\"fmly\":true}";
    private static final String INIT_JSON_BODY = "{" + CLIENT_CONTEXT + "}";

    /**
     *
     * @param fmipHostUrl Base URL where the Find My iPhone service is located. E.g https://fmipmobile.icloud.com
     * @param username iCloud username (Apple ID)
     * @param password iCloud password
     */
    public Sosumi(String fmipHostUrl, String username, String password) throws MalformedURLException {
        this.fmipHost = new URL(fmipHostUrl);
        this.username = username;
        this.password = password;
        devices = new HashMap<String, DeviceInfo>();
        httpclient = HttpClients.createDefault();
    }

    private void refresh() throws SosumiException {
        CloseableHttpResponse resp = postApiCall("initClient", Sosumi.INIT_JSON_BODY);
        int redirects = 0;

        while ((redirects < MAX_REDIRECTS) && resp.getStatusLine().getStatusCode() == 330) {
            grabHeaders(resp);
            LOG.debug("Following 330 redirect...");
            resp = postApiCall("initClient", Sosumi.INIT_JSON_BODY);
        }

        LOG.debug("Response: " + resp.getStatusLine());

        if (resp.getStatusLine().getStatusCode() == 200) {
            try {
                String data = EntityUtils.toString(resp.getEntity());
                LOG.debug(data);
                JsonElement json = new JsonParser().parse(data);

                JsonArray jsonArray = json.getAsJsonObject().get("content").getAsJsonArray();
                HashMap<String, DeviceInfo> tmpDevices = new HashMap<String, DeviceInfo>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    DeviceInfo di = DeviceInfo.fromJson(jsonArray.get(i).toString());
                    tmpDevices.put(di.getDeviceName(), di);
                    LOG.debug(di);
                }
                this.devices = tmpDevices;

            } catch (IOException ex) {
                throw new SosumiException("Failed to read response payload", ex);
            } catch (ParseException ex) {
                throw new SosumiException("Failed to parse response payload", ex);
            }
        } else {
            LOG.warn("No successful response received");
            throw new SosumiException("Did not receive a successful respose from FMIP service");
        }
    }

    public void sendMessage(String deviceName, String text, String subject, boolean sound) throws SosumiException {
        DeviceInfo di = devices.get(deviceName);
        if (di == null) {
            refresh();
            di = devices.get(deviceName);
            if (di == null) {
                throw new SosumiException("Unknown device: " + deviceName);
            }
        }

        String json = "{" + CLIENT_CONTEXT
                + ",\"device\":\"" + di.getDeviceId() + "\""
                + ",\"emailUpdates\":null"
                + ",\"sound\":\"" + sound + "\""
                + ",\"subject\":\"" + subject + "\""
                + ",\"text\":\"" + text + "\""
                + ",\"userText\":\"true\""
                + "}";

        CloseableHttpResponse resp = postApiCall("sendMessage", json);
        LOG.debug("Response: " + resp.getStatusLine());
        if (resp.getStatusLine().getStatusCode() == 200) {
            try {
                String data = EntityUtils.toString(resp.getEntity());
                LOG.debug(data);

            } catch (IOException ex) {
                throw new SosumiException("Failed to read response payload", ex);
            } catch (ParseException ex) {
                throw new SosumiException("Failed to parse response payload", ex);
            }
        } else {
            LOG.warn("No successful response received");
            throw new SosumiException("Did not receive a successful respose from FMIP service");
        }
    }

    /**
     * Get the location of a device
     *
     * @param deviceName the name of the device to locate
     * @param timeout timeout in seconds
     * @return the device location
     * @throws SosumiException
     */
    @SuppressWarnings("SleepWhileInLoop")
    public DeviceLocation locateDevice(String deviceName, Integer timeout) throws SosumiException {
        if (timeout == null) {
            timeout = 120;
        }
        if (devices.get(deviceName) == null) {
            refresh();
        }

        long timeoutMillis = timeout * 1000L;
        long start = System.currentTimeMillis();
        while (!devices.get(deviceName).isLocationFinished()) {
            if ((System.currentTimeMillis() - start) > timeoutMillis) {
                throw new SosumiException("Failed to locate device. Request timed out (" + timeout + "s)");
            }
            try {
                Thread.sleep(locateRefreshInterval);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            refresh();
        }

        return devices.get(deviceName).getDeviceLocation();
    }

    private CloseableHttpResponse postApiCall(String urlFunction, String jsonBody) throws SosumiException {

        URL baseUrl = this.partition != null ? this.partition : getFmipHost();

        StringBuilder url = new StringBuilder();
        url.append(baseUrl.toExternalForm());
        url.append("/fmipservice/device/");
        url.append(this.scope != null ? this.scope : getUsername());
        url.append("/");
        url.append(urlFunction);

        HttpPost httppost = new HttpPost(url.toString());

        httppost.addHeader("Accept-Language", "en-us");
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        httppost.addHeader("X-Apple-Realm-Support", "1.0");
        httppost.addHeader("X-Apple-Find-Api-Ver", "3.0");
        httppost.addHeader("X-Apple-Authscheme", "UserIdGuest");
        httppost.addHeader("User-agent", "FindMyiPhone/376 CFNetwork/672.0.8 Darwin/14.0.0");
        httppost.addHeader("Authorization", "Basic " + getBasicAuthCredentials());

        HttpEntity entity;
        try {
            entity = new StringEntity(jsonBody);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        httppost.setEntity(entity);

        LOG.debug("Executing request " + httppost.getRequestLine());

        try {
            CloseableHttpResponse resp = httpclient.execute(httppost);
            return resp;
        } catch (IOException ioe) {
            throw new SosumiException("Failed to execute request", ioe);
        }
    }

    private String getBasicAuthCredentials() {
        String userAndPass = getUsername() + ":" + getPassword();
        return Base64.encodeBase64String(userAndPass.getBytes());
    }

    void setLocateRefreshInterval(long locateRefreshInterval) {
        this.locateRefreshInterval = locateRefreshInterval;
    }

    private String getUsername() {
        return username;
    }

    private String getPassword() {
        return password;
    }

    private URL getFmipHost() {
        return fmipHost;
    }

    private void grabHeaders(CloseableHttpResponse response) {
        Header hostHdr = response.getFirstHeader("X-Apple-MMe-Host");
        if (hostHdr != null) {
            try {
                String val = hostHdr.getValue();
                int colon = val.indexOf(':');
                if (colon >= 0) {
                    String host = val.substring(0, colon);
                    int port = Integer.parseInt(val.substring(colon + 1));
                    this.partition = new URL(fmipHost.getProtocol(), host, port, "/");
                } else {
                    this.partition = new URL(fmipHost.getProtocol(), hostHdr.getValue(), "/");
                }
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }
        Header scopeHdr = response.getFirstHeader("X-Apple-MMe-Scope");
        if (scopeHdr != null) {
            this.scope = scopeHdr.getValue();
        }
    }
}
