var oAuthPlugin = {

	callNativeFunction : function(success, error, oauth_type, options) {
		alert("callNativeFunction");
		cordova.exec(success, error, 'OauthPlugin', oauth_type, [options]);
		// return Cordova.exec(success, fail, "OauthPlugin", "getToken", ["resultType","DataType","New Data"]);
		// return cordova.exec(successCallback, failureCallback, "MyCustomSavePluginCommand","saveToDocumentsMethod",[data[0]]);
	},
}; 