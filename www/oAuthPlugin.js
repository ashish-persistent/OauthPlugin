var oAuthPlugin = {

	callNativeFunction : function(success, error, options) {
		// alert("callNativeFunction");
		cordova.exec(success, error, 'OauthPlugin', 'getToken', [options]);
		// return Cordova.exec(success, fail, "OauthPlugin", "getToken", ["resultType","DataType","New Data"]);
		// return cordova.exec(successCallback, failureCallback, "MyCustomSavePluginCommand","saveToDocumentsMethod",[data[0]]);
	},
}; 