/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.infiniteautomation.mangoApi.MangoApiModuleDefinition;

/**
 * Filter to pull in custom headers and apply to all API Responses
 * 
 * @author Terry Packer
 *
 */
public class CustomHeadersFilter implements Filter{
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException { }

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		//Using our reloading properties we will add any additional headers to the response
		Properties headers = MangoApiModuleDefinition.props.getPropertiesCopy();
		Iterator<Object> it = headers.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			httpResponse.setHeader(key, headers.getProperty(key));
		}
		
		chain.doFilter(request, response);
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() { }

}
