/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

/**
 * 
 * @author Terry Packer
 */
public class MangoStoreClient {

	private String storeUrl;
	private String storeLoginUrl;
	private String sessionId;
	
	public MangoStoreClient(String storeUrl){
		this.storeUrl = storeUrl;
		this.storeLoginUrl = storeUrl + "/login";
	}
	
	public String getSessionId(){
		return this.sessionId;
	}
	
	public HttpClient login(String email, String password) throws ClientProtocolException, IOException{
		
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpClient httpclient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

		HttpPost httppost = new HttpPost(storeLoginUrl);

		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("submit", "Login"));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		//Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
		    InputStream instream = entity.getContent();
		    try {
		        // do something useful
		    	if(response.getStatusLine().getStatusCode() > 399){
		    		throw new IOException("Store Login failed: " + response.getStatusLine().getReasonPhrase());
		    	}else{
		    		for(Header header : response.getAllHeaders()){
		    			System.out.println(header.getName() + "-->" + header.getValue());
		    			if(header.getName().equals("Set-Cookie")){
		    				//Set our cookie here
		    				String[] cookiePath = header.getValue().split(";");
		    				//Now we have JSESSIONID=stuff , Path=/
		    				String[] cookie = cookiePath[0].split("=");
		    				String[] path = cookiePath[1].split("=");
		    				this.sessionId = cookie[1];
		    				BasicClientCookie c = new BasicClientCookie(cookie[0], cookie[1]);
		    				c.setDomain(storeUrl);
		    				c.setPath(path[1]);
		    				c.setVersion(0);
		    				cookieStore.addCookie(c);	
		    			}
		    		}
		    	}
		    } finally {
		        instream.close();
		    }
		}
		return httpclient;
	}
}
