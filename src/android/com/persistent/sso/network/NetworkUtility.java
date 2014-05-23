package com.persistent.sso.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;


public class NetworkUtility 
{	
	public static final String INVALID_CREDENTIALS 		= "Invalid User Credentials";
	public static final String SERVER_ERROR 			= "Server responded with error";
	public static final String NETWORK_ERROR 			= "Could not connect to server";
	public static final String EMPTY_DATA 				= "No data found at server";
	public static final String DEVICE_BLOCKED 		    = "This device is blocked";
	public static final String DEVICE_BLOCKED_MSG       = "Your device is blocked, please contact administrator";
	public static final String FORBIDDEN_NO_RESPONSE    = "Unable to authenticate. Please verify your credential, or contact the administrator";
	
	private static final String CONTENT_TYPE 			= "Content-Type";
	private static final int  CONTENT_TIME_OUT 			=  50000;
	private static final String TAG = "NetworkUtility:" ;

			
	private final Map<String, String> headerMap;
	private NetworkUtilityListener listener;
	
	private String expectedResponseContentType;
	private  boolean checkResponseContentType;
	
	public NetworkUtility(NetworkUtilityListener listener, Map<String, String> headerMap) {
		super();
		this.listener = listener;
		this.headerMap = headerMap;
	}
	

	public static final int REQUEST_METHOD_GET 		= 0;
	public static final int REQUEST_METHOD_POST 	= 1;
	public static final int REQUEST_METHOD_PUT 		= 2;
	public static final int REQUEST_METHOD_DELETE 	= 3;
	
	private int requestMethod;
	public void setRequestMethod(final int requestMethod)
	{
		this.requestMethod = requestMethod;
	}

	private String urlString;
	public void setURL(final String url)
	{
		this.urlString = url;
	}
	
	private byte[] dataToSend;
	public void setData(final byte[] dataToSend)
	{
		this.dataToSend = dataToSend;
	}
	
	private HttpURLConnection httpUrlconnection = null;
	
