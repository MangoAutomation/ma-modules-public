/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.io.File;
import java.util.List;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.gviews.component.ViewComponent;
import com.serotonin.m2m2.gviews.edit.ImageUploadServletDefinition;
import com.serotonin.m2m2.module.DatabaseSchemaDefinition;
import com.serotonin.util.DirectoryUtils;

public class GraphicalViewsSchemaDefinition extends DatabaseSchemaDefinition {
    static {
        Common.JSON_CONTEXT.addResolver(new ViewComponent.Resolver(), ViewComponent.class);
    }

    @Override
    public String getNewInstallationCheckTableName() {
        return "graphicalViews";
    }

    @Override
    public void addConversionTableNames(List<String> tableNames) {
        tableNames.add("graphicalViews");
    }

    @Override
    public String getUpgradePackage() {
        return "com.serotonin.m2m2.gviews.upgrade";
    }

    @Override
    public void postRuntimeManagerTerminate(boolean uninstall) {
        super.postRuntimeManagerTerminate(uninstall);
        // Remove the background image upload directory.
        if(uninstall) {
            File uploadDir = new File(ImageUploadServletDefinition.UPLOAD_DIR);
            if(uploadDir.exists()) {
                DirectoryUtils.deleteDirectory(uploadDir);
            }
        }
    }

    @Override
    public int getDatabaseSchemaVersion() {
        return 3;
    }
}
