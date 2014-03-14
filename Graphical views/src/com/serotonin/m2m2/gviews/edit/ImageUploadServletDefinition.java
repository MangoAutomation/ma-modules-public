/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.edit;

import javax.servlet.http.HttpServlet;

import com.serotonin.m2m2.Common;
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
    public void preInitialize() {
        IMAGE_DIR = getModule().getWebPath() + "/" + Constants.DIR_WEB + "/" + IMAGE_DIR_NAME;
        UPLOAD_DIR = Common.M2M2_HOME + getModule().getDirectoryPath() + "/" + Constants.DIR_WEB + "/" + IMAGE_DIR_NAME;
    }
}
