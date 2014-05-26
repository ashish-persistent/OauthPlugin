package com.persistentsys.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.persistent.sso.lib.PeasClientAuthenticationException;
import com.persistent.sso.lib.PeasClientAuthenticator;

public class RedirectedActivity extends Activity {

	// private static final String TAG = "RedirectedActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("iGreet", "iGreet: RedirectedActivity");

		try {
			PeasClientAuthenticator.getAuthenticationHandler().getAccessToken(this);
		} catch (PeasClientAuthenticationException e) {
			e.printStackTrace();
		} 
	}
}
