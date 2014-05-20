var oAuthPlugin = {
	callNativeFunction : function() {
		// alert("callNativeFunction");
		cordova.exec(
		// Register the callback handler
		function callback(data) {
			alert(data.access_token);
			// contentsDiv.innerHTML = 'File contents set.';
			// console.log('Wrote date ' + dateStr);
		},
		// Register the errorHandler
		function errorHandler(err) {
			alert('Error');
		},
		// Define what class to route messages to
		'OauthPlugin',
		// Execute this method on the above class
		'getToken',
		// An array containing one String (our newly created Date String).
		["dateStr"]);
	},
};
