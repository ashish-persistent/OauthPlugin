import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.persistent.sso.lib.PeasClientAuthenticator;
import com.persistentsys.plugin.OAuthPluginListener;




public class OauthPlugin extends CordovaPlugin {
	public static final String ACTION_AUTHORIZE = "authorize";
	public static final String ACTION_PEAS_AUTHORIZE = "peasAuthorize";

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
			
			PeasClientAuthenticator auth = PeasClientAuthenticator.getAuthenticationHandler();
			auth.setPluginActivity(this.cordova.getActivity());
			auth.setParams(baseUrl, consumerKey, secretKey, redirectUrl);
			auth.setListener(new OAuthPluginListener() {
				
				@Override
				public void onSuccess(JSONObject tokenDetails) {
					// TODO Auto-generated method stub
					callbackContext.success(tokenDetails);
					Log.v("onSuccess", "onSuccess: " + tokenDetails);
				}
				
				@Override
				public void onFail(String error) {
					// TODO Auto-generated method stub
					
				}
			});
			if (ACTION_AUTHORIZE.equals(action)) {
				auth.authorize();
				return true;
			} else if(ACTION_PEAS_AUTHORIZE.equals(action)) {
				auth.peasAuthorize();
				return true;
			}
			return false;
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
	}
}
