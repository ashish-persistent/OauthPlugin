package com.persistent.sso.lib;

import android.app.Activity;

import com.persistentsys.plugin.OAuthPluginListener;

public abstract class BaseAuthenticator {
	protected String clientID, redirectUrl, baseUrl, secretKey;
	protected OAuthPluginListener listener;
	protected final static String QUERY_PARAMATER_AUTH_CODE = "code";
	protected String TAG;

	protected Activity pluginActivity;
	
	
	protected BaseAuthenticator() {
		clientID = redirectUrl = baseUrl = secretKey = null;
		TAG = this.getClass().getName();
	}

	public void setPluginActivity(Activity pluginActivity) {
		this.pluginActivity = pluginActivity;
	}

	public void setParams(String clientID, String secretKey,
			String redirectUrl) {
		this.clientID = clientID;
		this.secretKey = secretKey;
		this.redirectUrl = redirectUrl;
	}

	public void setListener(OAuthPluginListener listener) {
		this.listener = listener;
	}

	
	abstract public void authorize(String url);
}
