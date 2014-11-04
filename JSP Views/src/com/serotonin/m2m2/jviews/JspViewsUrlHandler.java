/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.jviews;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.ResourceNotFoundException;

/**
 * @author Terry Packer
 *
 */
public class JspViewsUrlHandler implements UrlHandler{
	
	private String webBase;
	private String modulePath;
	
	public JspViewsUrlHandler(String modulePath, String webBase){
		this.modulePath = Common.MA_HOME + modulePath + "/web/";
		this.webBase = webBase + "/web/";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.UrlHandler#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Map)
	 */
	@Override
	public View handleRequest(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> model)
			throws Exception {
		
		//Here we would map to the pages by loading the view
		String contextPath = request.getRequestURI();
		//Check the page for existence
		String[] extensionParts = contextPath.split(".shtm");
		String jspFile = extensionParts[0] + ".jsp";
		String[] parts = jspFile.split(JspViewsUriMappingDefinition.urlBase);
		
		File page = new File(modulePath + parts[1]);
		if(page.exists()){
			 request.getRequestDispatcher(webBase + parts[1]).forward(request, response);
			return null; //This forces the use of the jsp path, wich doesn't exist but doesn't seem to matter.  This should be fixed but not sure how
		}else{
			//Return a 404
			throw new ResourceNotFoundException(page.getName());
		}

	}

	
    static class JspPageView implements View {
        private final String content;

        public JspPageView(String content) {
            this.content = content;
        }

        @Override
        public String getContentType() {
            return "text/html";
        }

        @Override
        public void render(@SuppressWarnings("rawtypes") Map model, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            response.getWriter().write(content);
        }
    }
    
    
    static class DashboardJavascriptResourceView implements View {
        private final String content;

        public DashboardJavascriptResourceView(String content) {
            this.content = content;
        }

        @Override
        public String getContentType() {
            return "application/javascript";
        }

        @Override
        public void render(@SuppressWarnings("rawtypes") Map model, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            response.getWriter().write(content);
        }
    }
}
