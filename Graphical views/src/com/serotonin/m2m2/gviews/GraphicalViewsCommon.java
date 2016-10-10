/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.directwebremoting.WebContextFactory;

import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.gviews.edit.ImageUploadServletDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;

public class GraphicalViewsCommon {
    private static final String VIEW_KEY = GraphicalViewsCommon.class + ".view";
    private static final String EDIT_VIEW_KEY = GraphicalViewsCommon.class + ".editView";
    private static final String ANON_VIEW_KEY = GraphicalViewsCommon.class + ".anonymousViews";

    @SuppressWarnings("unchecked")
    public static List<ViewComponentState> getGraphicalViewListStates(LongPollData data) {
        List<ViewComponentState> states = (List<ViewComponentState>) data.getState()
                .getAttribute("viewComponentStates");
        if (states == null) {
            synchronized (data) {
                states = (List<ViewComponentState>) data.getState().getAttribute("viewComponentStates");
                if (states == null) {
                    states = new ArrayList<ViewComponentState>();
                    data.getState().setAttribute("viewComponentStates", states);
                }
            }
        }
        return states;
    }

    public static void setGraphicalViewListStates(LongPollData data, List<ViewComponentState> states) {
        data.getState().setAttribute("viewComponentStates", states);
    }

    public static void ensureViewPermission(User user, GraphicalView view) throws PermissionException {
        if(!view.isReader(user)&&!view.isSetter(user))
            throw new PermissionException("User does not have permission to the view", user);
    }

    public static void ensureViewEditPermission(User user, GraphicalView view) throws PermissionException {
        if (!view.isEditor(user))
            throw new PermissionException("User does not have permission to edit the view", user);
    }
    
    public static void ensureCanCreate(User user) {
    	if(user == null || !Permissions.hasPermission(user, SystemSettingsDao.getValue(GraphicalViewAddViewPermissionDefinition.PERMISSION)))
    		throw new PermissionException("User does not have permission to create new views", user);
    }

    //
    // Anonymous views
    public static GraphicalView getAnonymousViewDwr(int id) {
        return getAnonymousView(WebContextFactory.get().getHttpServletRequest(), id);
    }

    public static GraphicalView getAnonymousView(HttpServletRequest request, int id) {
        List<GraphicalView> views = getAnonymousViews(request);
        if (views == null)
            return null;
        for (GraphicalView view : views) {
            if (view.getId() == id)
                return view;
        }
        return null;
    }

    public static void addAnonymousView(HttpServletRequest request, GraphicalView view) {
        List<GraphicalView> views = getAnonymousViews(request);
        if (views == null) {
            views = new ArrayList<GraphicalView>();
            request.getSession().setAttribute(ANON_VIEW_KEY, views);
        }
        // Remove the view if it already exists.
        for (int i = views.size() - 1; i >= 0; i--) {
            if (views.get(i).getId() == view.getId())
                views.remove(i);
        }
        views.add(view);
    }

    @SuppressWarnings("unchecked")
    private static List<GraphicalView> getAnonymousViews(HttpServletRequest request) {
        return (List<GraphicalView>) request.getSession().getAttribute(ANON_VIEW_KEY);
    }

    //
    // User view
    public static void setUserView(User user, GraphicalView view) {
        user.setAttribute(VIEW_KEY, view);
    }

    public static GraphicalView getUserView(User user) {
        return (GraphicalView) user.getAttribute(VIEW_KEY);
    }

    //
    // User edit view
    public static void setUserEditView(User user, GraphicalView view) {
        user.setAttribute(EDIT_VIEW_KEY, view);
    }

    public static GraphicalView getUserEditView(User user) {
        return (GraphicalView) user.getAttribute(EDIT_VIEW_KEY);
    }

    //
    // Image stuff
    private static int nextImageId = -1;

    public static String copyImage(String oldFilename) throws IOException {
        if (oldFilename == null)
            return null;

        File uploadDir = getUploadDir();
        if (oldFilename.startsWith(ImageUploadServletDefinition.UPLOAD_DIR))
            oldFilename = oldFilename.substring(ImageUploadServletDefinition.UPLOAD_DIR.length());
        String newFilename = getNextImageFilename(uploadDir, oldFilename);

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(new File(uploadDir, oldFilename));
            fos = new FileOutputStream(new File(uploadDir, newFilename));
            IOUtils.copy(fis, fos);
        }
        catch (FileNotFoundException e) {
            // The old file was not found.
            return null;
        }
        finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }

        return ImageUploadServletDefinition.UPLOAD_DIR + newFilename;
    }

    public static File getUploadDir() {
        // Make sure the directory exists.
        File dir = new File(ImageUploadServletDefinition.UPLOAD_DIR);
        dir.mkdirs();

        return dir;
    }

    public static String getNextImageFilename(File uploadDir, String originalFilename) {
        // Get an image id.
        int imageId = getNextImageId(uploadDir);

        // Create the image file name.
        String filename = Integer.toString(imageId);
        int dot = originalFilename.lastIndexOf('.');
        if (dot != -1)
            filename += originalFilename.substring(dot);

        return filename;
    }

    private static int getNextImageId(File uploadDir) {
        if (nextImageId == -1) {
            // Synchronize
            synchronized (ImageUploadServletDefinition.IMAGE_DIR) {
                if (nextImageId == -1) {
                    // Initialize the next image id field.
                    nextImageId = 1;

                    String[] names = uploadDir.list();
                    int index, dot;
                    for (int i = 0; i < names.length; i++) {
                        dot = names[i].lastIndexOf('.');
                        try {
                            if (dot == -1)
                                index = Integer.parseInt(names[i]);
                            else
                                index = Integer.parseInt(names[i].substring(0, dot));
                            if (index >= nextImageId)
                                nextImageId = index + 1;
                        }
                        catch (NumberFormatException e) { /* no op */
                        }
                    }
                }
            }
        }
        return nextImageId++;
    }

    public static void deleteImage(String filename) {
        if (filename == null)
            return;

        File uploadDir = getUploadDir();
        if (filename.startsWith(ImageUploadServletDefinition.UPLOAD_DIR))
            filename = filename.substring(ImageUploadServletDefinition.UPLOAD_DIR.length());

        new File(uploadDir, filename).delete();
    }
}
