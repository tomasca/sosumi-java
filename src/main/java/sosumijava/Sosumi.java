/*
 * sosumi-java
 * Copyright 2014 Tomas Carlfalk. All rights reserved.
 */
package sosumijava;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 *
 * @author tomasca
 * @author Danilo Recchia
 */
public class Sosumi {

	private static final Gson gson = new GsonBuilder().create();
	private static final Logger LOG = Logger.getLogger(Sosumi.class);
	private static final int MAX_REDIRECTS = 4;
	private final Map<String, Object> clientContext;
	
	private URL fmipHost;
	private URL partition;
	private String scope;
	private CloseableHttpClient httpclient;
	private long locateRefreshInterval = 5000L;
	private String password;
	private String username;
	private HashMap<String, DeviceInfo> devices;

	/**
	 *
	 * @param fmipHostUrl Base URL where the Find My iPhone service is located. E.g https://fmipmobile.icloud.com
	 * @param username iCloud username (Apple ID)
	 * @param password iCloud password
	 */
	public Sosumi(
			final String fmipHostUrl, 
			final String username, 
			final String password) throws MalformedURLException {

		this.fmipHost = new URL(fmipHostUrl);
		this.username = username;
		this.password = password;
		devices = new HashMap<String, DeviceInfo>(4);
		httpclient = HttpClients.createDefault();
		
		clientContext = new HashMap<String, Object>(10);
		clientContext.put( "appName", "FindMyiPhone" );
		clientContext.put( "appVersion", "3.0" );
		clientContext.put( "buildVersion", "376" );
		clientContext.put( "clientTimestamp", 0 );
		clientContext.put( "deviceUDID", null );
		clientContext.put( "inactiveTime", 1 );
		clientContext.put( "osVersion", "7.0.3" );
		clientContext.put( "productType", "iPhone6,1" );
		clientContext.put( "fmly", true );
	}
	
