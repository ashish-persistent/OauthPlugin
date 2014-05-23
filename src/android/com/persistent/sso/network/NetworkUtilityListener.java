package com.persistent.sso.network;

public interface NetworkUtilityListener {
	
	boolean onSuccess( String message, Object response);
	
	
	void onFailure( String message, Object response);
}
