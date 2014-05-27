package com.persistent.sso.lib;

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
import com.persistentsys.plugin.OAuthPluginListener;

public class PeasClientAuthenticator {

	private String clientID, redirectUrl, baseUrl, secretKey, ssoUrl;
	private OAuthPluginListener listener;
	private final static String QUERY_PARAMATER_AUTH_CODE = "code";

	private static final String TAG = "PeasClientAuthenticator";

	private Activity pluginActivity;

	static final String HEADER_KEY_SSO_API_APPID = "appid";
	static final String HEADER_VALUE_SSO_API_APPID = "peasappv2";

	public static PeasClientAuthenticator getAuthenticationHandler() {

		if (instance == null) {
			instance = new PeasClientAuthenticator();
		}
		return instance;
	}

	private static PeasClientAuthenticator instance = null;

	private PeasClientAuthenticator() {
		clientID = redirectUrl = baseUrl = secretKey = ssoUrl = null;

	}

	public void setPluginActivity(Activity pluginActivity) {
		this.pluginActivity = pluginActivity;
	}

	public void setParams(String baseUrl, String clientID, String secretKey,
			String redirectUrl) {
		this.baseUrl = baseUrl;
		this.clientID = clientID;
		this.secretKey = secretKey;
		this.redirectUrl = redirectUrl;
	}

	public void setListener(OAuthPluginListener listener) {
		this.listener = listener;
	}

	public void authorize() throws PeasClientAuthenticationException {

		String url;
		if (ssoUrl == null) {
			url = baseUrl;
		} else {
			url = ssoUrl;
		}

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

		sb.append("&packageName=");
		sb.append(pluginActivity.getBaseContext().getPackageName());

		sb.append("&apiName=DummyApiName");

		sb.append("&redirect_url=");
		sb.append(redirectUrl);

		final String finalAuthURI = sb.toString();
		Log.d(TAG, TAG + ".authorize(): finalAuthURI = " + finalAuthURI);

		final Uri authUri = Uri.parse(finalAuthURI);
		final Intent launchBrowser = new Intent(Intent.ACTION_VIEW, authUri);
		launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pluginActivity.getBaseContext().startActivity(launchBrowser);

	}

	public void peasAuthorize() throws PeasClientAuthenticationException {
		if (baseUrl == null || clientID == null || redirectUrl == null) {
			throw new PeasClientAuthenticationException(
					"Invalid parameters for authorize");
		}

		new GetSsoUrkTaskt(baseUrl, new SsoUrlTaskListener() {

			@Override
			public void onReceivedSsoUrl(String ssoUrl) {
				if (ssoUrl == null) {
					this.onFailure(null);
				} else {
					try {
						PeasClientAuthenticator.this.ssoUrl = ssoUrl;
						authorize();
					} catch (PeasClientAuthenticationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
			throw new PeasClientAuthenticationException(
					"Invalid clientID or Secret or authCode");
		}

		String url;
		if (ssoUrl == null) {
			url = baseUrl;
		} else {
			url = ssoUrl;
		}

		new SendAcessTokenRequestTask(clientID, secretKey, authCode, url,
				redirectUrl, new PeasClientAuthenticationTokenListener() {

					@Override
					public void onTokenReceived(JSONObject authResponse) {
						listener.onSuccess(authResponse);

					}

					@Override
					public void onTokenNotReceived() {
						// TODO Auto-generated method stub

					}
				}).execute();
		// new GetSsoUrkTaskt(baseUrl, new SsoUrlTaskListener() {
		// @Override
		// public void onReceivedSsoUrl(String ssoURL) {
		// new SendAcessTokenRequestTask(clientID, secretKey, authCode,
		// ssoURL, redirectUrl,
		// new PeasClientAuthenticationTokenListener() {
		//
		// @Override
		// public void onTokenReceived(JSONObject authResponse) {
		// listener.onSuccess(authResponse);
		//
		// }
		//
		// @Override
		// public void onTokenNotReceived() {
		// // TODO Auto-generated method stub
		//
		// }
		// }).execute();
		// }
		//
		// @Override
		// public void onFailure(String reason) {
		//
		// }
		// }).execute();
	}

	private String getIMEI() {
		final TelephonyManager telephonymanager = (TelephonyManager) pluginActivity
				.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		return telephonymanager.getDeviceId();
	}

	public static class oAuthResponse {

		// Name of the fields in JSON used while parsing
		public static final String ACCESS_TOKEN = "access_token";
		public static final String ACCESS_PASSWORD = "password ";
		public static final String ACCESS_USERNAME = "userName";
		public static final String ACCESS_DOMAIN = "domainName";

		public static final String ACCESS_CODE = "code";

		private String access_token;
		private String username;
		private String password;
		private String domainName;

		public String getAccessToken() {
			return access_token;
		}

		public void setAccessToken(String token) {
			access_token = token;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDomainName() {
			return domainName;
		}

		public void setDomainName(String domainName) {
			this.domainName = domainName;
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

		System.out
				.println("GetSsoUrkTaskt.onPostExecute(): ssoUrl = " + ssoUrl);

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
		/*
		 * Log.v("Json", "Json" + jsonObj); final oAuthResponse tokenDetials =
		 * new oAuthResponse();
		 * 
		 * try { final JSONTokener tokener = new JSONTokener(responseData);
		 * JSONObject jsonObject = new JSONObject(tokener);
		 * 
		 * if (jsonObject.has(oAuthResponse.ACCESS_TOKEN)) { final String
		 * accessToken = jsonObject .getString(oAuthResponse.ACCESS_TOKEN);
		 * tokenDetials.setAccessToken(accessToken); }
		 * 
		 * if (jsonObject.has(oAuthResponse.ACCESS_USERNAME)) { final String
		 * username = jsonObject .getString(oAuthResponse.ACCESS_USERNAME);
		 * tokenDetials.setUsername(username); }
		 * 
		 * if (jsonObject.has("password")) { final String pwd =
		 * jsonObject.getString("password"); tokenDetials.setPassword(pwd);
		 * 
		 * System.out.println("parseAccessToken(): set pwd  = " + pwd); }
		 * 
		 * if (jsonObject.has(oAuthResponse.ACCESS_DOMAIN)) { final String
		 * domain = jsonObject .getString(oAuthResponse.ACCESS_DOMAIN);
		 * tokenDetials.setDomainName(domain); }
		 * 
		 * } catch (JSONException e) { e.printStackTrace(); }
		 */

		// return tokenDetials;
	}

}