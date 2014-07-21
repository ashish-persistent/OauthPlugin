package com.persistent.sso.lib;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.persistent.sso.network.NetworkUtility;
import com.persistent.sso.network.NetworkUtilityListener;

public class PeasClientAuthenticator extends BaseAuthenticator {

	static final String HEADER_KEY_SSO_API_APPID = "appid";
	static final String HEADER_VALUE_SSO_API_APPID = "peasappv3.1";
	static final String CONTENT_TYPE = "Content-Type";

	public static PeasClientAuthenticator getAuthenticationHandler() {

		if (instance == null) {
			instance = new PeasClientAuthenticator();
		}
		return instance;
	}

	private static PeasClientAuthenticator instance = null;

	private PeasClientAuthenticator() {
		super();
	}

	public void logout() throws PeasClientAuthenticationException {

		if (baseUrl == null) {

		} else {
			String logoutURL = baseUrl + "/logout";
			new LogoutAsyncTask(new PeasClientLogoutListener() {

				@Override
				public void onLogoutFailed(String reason) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onLoggedOut(JSONObject result) {
					listener.onLogoutSuccess(result);
					// TODO Auto-generated method stub

				}
			}, logoutURL, getIMEI()).execute();

		}

	}

	public void authorize(String url) {
		Log.v(TAG, "iGreet: authorize: " + url);

		final StringBuilder sb = new StringBuilder();
		sb.append(url);

		sb.append("/authorize?response_type=code&client_id=");
		sb.append(clientID);

		sb.append("&scope=READ&state=");
		sb.append(baseUrl);

		sb.append("&deviceId=");
		sb.append(getIMEI());

		sb.append("&deviceOs=Android");

		sb.append("&deviceOsVersion=");
		sb.append(android.os.Build.VERSION.RELEASE);

		sb.append("&packageName=com.persistentsys.surveyapp");
		// sb.append(pluginActivity.getBaseContext().getPackageName());

		sb.append("&apiName=DummyApiName");

		sb.append("&redirect_url=");
		sb.append(redirectUrl);
		baseUrl = url;

		final String finalAuthURI = sb.toString();
		Log.d(TAG, TAG + ".authorize(): finalAuthURI = " + finalAuthURI);

		final Uri authUri = Uri.parse(finalAuthURI);
		final Intent launchBrowser = new Intent(Intent.ACTION_VIEW, authUri);
		launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pluginActivity.getBaseContext().startActivity(launchBrowser);

	}

	public void peasAuthorize(String url)
			throws PeasClientAuthenticationException {
		Log.v("iGreet", "iGreet: peasAuthorize");
		baseUrl = url;
		if (baseUrl == null || clientID == null || redirectUrl == null) {
			Log.v("iGreet", "iGreet: Invalid parameters for authorize");
			throw new PeasClientAuthenticationException(
					"Invalid parameters for authorize");
		}

		new GetSsoUrkTaskt(baseUrl, new SsoUrlTaskListener() {

			@Override
			public void onReceivedSsoUrl(String ssoUrl) {
				if (ssoUrl == null) {
					this.onFailure(null);
				} else {
					authorize(ssoUrl);
				}
			}

			@Override
			public void onFailure(String reason) {
				Log.w(TAG, TAG + ".authorize.onFailure()");

			}
		}).execute();

	}

	public void getAccessToken(Activity activity)
			throws PeasClientAuthenticationException {
		Intent intent = activity.getIntent();
		Uri customUri = intent.getData();
		final String authCode = customUri
				.getQueryParameter(QUERY_PARAMATER_AUTH_CODE);
		Intent i = new Intent(activity, pluginActivity.getClass());
		// i.addCategory(Intent.CATEGORY_BROWSABLE);
		// i.setAction(Intent.ACTION_MAIN);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		activity.startActivity(i);
		activity.finish();
		Log.v("iGreet", "iGreet: " + authCode);
		if (baseUrl == null || clientID == null || secretKey == null
				|| authCode == null || redirectUrl == null) {
			listener.onFail("Error in login");
		}

		new SendAcessTokenRequestTask(clientID, secretKey, authCode, baseUrl,
				redirectUrl, new PeasClientAuthenticationTokenListener() {

					@Override
					public void onTokenReceived(JSONObject authResponse) {
						listener.onSuccess(authResponse);

					}

					@Override
					public void onTokenNotReceived() {
						listener.onFail("Error in getting token");
						// TODO Auto-generated method stub

					}
				}).execute();
	}

