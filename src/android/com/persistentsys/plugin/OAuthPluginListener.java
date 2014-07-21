package com.persistentsys.plugin;

import org.json.JSONObject;

public interface OAuthPluginListener {
	void onSuccess(JSONObject tokenDetails);
	void onFail(String error);
	void onLogoutSuccess(JSONObject result);
}