	private void connect() throws IOException
	{
		OutputStream outputstream 	= null;

		try 
		{
			Log.d(TAG +"sendRequest()", "Sending request to server URL:"+urlString);
			final URL url  = new URL(urlString);

			if(urlString.startsWith( "https"))
			{
				trustAllHosts();
				final HttpsURLConnection httpsUrlconnection = (HttpsURLConnection)url.openConnection();

				httpsUrlconnection.setHostnameVerifier(DO_NOT_VERIFY);
				httpUrlconnection = httpsUrlconnection;
			}
			else if(urlString.startsWith( "http"))
			{
				httpUrlconnection = (HttpURLConnection)url.openConnection();
			}
			
			if(httpUrlconnection == null)
				return;

			httpUrlconnection.setAllowUserInteraction(false);
			httpUrlconnection.setUseCaches(false);

			switch(requestMethod )
			{
			case REQUEST_METHOD_GET:
				httpUrlconnection.setRequestMethod( "GET");
				break;

			case REQUEST_METHOD_POST:
				httpUrlconnection.setDoInput(true);
				httpUrlconnection.setDoOutput(true);
				httpUrlconnection.setRequestMethod( "POST");
				break;

			case REQUEST_METHOD_PUT:
				httpUrlconnection.setRequestMethod( "PUT");
				break;

			case REQUEST_METHOD_DELETE:
				httpUrlconnection.setRequestMethod( "DELETE");
				break;
			}

			Log.d( TAG + "sendRequest()" , "requestMethod = "+requestMethod);

			int retryTimeOut = 3;	
			int timeOut = CONTENT_TIME_OUT;
			while(retryTimeOut>0)
			{			
				try {
					httpUrlconnection.setConnectTimeout(timeOut);				
					httpUrlconnection.setReadTimeout(0x2bf20);
					setCommonHeaders(httpUrlconnection);
					httpUrlconnection.connect();
					Log.i( TAG +"RTA.NetworkUtility.sendRequest()", "Connect Successful");

					if( (requestMethod == REQUEST_METHOD_POST || requestMethod == REQUEST_METHOD_PUT )
							&& dataToSend != null )
					{
						outputstream = httpUrlconnection.getOutputStream();
						if(outputstream == null)
							return;

						Log.i( TAG +"RTA.NetworkUtility.sendRequest()","Sending following request: "+(dataToSend).toString());
						outputstream.write( dataToSend);
						outputstream.flush();
					}
					break;
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					timeOut *= 2;
					retryTimeOut--;
				}
			}
		} 
		finally
		{
			if(outputstream != null)  {  try { outputstream.close();  } catch(Exception exception) { }  }
		}
	}
	
	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/* Trust every server - don't check for any certificate */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager(){
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };
		
		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		if(httpUrlconnection != null)  {  try { httpUrlconnection.disconnect();  } catch(Exception exception) { }  }
	}
	
	public String sendRequest()
	{
		StringBuilder response 				= new StringBuilder();
		String errorMessage					= null;
		boolean success 					= false;
		String error						= null;
		InputStream is						= null;
		try
		{
			connect() ;

			Log.d( TAG + "sendRequest()", "Successfully sent request, waiting for response from server");
			final int responseCode  = httpUrlconnection.getResponseCode();
			Log.d( TAG + "sendRequest()", " Received response code:" +responseCode);

			boolean isResponseContentTypeCorrect = false;
			if( checkResponseContentType && expectedResponseContentType != null )
			{
				final String actualResponseContentType =  httpUrlconnection.getHeaderField( CONTENT_TYPE );
				Log.d( TAG + "sendRequest()",  "actualResponseContentType = " +actualResponseContentType);
				if( actualResponseContentType != null  && actualResponseContentType.contains(expectedResponseContentType)) 
				{
					isResponseContentTypeCorrect = true;
				}
			}
			else
			{
				isResponseContentTypeCorrect = true;
			}

			Log.d( TAG + "sendRequest()", "isResponseContentTypeCorrect = " +isResponseContentTypeCorrect);

			if (responseCode == HttpURLConnection.HTTP_OK) 
			{
				if( isResponseContentTypeCorrect ) 
				{
					is 		 = httpUrlconnection.getInputStream() ;
					response = readData(is) ;
					Log.d( "NetworkUtility", "NetworkUtility.sendRequest(): Received following response from server:" + response);
					success = true;
				}
				else
				{
					error 			= null;
					errorMessage 	= SERVER_ERROR;
				}
			}
			else
			{
				if(responseCode == HttpURLConnection.HTTP_FORBIDDEN){
					is 		 = httpUrlconnection.getErrorStream();
					response = readData(is) ;
					
					if(response != null){
						error = response.toString();
					}
					errorMessage = FORBIDDEN_NO_RESPONSE;

				}
				else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
					error 			= null;
					errorMessage 	= INVALID_CREDENTIALS;
				} 
				else
				{
					if( isResponseContentTypeCorrect ) 
					{
						is 				= httpUrlconnection.getErrorStream() ;
						error 			= readData(is).toString() ;
					}
					else
					{
						error = null;
					}
					errorMessage 	= SERVER_ERROR;
				}
			}

		} catch ( Exception e) {
			e.printStackTrace();
			Log.e("NetworkUtility.sendRequest()", "Error, could not send request to server due to: "+(e.getMessage()));
			//System.err.println((new StringBuilder("NetworkUtility.sendRequest(): Error, could not send request to server due to: ")).append(e.getMessage()).toString());
			errorMessage = NETWORK_ERROR;
		}
		finally
		{
			if( is != null ){ try { is.close(); }  catch ( Exception e){ }  }
			
			disconnect();
			
			if( listener != null)
			{
				if( success)
				{
					listener.onSuccess( null, response.toString());
				}
				else
				{
					Log.e("NetworkUtility.requestHTTPConnection(): ->", "respCode != 200");    
					listener.onFailure( errorMessage, error);
				}
			}
		}	
		return response.toString();
	}
	
	private StringBuilder readData(final InputStream is )
	{
		StringBuilder response 				= new StringBuilder();
		try
		{
			final char[] buffer 		= new char[5*1024];
			final Reader in 			= new InputStreamReader(is, "UTF-8");
			while(true)
			{
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				response.append(buffer, 0, rsz);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
		
	}
	public InputStream getStreamFromRequest()
    {
        InputStream response = null;
		try 
		{
			connect();
			
			final int responseCode  = httpUrlconnection.getResponseCode();
			Log.d(  TAG +"sendRequest()", "Received response code:"+responseCode);
			
			if (responseCode == HttpURLConnection.HTTP_OK) {
				response = httpUrlconnection.getInputStream();
				Log.d( "NetworkUtility", "NetworkUtility.sendRequest(): Received following response from server:" + response);
			}
			else {
				response = null;
			} 
		} catch ( Exception e) {
			e.printStackTrace();
			Log.e("NetworkUtility.sendRequest()", "Error, could not send request to server due to: "+(e.getMessage()).toString());
		}
		return response;
    }
	
	
	private void setCommonHeaders(final URLConnection httpurlconnection) {
		if( headerMap != null)
		{
			final Set<String> keys = headerMap.keySet();
			final Iterator<String> keysIterator = keys.iterator();
			while( keysIterator.hasNext())
			{
				final String key 	=  keysIterator.next();
				final String value 	= headerMap.get(key) ;
				httpurlconnection.setRequestProperty(key, value);
				Log.v( "NetworkUtility", "NetworkUtility.setCommonHeaders(): " +key+','+value) ;
			}
		}
	}

	public void setResponseContentType(final String responseContentType) {
		if( responseContentType != null)
		{
			this.expectedResponseContentType = responseContentType;
			this.checkResponseContentType = true; 
		}
	}
}
