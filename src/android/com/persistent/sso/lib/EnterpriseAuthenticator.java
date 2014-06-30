package com.persistent.sso.lib;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.persistent.sso.network.NetworkUtility;

public class EnterpriseAuthenticator extends BaseAuthenticator {

	public static EnterpriseAuthenticator getAuthenticationHandler() {

		if (instance == null) {
			instance = new EnterpriseAuthenticator();
		}
		return instance;
	}

	private static EnterpriseAuthenticator instance = null;

	private EnterpriseAuthenticator() {
		super();

	}

	public void authorize(String url) {
		Log.v(TAG, "iGreet: authorize");
		baseUrl = url;
		final StringBuilder sb = new StringBuilder();
		sb.append(baseUrl);

		sb.append("/authorize?response_type=code&client_id=");
		sb.append(clientID);

		sb.append("&scope=Read");

		sb.append("&redirect_uri=");
		sb.append(redirectUrl);

		final String finalAuthURI = sb.toString();
		Log.d(TAG, TAG + ".authorize(): finalAuthURI = " + finalAuthURI);

		Uri authUri = Uri.parse(finalAuthURI);
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, authUri);
		launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pluginActivity.getBaseContext().startActivity(launchBrowser);

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
		Log.v(TAG, "iGreet: " + authCode);
		if (baseUrl == null || clientID == null || secretKey == null
				|| authCode == null || redirectUrl == null) {
			 Log.v("iGreet", "iGreet: Invalid clientID or Secret or authCode");
			 listener.onFail("Error in login");
		} else {
			new RequestAcessTokenTask(clientID, secretKey, authCode, baseUrl,
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

	}
}



class RequestAcessTokenTask extends AsyncTask<String, Void, String> {
	private static final String TAG = "RequestAcessTokenTask";

	private final String clientID;
	private final String clientSecret;
	private final String authCode;
	private final String ssoURL;
	private final String redirectURL;
	private final PeasClientAuthenticationTokenListener listener;

	public RequestAcessTokenTask(String clientID, String clientSecret,
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
				sb.append("&scope=Read&code=");
				sb.append(authCode);
				sb.append("&client_id=");
				sb.append(clientID);

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
						.setRequestMethod(NetworkUtility.REQUEST_METHOD_POST);
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
	}

}