	private String getIMEI() {
		final TelephonyManager telephonymanager = (TelephonyManager) pluginActivity
				.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		return telephonymanager.getDeviceId();
	}
}

class PeasNetworkHeaders {
	private static final String CONTENT_TYPE = "Content-Type";

	public PeasNetworkHeaders() {
		super();
	}

	public Map<String, String> getLogoutRequestHeaders() {
		final Map<String, String> headers = new HashMap<String, String>(1);
		headers.put(CONTENT_TYPE, LogoutAsyncTask.CONTENT_TYPE_JSON);
		return headers;
	}

}

class LogoutAsyncTask extends AsyncTask<URL, Integer, String> {
	private final PeasClientLogoutListener listener;

	private static final String TAG = "LogoutAsyncTask";

	public static final String KEY_DEVICE_ID = "deviceId";

	public static final String CONTENT_TYPE_JSON = "application/json";

	private final String url;
	private final String deviceId;

	public LogoutAsyncTask(final PeasClientLogoutListener logoutListener,
			final String url, final String deviceId) {
		this.listener = logoutListener;
		this.url = url;
		this.deviceId = deviceId;
	}

	@Override
	protected String doInBackground(URL... params) {
		Log.d(TAG, TAG + ".doInBackground()");

		final Map<String, String> headers = new HashMap<String, String>(1);
		headers.put(PeasClientAuthenticator.HEADER_KEY_SSO_API_APPID,
				PeasClientAuthenticator.HEADER_VALUE_SSO_API_APPID);

		headers.put(PeasClientAuthenticator.CONTENT_TYPE, CONTENT_TYPE_JSON);

		final NetworkUtility argHTTPClient = new NetworkUtility(
				new LogoutApiListener(), headers);
		argHTTPClient.setURL(url);
		argHTTPClient.setRequestMethod(NetworkUtility.REQUEST_METHOD_POST);

		final JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_DEVICE_ID, deviceId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d(TAG,
				TAG + ".doInBackground(): submitting data = " + obj.toString());

		argHTTPClient.setData(obj.toString().getBytes());

		return argHTTPClient.sendRequest();
	}

	@Override
	protected void onPostExecute(String error) {
		Log.d(TAG, TAG + ".onPostExecute(): error string = " + error);
	}

	private class LogoutApiListener implements NetworkUtilityListener {

		@Override
		public boolean onSuccess(String message, Object response) {
			if (listener != null) { 
				String result = (String)response;
				listener.onLoggedOut(parseAcessToken(result));
			}
			return false;
		}

		@Override
		public void onFailure(String message, Object response) {
			if (listener != null) {
				listener.onLogoutFailed(message);
			}
		}
		
		private JSONObject parseAcessToken(String responseData) {

			System.out
					.println("parseAccessToken(): responseData = " + responseData);
			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(responseData);
				return jsonObj;
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}


	}

}

class GetSsoUrkTaskt extends AsyncTask<String, Void, String> {
	private static final String oAuthUrlFooter = "/peas/ssourl";

	private final String peasURL;
	private final SsoUrlTaskListener listener;

