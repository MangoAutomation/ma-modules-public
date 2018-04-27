/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.edit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.serotonin.io.StreamUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.gviews.GraphicalView;
import com.serotonin.m2m2.gviews.GraphicalViewUploadPermissionDefinition;
import com.serotonin.m2m2.gviews.GraphicalViewsCommon;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;

public class ImageUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (ServletFileUpload.isMultipartContent(request)) {
            User user = Common.getUser(request);
            GraphicalView view = GraphicalViewsCommon.getUserEditView(user);

            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

            //Fail if we don't have permissions for this
            if(!Permissions.hasPermission(user, SystemSettingsDao.instance.getValue(GraphicalViewUploadPermissionDefinition.PERMISSION))){
            	//The GraphicalViewDwr.clearBackground() method will notify the user of a failure so we can ignore them here
            	return;
            }
            
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
                    
                    try{
                    	//Test the file for Image of type: BMP, GIF, JPG or PNG
                    	// will throw IOException if not supported or null if not an image
                    	if(ImageIO.read(diskItem.getInputStream()) != null){
	                        // Create the path to the upload directory.
	                        File dir = GraphicalViewsCommon.getUploadDir();
	
	                        // Create the image file name.
	                        String filename = GraphicalViewsCommon.getNextImageFilename(dir, diskItem.getName());
	
	                        // Save the file.
	                        FileOutputStream fos = new FileOutputStream(new File(dir, filename));
	                        StreamUtils.transfer(diskItem.getInputStream(), fos);
	                        fos.close();
	
	                        view.setBackgroundFilename(ImageUploadServletDefinition.IMAGE_DIR + "/" + filename);
                    	}else{
                    		//Unsupported File Type
                    	}
                    }catch(Exception e){
                    	//Unsupported Image Type
                    }

                }
            }
        }
    }
}
