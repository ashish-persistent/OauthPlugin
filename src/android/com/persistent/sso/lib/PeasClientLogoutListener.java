package com.persistent.sso.lib;

public interface PeasClientLogoutListener {

	void onLoggedOut( );
	
	void onLogoutFailed( String reason);
}