	public GetSsoUrkTaskt(String peasURL, final SsoUrlTaskListener listener) {
		super();
		this.peasURL = peasURL;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... arg0) {
		try {
			return getSsoUrl(peasURL + oAuthUrlFooter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getSsoUrl(final String ssoApiUrl) {
		final Map<String, String> headers = new HashMap<String, String>(1);
		headers.put(PeasClientAuthenticator.HEADER_KEY_SSO_API_APPID,
				PeasClientAuthenticator.HEADER_VALUE_SSO_API_APPID);

		final NetworkUtility argHTTPClient = new NetworkUtility(null, headers);
		argHTTPClient.setURL(ssoApiUrl);
		argHTTPClient.setRequestMethod(NetworkUtility.REQUEST_METHOD_GET);
		final String response = argHTTPClient.sendRequest();

		final String ssoUrl = parseSsoUrl(response);

		return ssoUrl;
	}

	@Override
	protected void onPostExecute(String ssoUrl) {
		super.onPostExecute(ssoUrl);
		if (ssoUrl != null) {
			if (listener != null) {
				listener.onReceivedSsoUrl(ssoUrl);
			}
		} else {
			if (listener != null) {
				listener.onFailure(null);
			}
		}

	}

	private static final String STATUS = "status";
	private static final String URL = "url";

	private String parseSsoUrl(String responseData) {
		System.out.println("parseSsoUrl(): responseData = " + responseData);
		String ssoUrl = null;
		try {
			final JSONTokener tokener = new JSONTokener(responseData);
			JSONObject jsonObject = new JSONObject(tokener);

			if (jsonObject.has(STATUS)) {
				final String statusValue = jsonObject.getString(STATUS);

				if (statusValue != null && statusValue.equals("200")) {
					if (jsonObject.has(URL)) {
						final String url = jsonObject.getString(URL);
						if (url != null) {
							ssoUrl = url;
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ssoUrl;
	}
}

interface SsoUrlTaskListener {
	void onReceivedSsoUrl(String ssoURL);

	void onFailure(String reason);
};

class SendAcessTokenRequestTask extends AsyncTask<String, Void, String> {
	private static final String TAG = "SendAcessTokenRequestTask";

	private final String clientID;
	private final String clientSecret;
	private final String authCode;
	private final String ssoURL;
	private final String redirectURL;
	private final PeasClientAuthenticationTokenListener listener;

	public SendAcessTokenRequestTask(String clientID, String clientSecret,
			String authCode, String ssoURL, String redirectURL,
			PeasClientAuthenticationTokenListener listener) {
		super();
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.authCode = authCode;
		this.ssoURL = ssoURL;
		this.redirectURL = redirectURL;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... arg0) {
		return sendAcessTokenRequest(clientID, clientSecret, authCode, ssoURL,
				redirectURL);
	}

	private String sendAcessTokenRequest(final String clientID,
			final String clientSecret, final String authCode,
			final String ssoURL, final String redirectURL) {
		String response = null;

		try {
			try {
				final StringBuilder sb = new StringBuilder();
				sb.append(ssoURL);

				sb.append("/token?grant_type=authorization_code");
				sb.append("&response_type=code&code=");
				sb.append(authCode);

				sb.append("&redirect_uri=");
				sb.append(redirectURL);

				final String tokenURL = sb.toString();
				Log.d(TAG, TAG + ".sendAuthorizeRequest(): tokenURL = "
						+ tokenURL);

				final String params = clientID + ':' + clientSecret;
				final String runtimeHeaderValue = new String(Base64.encode(
						params.getBytes(), Base64.DEFAULT)).trim();
				final Map<String, String> headers = new HashMap<String, String>(
						1);
				headers.put("Authorization", "Basic " + runtimeHeaderValue);

				final NetworkUtility argHTTPClient = new NetworkUtility(null,
						headers);
				argHTTPClient.setURL(tokenURL);
				argHTTPClient
						.setRequestMethod(NetworkUtility.REQUEST_METHOD_GET);
				response = argHTTPClient.sendRequest();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	@Override
	protected void onPostExecute(String response) {
		super.onPostExecute(response);

		if (response != null) {
			JSONObject tokenDetials = null;
			if (response != null && !response.isEmpty()) {
				tokenDetials = parseAcessToken(response);
				if (listener != null) {
					listener.onTokenReceived(tokenDetials);
				}
			} else {
				if (listener != null) {
					listener.onTokenNotReceived();
				}
			}
		} else {
			if (listener != null) {
				listener.onTokenNotReceived();
			}
		}
	}

	private JSONObject parseAcessToken(String responseData) {

		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(responseData);
			return jsonObj;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

}