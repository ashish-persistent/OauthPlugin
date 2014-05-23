package com.persistentsys.plugin;

import android.app.Activity;
import android.os.Bundle;

import com.persistent.sso.lib.PeasClientAuthenticationException;
import com.persistent.sso.lib.PeasClientAuthenticator;

public class RedirectedActivity extends Activity {

	// private static final String TAG = "RedirectedActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			PeasClientAuthenticator.getAuthenticationHandler().getAccessToken(this);
		} catch (PeasClientAuthenticationException e) {
			e.printStackTrace();
		} 
	}
}
