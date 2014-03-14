/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.edit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.serotonin.io.StreamUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.gviews.GraphicalView;
import com.serotonin.m2m2.gviews.GraphicalViewsCommon;
import com.serotonin.m2m2.vo.User;

public class ImageUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (ServletFileUpload.isMultipartContent(request)) {
            User user = Common.getUser(request);
            GraphicalView view = GraphicalViewsCommon.getUserEditView(user);

            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

            List<FileItem> items;
            try {
                items = upload.parseRequest(request);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            for (FileItem item : items) {
                if ("backgroundImage".equals(item.getFieldName())) {
                    final DiskFileItem diskItem = (DiskFileItem) item;

                    // Create the path to the upload directory.
                    File dir = GraphicalViewsCommon.getUploadDir();

                    // Create the image file name.
                    String filename = GraphicalViewsCommon.getNextImageFilename(dir, diskItem.getName());

                    // Save the file.
                    FileOutputStream fos = new FileOutputStream(new File(dir, filename));
                    StreamUtils.transfer(diskItem.getInputStream(), fos);
                    fos.close();

                    view.setBackgroundFilename(ImageUploadServletDefinition.IMAGE_DIR + "/" + filename);
                }
            }
        }
    }
}
