package com.persistent.sso.lib;

import org.json.JSONObject;

public interface PeasClientLogoutListener {

	void onLoggedOut(JSONObject result);

	void onLogoutFailed(String reason);
}