	private void refresh() throws SosumiException {

		CloseableHttpResponse resp = postApiCall("initClient", gson.toJson( clientContext ));
		int redirects = 0;

		while ((redirects < MAX_REDIRECTS) && resp.getStatusLine().getStatusCode() == 330) {
			grabHeaders(resp);
			LOG.debug("Following 330 redirect...");
			resp = postApiCall("initClient", gson.toJson( clientContext ));
		}

		LOG.debug("Response: " + resp.getStatusLine());

		if (resp.getStatusLine().getStatusCode() == 200) {
			try {
				String data = EntityUtils.toString(resp.getEntity());
				LOG.debug(data);
				JsonElement json = new JsonParser().parse(data);

				JsonArray jsonArray = json.getAsJsonObject().get("content").getAsJsonArray();
				HashMap<String, DeviceInfo> tmpDevices = new HashMap<String, DeviceInfo>(5);
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

	public Collection<DeviceInfo> getDevices() throws SosumiException {

		if ( this.devices.size() < 1 ) {
			this.refresh();
		}

		return this.devices.values();
	}

	public void sendMessage(
			final String deviceName, 
			final String text, 
			final String subject, 
			final boolean sound, 
			final boolean emailUpdates ) throws SosumiException {

		DeviceInfo di = getDeviceInfo( deviceName );

		Map<String, Object> jsonMap = new HashMap<String, Object>(7);
		jsonMap.put( "clientContext", clientContext );
		jsonMap.put( "device", di.getDeviceId() );
		jsonMap.put( "emailUpdates", emailUpdates );
		jsonMap.put( "sound", sound );
		jsonMap.put( "subject", subject );
		jsonMap.put( "text", text );
		jsonMap.put( "userText", true );
		
		doRemoteRequestAndReadAnswer( "sendMessage", gson.toJson( jsonMap ) );
	}

	public void playSound(
			final String deviceName, 
			final String title ) throws SosumiException {

		DeviceInfo di = getDeviceInfo( deviceName );
		
		Map<String, Object> jsonMap = new HashMap<String, Object>(3);
		jsonMap.put( "clientContext", clientContext );
		jsonMap.put( "device", di.getDeviceId() );
		jsonMap.put( "subject", title );
		
		doRemoteRequestAndReadAnswer( "playSound", gson.toJson( jsonMap ) );
	}

	private DeviceInfo getDeviceInfo(
			final String deviceName ) throws SosumiException {

		DeviceInfo di = devices.get(deviceName);
		if (di == null) {
			refresh();
			di = devices.get(deviceName);
			if (di == null) {
				throw new SosumiException("Unknown device: " + deviceName);
			}
		}
		return di;
	}

	public void startLostMode( 
			final String deviceName, 
			final String msg, 
			final String ownerNbr, 
			final String passcode, 
			final boolean playSound, 
			final boolean emailUpdates ) throws SosumiException {

		DeviceInfo di = getDeviceInfo( deviceName );

		Map<String, Object> jsonMap = new HashMap<String, Object>(11);
		jsonMap.put( "clientContext", clientContext );
		jsonMap.put( "device", di.getDeviceId() );
		jsonMap.put( "lostModeEnabled", true );
		jsonMap.put( "trackingEnabled", true );
		jsonMap.put( "sound", playSound );
		jsonMap.put( "emailUpdates", emailUpdates );
		
		if ( passcode != null ) {
			jsonMap.put( "passcode", passcode );
		}

		if ( ownerNbr != null ) {
			jsonMap.put( "ownerNbr", ownerNbr );
		}

		if ( msg != null ) {
			jsonMap.put( "userText", true );
			jsonMap.put( "text", msg );
		} else {
			jsonMap.put( "userText", false );
		}

		doRemoteRequestAndReadAnswer( "lostDevice", gson.toJson( jsonMap ) );
	}

	public void stopLostMode( 
			final String deviceName ) throws SosumiException {

		DeviceInfo di = getDeviceInfo( deviceName );
		
		Map<String, Object> jsonMap = new HashMap<String, Object>(6);
		jsonMap.put( "clientContext", clientContext );
		jsonMap.put( "device", di.getDeviceId() );
		jsonMap.put( "lostModeEnabled", true );
		jsonMap.put( "trackingEnabled", false );
		jsonMap.put( "emailUpdates", false );
		jsonMap.put( "userText", false );

		doRemoteRequestAndReadAnswer( "lostDevice", gson.toJson( jsonMap ) );
	}

	public void lock( 
			final String deviceName,
			final String passcode ) throws SosumiException {

		DeviceInfo di = getDeviceInfo( deviceName );

		Map<String, Object> jsonMap = new HashMap<String, Object>(3);
		jsonMap.put( "clientContext", clientContext );
		jsonMap.put( "device", di.getDeviceId() );
		jsonMap.put( "emailUpdates", true );
		
		if ( passcode != null ) {
			jsonMap.put( "passcode", passcode );
		}
		
		doRemoteRequestAndReadAnswer( "remoteLock", gson.toJson( jsonMap ) );
	}

	private void doRemoteRequestAndReadAnswer(
			final String endpoint,
			final String json ) throws SosumiException {

		CloseableHttpResponse resp = postApiCall(endpoint, json);
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

	public void wipe( 
			final  String deviceName ) throws SosumiException {
		
		DeviceInfo di = getDeviceInfo( deviceName );

		Map<String, Object> jsonMap = new HashMap<String, Object>(3);
		jsonMap.put( "clientContext", clientContext );
		jsonMap.put( "device", di.getDeviceId() );
		
		doRemoteRequestAndReadAnswer( "remoteWipe", gson.toJson( jsonMap ) );
	}

	/**
	 * Get the location of a device
	 *
	 * @param deviceName the name of the device to locate
	 * @param timeout timeout in seconds
	 * @return the device location
	 * @throws SosumiException
	 */
	public DeviceLocation locateDevice(
			final String deviceName, 
			final Integer timeout ) throws SosumiException {

		int internalTimeout = 120;
		
		if (timeout != null) {
			internalTimeout = timeout;
		}
		if (devices.get(deviceName) == null) {
			refresh();
		}

		long timeoutMillis = internalTimeout * 1000L;
		long start = System.currentTimeMillis();
		while (!devices.get(deviceName).isLocationFinished()) {
			if ((System.currentTimeMillis() - start) > timeoutMillis) {
				throw new SosumiException("Failed to locate device. Request timed out (" + internalTimeout + "s)");
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

	private CloseableHttpResponse postApiCall(
			final String urlFunction, 
			final String jsonBody) throws SosumiException {

		URL baseUrl = this.partition != null ? this.partition : this.fmipHost;

		StringBuilder url = new StringBuilder();
		url.append(baseUrl.toExternalForm());
		url.append("/fmipservice/device/");
		url.append(this.scope != null ? this.scope : this.username);
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
		
		String userAndPass = this.username + ":" + this.password;
		return Base64.encodeBase64String(userAndPass.getBytes());
	}

	void setLocateRefreshInterval(long locateRefreshInterval) {
		
		this.locateRefreshInterval = locateRefreshInterval;
	}

	private void grabHeaders(
			final CloseableHttpResponse response) {

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
