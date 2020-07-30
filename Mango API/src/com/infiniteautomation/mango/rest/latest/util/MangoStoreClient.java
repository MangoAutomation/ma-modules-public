/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

import com.serotonin.io.StreamUtils;
import com.serotonin.web.http.HttpUtils4;

/**
 *
 * @author Terry Packer
 */
public class MangoStoreClient {

    private String storeUrl;
    private String sessionId;
    private HttpClient httpClient;

    public MangoStoreClient(String storeUrl){
        this.storeUrl = storeUrl;
    }

    public String getSessionId(){
        return this.sessionId;
    }

    public HttpClient getHttpClient(){
        return this.httpClient;
    }

    /**
     * Login to the store
     * @param email
     * @param password
     * @param retries
     * @throws ClientProtocolException
     * @throws IOException
     * @throws HttpException
     */
    public void login(String email, String password, int retries) throws ClientProtocolException, IOException, HttpException{

        BasicCookieStore cookieStore = new BasicCookieStore();
        httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        HttpPost httppost = new HttpPost(storeUrl + "/login");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("submit", "Login"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = executeRequest(httppost, 302, retries);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                for(Header header : response.getAllHeaders()){
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
            } finally {
                instream.close();
            }
        }
        return;
    }

    /**
     * Get a license token from the store
     * @param guid
     * @param distributor
     * @param retries
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public String getLicenseToken(String guid, String distributor, int retries) throws HttpException, IOException{
        StringBuilder baseUrl = new StringBuilder(storeUrl);

        baseUrl.append("/account/servlet/getDownloadToken?g=");
        baseUrl.append(guid);
        baseUrl.append("&d=");
        baseUrl.append(distributor);


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String uri = baseUrl.toString();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = executeRequest(request, HttpStatus.SC_OK, retries);
        InputStream in = response.getEntity().getContent();
        if (in == null)
            throw new IOException("Connection closed");

        StreamUtils.transfer(in, out);

        // Parse the response to get the token
        String tokenPairString = out.toString(StandardCharsets.UTF_8.name());
        String[] tokenPair = tokenPairString.split("=");
        if(tokenPair.length != 2)
            throw new IOException("Invalid token.");

        if("token".equals(tokenPair[0]))
            return tokenPair[1];
        else if("error".equals(tokenPair[0])){
            switch(tokenPair[1]){
                case "Unlicensed+instance":
                    throw new IOException("Unlicensed");
                case "Not+owner":
                    throw new IOException("Supplied user does not own this license");
            }
        }

        throw new IOException("Get license token failed for unknown reason");
    }

    /**
     * Get the license from the store using supplied token
     * @param token
     * @param retries
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public String getLicense(String token, int retries) throws IOException, HttpException{
        // Send the request
        String url = storeUrl + "/servlet/downloadLicense?token=" + token;
        HttpGet get = new HttpGet(url);
        HttpResponse response = executeRequest(get, 302, retries);
        String responseData = HttpUtils4.readResponseBody(response);

        // Should be an XML file. If it doesn't start with "<", it's an error.
        if (!responseData.startsWith("<")) {
            // Only log as info, because refreshes of a page where a previous download was successful will result in
            // an error being returned.
            throw new IOException("License download failed.");
        }else
            return responseData;
    }

    /**
     * Logout the current user
     * @param retries
     * @throws ClientProtocolException
     * @throws IOException
     * @throws HttpException
     */
    public void logout(int retries) throws ClientProtocolException, IOException, HttpException{
        String url = storeUrl + "/logout";
        HttpGet logout = new HttpGet(url);
        executeRequest(logout, 302, retries);
    }

    /**
     * Execute the response
     * @param request
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws HttpException
     */
    protected HttpResponse executeRequest(HttpRequestBase request, int expectedStatus, int retries) throws ClientProtocolException, IOException, HttpException{
        HttpResponse response = null;
        while (true) {
            try {
                response = httpClient.execute(request);
                if(response != null && response.getStatusLine().getStatusCode() == 308) { //Not in HttpStatus TODO
                    try {
                        String location = response.getLastHeader("Location").getValue();
                        String secureStore = storeUrl.replaceFirst("^http://", "https://");
                        request.setURI(new URI(location));
                        if(location.startsWith(secureStore))
                            storeUrl = secureStore;
                        continue;
                    } catch(URISyntaxException e) {
                        throw new HttpException("Syntax exception in returned location from redirect to: " + response.getLastHeader("Location").getValue());
                    }
                }
                break;
            }
            catch (SocketTimeoutException e) {
                if (retries <= 0)
                    throw e;
                retries--;
            }
        }
        if(response == null)
            throw new IOException("Connection failed");

        if ((response.getStatusLine().getStatusCode() == expectedStatus)||(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK))
            return response;
        else
            throw new HttpException("Invalid response code " + response.getStatusLine().getStatusCode()
                    + ", reason=" + response.getStatusLine().getReasonPhrase() + " for uri " + request.getURI().toString());

    }

}
