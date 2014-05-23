package com.persistent.sso.lib;

import org.json.JSONObject;

public interface PeasClientAuthenticationTokenListener {

	void onTokenReceived( JSONObject authResponse);
	
	void onTokenNotReceived( );
}
