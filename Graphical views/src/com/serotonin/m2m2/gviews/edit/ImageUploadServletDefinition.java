/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.edit;

import javax.servlet.http.HttpServlet;

import com.serotonin.m2m2.Constants;
import com.serotonin.m2m2.module.ServletDefinition;

public class ImageUploadServletDefinition extends ServletDefinition {
    public static String IMAGE_DIR_NAME = "graphicalViewUploads";
    public static String IMAGE_DIR;
    public static String UPLOAD_DIR;

    @Override
    public HttpServlet getServlet() {
        return new ImageUploadServlet();
    }

    @Override
    public String getUriPattern() {
        return "/graphicalViewsBackgroundUpload";
    }

    @Override
    public void preInitialize(boolean install, boolean upgrade) {
        IMAGE_DIR = "/" + Constants.DIR_MODULES + "/" + getModule().getName() + "/" + Constants.DIR_WEB + "/" + IMAGE_DIR_NAME;
        UPLOAD_DIR = getModule().modulePath().resolve(Constants.DIR_WEB).resolve(IMAGE_DIR_NAME).toAbsolutePath().toString();
    }
}
