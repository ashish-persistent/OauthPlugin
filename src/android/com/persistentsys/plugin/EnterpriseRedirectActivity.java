package com.persistentsys.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.persistent.sso.lib.EnterpriseAuthenticator;
import com.persistent.sso.lib.PeasClientAuthenticationException;

public class EnterpriseRedirectActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("iGreet", "iGreet: EnterpriseRedirectActivity");

		try {
			EnterpriseAuthenticator.getAuthenticationHandler().getAccessToken(
					this);
		} catch (PeasClientAuthenticationException e) {
			e.printStackTrace();
		}
	}

}
