import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.persistent.sso.lib.EnterpriseAuthenticator;
import com.persistent.sso.lib.PeasClientAuthenticator;
import com.persistentsys.plugin.OAuthPluginListener;




public class OauthPlugin extends CordovaPlugin {
	public static final String ACTION_AUTHORIZE = "authorize";
	public static final String ACTION_PEAS_AUTHORIZE = "peasAuthorize";
	public static final String ACTION_ENTERPRISE_AUTHORIZE = "enterpriseAuthorize";
	public static final String TAG = OauthPlugin.class.getName();

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		// TODO Auto-generated method stub
		super.execute(action, args, callbackContext);
		Log.v("iGreet", "iGreet: execute");

		try {
			JSONObject arg_object = args.getJSONObject(0);
			String baseUrl = arg_object.getString("baseUrl");
			String consumerKey = arg_object.getString("consumerKey");
			String secretKey = arg_object.getString("secretKey");
			String redirectUrl = arg_object.getString("redirectUrl");
			Log.v(TAG, "iGreet:" + baseUrl + consumerKey + secretKey + redirectUrl + action);
			
			
			
			if(ACTION_ENTERPRISE_AUTHORIZE.equals(action)) {
				EnterpriseAuthenticator authenticator = EnterpriseAuthenticator.getAuthenticationHandler();
				authenticator.setPluginActivity(this.cordova.getActivity());
				authenticator.setParams(consumerKey, secretKey, redirectUrl);
				authenticator.setListener(new OAuthPluginListener() {
					
					@Override
					public void onSuccess(JSONObject tokenDetails) {
						// TODO Auto-generated method stub
						callbackContext.success(tokenDetails);
						Log.v(TAG, "onSuccess: " + tokenDetails);
					}
					
					@Override
					public void onFail(String error) {
						// TODO Auto-generated method stub
						
					}
				});
				authenticator.authorize(baseUrl);
				return true;
			} else {
				
				PeasClientAuthenticator authenticator = PeasClientAuthenticator.getAuthenticationHandler();
				authenticator.setPluginActivity(this.cordova.getActivity());
				authenticator.setParams(consumerKey, secretKey, redirectUrl);
				authenticator.setListener(new OAuthPluginListener() {
					
					@Override
					public void onSuccess(JSONObject tokenDetails) {
						// TODO Auto-generated method stub
						callbackContext.success(tokenDetails);
						Log.v(TAG, "onSuccess: " + tokenDetails);
					}
					
					@Override
					public void onFail(String error) {
						// TODO Auto-generated method stub
						
					}
				});
				if (ACTION_AUTHORIZE.equals(action)) {
					authenticator.authorize(baseUrl);
					return true;
				} else if(ACTION_PEAS_AUTHORIZE.equals(action)) {
					authenticator.peasAuthorize(baseUrl);
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
	}
}
