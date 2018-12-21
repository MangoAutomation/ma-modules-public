/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.gviews.component.AnalogGraphicComponent;
import com.serotonin.m2m2.gviews.component.BinaryGraphicComponent;
import com.serotonin.m2m2.gviews.component.CompoundChild;
import com.serotonin.m2m2.gviews.component.CompoundComponent;
import com.serotonin.m2m2.gviews.component.DynamicGraphicComponent;
import com.serotonin.m2m2.gviews.component.HtmlComponent;
import com.serotonin.m2m2.gviews.component.ImageChartComponent;
import com.serotonin.m2m2.gviews.component.MultistateGraphicComponent;
import com.serotonin.m2m2.gviews.component.PointComponent;
import com.serotonin.m2m2.gviews.component.ScriptComponent;
import com.serotonin.m2m2.gviews.component.SimpleCompoundComponent;
import com.serotonin.m2m2.gviews.component.SimplePointComponent;
import com.serotonin.m2m2.gviews.component.ThumbnailComponent;
import com.serotonin.m2m2.gviews.component.ViewComponent;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.DynamicImage;
import com.serotonin.m2m2.view.ImageSet;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.AnonymousUser;
import com.serotonin.m2m2.vo.DataPointExtendedNameComparator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.beans.DataPointBean;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author mlohbihler
 */
public class GraphicalViewDwr extends ModuleDwr {
	
	private static final int pointEventsLimit = 10; //10 most recent events
	
    //
    //
    // Anonymous views
    //
    public List<ViewComponentState> getViewPointDataAnon(int viewId) {
        GraphicalView view = GraphicalViewsCommon.getAnonymousViewDwr(viewId);
        if (view == null)
            return new ArrayList<ViewComponentState>();
        return getViewPointData(null, view, false);
    }

    @DwrPermission(anonymous = true)
    public String setViewPointAnon(int viewId, String viewComponentId, String valueStr) {
        GraphicalView view = GraphicalViewsCommon.getAnonymousViewDwr(viewId);
        if (view == null)
            throw new PermissionException(new TranslatableMessage("common.default", "View is not in session"), null);

        if (view.getAnonymousAccess() != ShareUser.ACCESS_SET)
            throw new PermissionException(new TranslatableMessage("common.default", "Point is not anonymously settable"), null);

        // Allow the set.
        setPointImpl(view.findDataPoint(viewComponentId), valueStr, new AnonymousUser());

        return viewComponentId;
    }
    

    /**
     * Retrieves point state for all points on a given view. This is the monitoring version of the method. See below for
     * the view editing version.
     * 
     * @param viewId
     * @return
     */
    public List<ViewComponentState> getViewPointData(boolean edit) {
        User user = Common.getUser();

        GraphicalView view;
        if (edit)
            view = GraphicalViewsCommon.getUserEditView(user);
        else
            view = GraphicalViewsCommon.getUserView(user);

        return getViewPointData(user, view, edit);
    }

    private List<ViewComponentState> getViewPointData(User user, GraphicalView view, boolean edit) {
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        List<ViewComponentState> states = new ArrayList<ViewComponentState>();
        Map<String, Object> model = new HashMap<String, Object>();

        for (ViewComponent viewComponent : view.getViewComponents()) {
        	
        	//Are we to update this component
            boolean update = System.currentTimeMillis() >= (viewComponent.getLastUpdated() + Common.getMillis(viewComponent.getUpdatePeriodType(), viewComponent.getUpdatePeriods()));
            
            if (viewComponent.isCompoundComponent() && (edit || viewComponent.isVisible())) {
                CompoundComponent compoundComponent = (CompoundComponent) viewComponent;

                boolean imageChart = compoundComponent instanceof ImageChartComponent;
                model.put("sessionUser", user);

                // Add states for each of the children
                for (CompoundChild child : compoundComponent.getChildComponents())
                    addPointComponentState(child.getViewComponent(), update, Common.runtimeManager, model, request, view, user,
                            states, edit, !imageChart);

                // Add a state for the compound component.
                ViewComponentState state = new ViewComponentState();
                state.setId(compoundComponent.getId());

                model.clear();
                model.put("sessionUser", user);
                model.put("compoundComponent", compoundComponent);

                List<Map<String, Object>> childData = new ArrayList<Map<String, Object>>();
                for (CompoundChild child : compoundComponent.getChildComponents()) {
                    if (child.getViewComponent().isPointComponent()) {
                        DataPointVO point = ((PointComponent) child.getViewComponent()).tgetDataPoint();
                        if (point != null) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            if (imageChart)
                                map.put("name", point.getName());
                            else
                                map.put("name", translate(child.getDescription()));
                            map.put("point", point);
                            map.put("pointValue", point.lastValue());
                            childData.add(map);
                        }
                    }
                }
                model.put("childData", childData);

                if (compoundComponent.hasInfo())
                    state.setInfo(generateViewContent(compoundComponent, update, request, "compoundInfoContent.jsp", model));

                //Check to see if we need to update it...
                
                if (imageChart){
                    state.setContent(((ImageChartComponent) compoundComponent).getImageChartData(getTranslations()));
                }else if (!edit){
                    state.setChart(compoundComponent.getImageChartData(getTranslations()));
                }

                states.add(state);
            }
            else
                addPointComponentState(viewComponent, update, Common.runtimeManager, model, request, view, user, states, edit, true);
        
            //Save the last time we updated
            if(update)
            	viewComponent.setLastUpdated(System.currentTimeMillis());

        }

        return states;
    }

    private void addPointComponentState(ViewComponent viewComponent, boolean update, RuntimeManager rtm, Map<String, Object> model,
            HttpServletRequest request, GraphicalView view, User user, List<ViewComponentState> states, boolean edit,
            boolean add) {
        if (viewComponent.isPointComponent() && (edit || viewComponent.isVisible())) {
            PointComponent pointComponent = (PointComponent) viewComponent;

            DataPointRT dataPointRT = null;
            if (pointComponent.tgetDataPoint() != null)
                dataPointRT = rtm.getDataPoint(pointComponent.tgetDataPoint().getId());

            ViewComponentState state = preparePointComponentState(pointComponent, update, user, dataPointRT, model, request);

            if (!edit) {
                if (pointComponent.isSettable()) {
                	if(view.isEditor(user) || view.isSetter(user))
                        setChange(pointComponent.tgetDataPoint(), state, dataPointRT, request, model);
                }

                if (pointComponent.tgetDataPoint() != null)
                    setChart(pointComponent.tgetDataPoint(), state, request, model);
            }

            if (add)
                states.add(state);
            
            model.clear();
        }
    }

    /**
     * Shared convenience method for creating a populated view component state.
     */
    private ViewComponentState preparePointComponentState(PointComponent pointComponent, boolean update, User user, DataPointRT point,
            Map<String, Object> model, HttpServletRequest request) {
        ViewComponentState state = new ViewComponentState();
        state.setId(pointComponent.getId());

        PointValueTime pointValue = prepareBasePointState(pointComponent.getId(), state,
                pointComponent.tgetDataPoint(), point, model);
        
        model.put("pointComponent", pointComponent);
        if (pointComponent.isValid()){
        	if(!update && pointComponent.getCachedContent(MODEL_ATTR_EVENTS) != null)
        		model.put(MODEL_ATTR_EVENTS, pointComponent.getCachedContent(MODEL_ATTR_EVENTS));
        	else{
        		setEvents(pointComponent.tgetDataPoint(), user, model, pointEventsLimit);
        		pointComponent.putCachedContent(MODEL_ATTR_EVENTS, model.get(MODEL_ATTR_EVENTS));
        	}
        }

        pointComponent.addDataToModel(model, pointValue);

        if (!pointComponent.isValid())
            model.put("invalid", "true");
        else {
            // Add the rendered text as a convenience to the snippets.
            model.put("text", pointComponent.tgetDataPoint().getTextRenderer().getText(pointValue, TextRenderer.HINT_FULL));
            state.setContent(generateViewContent(pointComponent, update, request, pointComponent.snippetName() + ".jsp", model));
            pointComponent.tgetDataPoint().updateLastValue(pointValue);
        }

        state.setInfo(generateViewContent(pointComponent, update, request, "infoContent.jsp", model));
        
        //TODO Cache this
        setMessages(state, request, getFullSnippetName("warningContent.jsp"), model);

        return state;
    }

    //
    //
    // View editing
    //
    @DwrPermission(user = true)
    public Map<String, Object> editInit() {
        Map<String, Object> result = new HashMap<String, Object>();
        User user = Common.getUser();

        // Users with which to share.
        //result.put("shareUsers", getShareUsers(user));

        // Users already sharing with.
        //Legacy code, can remove result.put("viewUsers", GraphicalViewsCommon.getUserEditView(user).getViewUsers());

        // View component types
        List<StringStringPair> components = new ArrayList<StringStringPair>();
        for (ImplDefinition impl : ViewComponent.getImplementations())
            components.add(new StringStringPair(impl.getName(), translate(impl.getNameKey())));
        result.put("componentTypes", components);

        // Available points
        List<DataPointVO> allPoints = DataPointDao.getInstance().getDataPoints(DataPointExtendedNameComparator.instance, false);
        List<DataPointBean> availablePoints = new ArrayList<DataPointBean>();
        final boolean admin = Permissions.hasAdminPermission(user);
        for (DataPointVO dataPoint : allPoints) {
            if (admin || Permissions.hasDataPointReadPermission(user, dataPoint))
                availablePoints.add(new DataPointBean(dataPoint));
        }
        result.put("pointList", availablePoints);

        return result;
    }

    @DwrPermission(user = true)
    public ViewComponent addComponent(String componentName) {
        ViewComponent viewComponent = ViewComponent.newInstance(componentName);

        User user = Common.getUser();
        GraphicalView view = GraphicalViewsCommon.getUserEditView(user);
        view.addViewComponent(viewComponent);
        viewComponent.validateDataPoint(user, false);

        return viewComponent;
    }

    @DwrPermission(user = true)
    public void setViewComponentLocation(String viewComponentId, float x, float y) {
        getViewComponent(viewComponentId).setLocation((int)x, (int)y);
    }

    @DwrPermission(user = true)
    public void deleteViewComponent(String viewComponentId) {
        GraphicalView view = GraphicalViewsCommon.getUserEditView(Common.getUser());
        view.removeViewComponent(getViewComponent(view, viewComponentId));
    }

    @DwrPermission(user = true)
    public ProcessResult setPointComponentSettings(String pointComponentId, int dataPointId, String name,
            boolean settable, String bkgdColorOverride, int updatePeriodType, int updatePeriods, boolean displayControls, int x, int y) {
        ProcessResult response = new ProcessResult();
        PointComponent pc = (PointComponent) getViewComponent(pointComponentId);
        User user = Common.getUser();

        DataPointVO dp = DataPointDao.getInstance().getDataPoint(dataPointId);
        if (dp == null || !Permissions.hasDataPointReadPermission(user, dp))
            response.addContextualMessage("settingsPointInfo", "validate.required");

        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType))
            response.addContextualMessage("settingsUpdatePeriodType", "validate.invalidValue");
        if (updatePeriods < 0)
            response.addContextualMessage("settingsUpdatePeriods", "validate.cannotBeNegative");
        
        if (x < 0)
            response.addContextualMessage("settingsX", "validate.cannotBeNegative");
        if (y < 0)
            response.addContextualMessage("settingsY", "validate.cannotBeNegative");

        if (!response.getHasMessages()) {
            pc.tsetDataPoint(dp);
            pc.setNameOverride(name);
            pc.setSettableOverride(settable && Permissions.hasDataPointSetPermission(user, dp));
            pc.setBkgdColorOverride(bkgdColorOverride);
            pc.setUpdatePeriodType(updatePeriodType);
            pc.setUpdatePeriods(updatePeriods);
            pc.setDisplayControls(displayControls);
            pc.setLocation(x, y);

            pc.validateDataPoint(user, false);

            response.addData("x", x);
            response.addData("y", y);
        }

        return response;
    }

    @DwrPermission(user = true)
    public List<String> getViewComponentIds() {
        User user = Common.getUser();
        List<String> result = new ArrayList<String>();
        for (ViewComponent vc : GraphicalViewsCommon.getUserEditView(user).getViewComponents())
            result.add(vc.getId());
        return result;
    }

    /**
     * Allows the setting of a given data point. Overrides BaseDwr to resolve the point view id.
     * 
     * @param pointId
     * @param valueStr
     * @return
     */
    @DwrPermission(user = true)
    public String setViewPoint(String viewComponentId, String valueStr) {
        User user = Common.getUser();
        GraphicalView view = GraphicalViewsCommon.getUserView(user);
        DataPointVO point = view.findDataPoint(viewComponentId);

        if (point != null) {
            // Check that setting is allowed.
        	if(!view.isSetter(user))
                throw new PermissionException(new TranslatableMessage("permission.exception.setDataPoint", user.getUsername()), user);

            // Try setting the point.
            setPointImpl(point, valueStr, user);
        }

        return viewComponentId;
    }

    //
    // Save view component
    //
    @DwrPermission(user = true)
    public void saveHtmlComponent(String viewComponentId, String content) {
        HtmlComponent c = (HtmlComponent) getViewComponent(viewComponentId);
        c.setContent(content);
    }

    @DwrPermission(user = true)
    public ProcessResult saveAnalogGraphicComponent(String viewComponentId, double min, double max,
            boolean displayText, String imageSetId) {
        ProcessResult response = new ProcessResult();

        // Validate
        if (min >= max)
            response.addContextualMessage("graphicRendererAnalogMin", "viewEdit.graphic.invalidMinMax");

        ImageSet imageSet = getImageSet(imageSetId);
        if (imageSet == null)
            response.addContextualMessage("graphicRendererAnalogImageSet", "viewEdit.graphic.missingImageSet");

        if (!response.getHasMessages()) {
            AnalogGraphicComponent c = (AnalogGraphicComponent) getViewComponent(viewComponentId);
            c.setMin(min);
            c.setMax(max);
            c.setDisplayText(displayText);
            c.tsetImageSet(imageSet);
            resetPointComponent(c);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveBinaryGraphicComponent(String viewComponentId, int zeroImage, int oneImage,
            boolean displayText, String imageSetId) {
        ProcessResult response = new ProcessResult();

        // Validate
        ImageSet imageSet = getImageSet(imageSetId);
        if (imageSet == null)
            response.addContextualMessage("graphicRendererBinaryImageSet", "viewEdit.graphic.missingImageSet");
        else {
            if (zeroImage == -1)
                response.addContextualMessage("graphicRendererBinaryImageSetZeroMsg",
                        "viewEdit.graphic.missingZeroImage");
            if (oneImage == -1)
                response.addContextualMessage("graphicRendererBinaryImageSetOneMsg", "viewEdit.graphic.missingOneImage");
        }

        if (!response.getHasMessages()) {
            BinaryGraphicComponent c = (BinaryGraphicComponent) getViewComponent(viewComponentId);
            c.tsetImageSet(imageSet);
            c.setZeroImage(zeroImage);
            c.setOneImage(oneImage);
            c.setDisplayText(displayText);
            resetPointComponent(c);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveDynamicGraphicComponent(String viewComponentId, double min, double max,
            boolean displayText, String dynamicImageId) {
        ProcessResult response = new ProcessResult();

        // Validate
        if (min >= max)
            response.addContextualMessage("graphicRendererDynamicMin", "viewEdit.graphic.invalidMinMax");

        DynamicImage dynamicImage = getDynamicImage(dynamicImageId);
        if (dynamicImage == null)
            response.addContextualMessage("graphicRendererDynamicImage", "viewEdit.graphic.missingDynamicImage");

        if (!response.getHasMessages()) {
            DynamicGraphicComponent c = (DynamicGraphicComponent) getViewComponent(viewComponentId);
            c.setMin(min);
            c.setMax(max);
            c.setDisplayText(displayText);
            c.tsetDynamicImage(dynamicImage);
            resetPointComponent(c);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveMultistateGraphicComponent(String viewComponentId, List<IntStringPair> imageStates,
            int defaultImage, boolean displayText, String imageSetId) {
        ProcessResult response = new ProcessResult();

        // Validate
        ImageSet imageSet = getImageSet(imageSetId);
        if (imageSet == null)
            response.addContextualMessage("graphicRendererMultistateImageSet", "viewEdit.graphic.missingImageSet");

        if (!response.getHasMessages()) {
            MultistateGraphicComponent c = (MultistateGraphicComponent) getViewComponent(viewComponentId);
            c.setImageStateList(imageStates);
            c.setDefaultImage(defaultImage);
            c.setDisplayText(displayText);
            c.tsetImageSet(imageSet);
            resetPointComponent(c);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveScriptComponent(String viewComponentId, String script) {
        ProcessResult response = new ProcessResult();

        // Validate
        if (StringUtils.isBlank(script))
            response.addContextualMessage("graphicRendererScriptScript", "viewEdit.graphic.missingScript");

        if (!response.getHasMessages()) {
            ScriptComponent c = (ScriptComponent) getViewComponent(viewComponentId);
            c.setScript(script);
            resetPointComponent(c);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveSimplePointComponent(String viewComponentId, boolean displayPointName,
            String styleAttribute) {
        SimplePointComponent c = (SimplePointComponent) getViewComponent(viewComponentId);
        c.setDisplayPointName(displayPointName);
        c.setStyleAttribute(styleAttribute);
        resetPointComponent(c);

        return new ProcessResult();
    }

    @DwrPermission(user = true)
    public ProcessResult saveThumbnailComponent(String viewComponentId, int scalePercent) {
        ProcessResult response = new ProcessResult();

        // Validate
        if (scalePercent < 1)
            response.addContextualMessage("graphicRendererThumbnailScalePercent", "viewEdit.graphic.invalidScale");

        if (!response.getHasMessages()) {
            ThumbnailComponent c = (ThumbnailComponent) getViewComponent(viewComponentId);
            c.setScalePercent(scalePercent);
            resetPointComponent(c);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveSimpleCompoundComponent(String viewComponentId, String name, String backgroundColour,
            List<StringStringPair> childPointIds) {
        ProcessResult response = new ProcessResult();

        validateCompoundComponent(response, name);

        String leadPointId = null;
        for (StringStringPair kvp : childPointIds) {
            if (SimpleCompoundComponent.LEAD_POINT.equals(kvp.getKey())) {
                leadPointId = kvp.getValue();
                break;
            }
        }

        if (NumberUtils.toInt(leadPointId, 0) <= 0)
            response.addContextualMessage("compoundPointSelect" + SimpleCompoundComponent.LEAD_POINT,
                    "dsEdit.validate.required");

        if (!response.getHasMessages()) {
            SimpleCompoundComponent c = (SimpleCompoundComponent) getViewComponent(viewComponentId);
            c.setName(name);
            c.setBackgroundColour(backgroundColour);
            saveCompoundPoints(c, childPointIds);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveImageChartComponent(String viewComponentId, String name, int width, int height,
            int durationType, int durationPeriods, int updatePeriodType, int updatePeriods, List<StringStringPair> childPointIds) {
        ProcessResult response = new ProcessResult();

        validateCompoundComponent(response, name);
        if (width < 1)
            response.addContextualMessage("imageChartWidth", "validate.greaterThanZero");
        if (height < 1)
            response.addContextualMessage("imageChartHeight", "validate.greaterThanZero");
        if (!Common.TIME_PERIOD_CODES.isValidId(durationType))
            response.addContextualMessage("imageChartDurationType", "validate.invalidValue");
        if (durationPeriods <= 0)
            response.addContextualMessage("imageChartDurationPeriods", "validate.greaterThanZero");
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType))
            response.addContextualMessage("imageChartUpdatePeriodType", "validate.invalidValue");
        if (updatePeriods < 0)
            response.addContextualMessage("imageChartUpdatePeriods", "validate.cannotBeNegative");
        
        if (!response.getHasMessages()) {
            ImageChartComponent c = (ImageChartComponent) getViewComponent(viewComponentId);
            c.setName(name);
            c.setWidth(width);
            c.setHeight(height);
            c.setDurationType(durationType);
            c.setDurationPeriods(durationPeriods);
            c.setUpdatePeriodType(updatePeriodType);
            c.setUpdatePeriods(updatePeriods);
            saveCompoundPoints(c, childPointIds);
        }

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult saveCompoundComponent(String viewComponentId, String name, List<StringStringPair> childPointIds) {
        ProcessResult response = new ProcessResult();

        validateCompoundComponent(response, name);

        if (!response.getHasMessages()) {
            CompoundComponent c = (CompoundComponent) getViewComponent(viewComponentId);
            c.setName(name);
            saveCompoundPoints(c, childPointIds);
        }

        return response;
    }

    private void validateCompoundComponent(ProcessResult response, String name) {
        if (StringUtils.isBlank(name))
            response.addContextualMessage("compoundName", "dsEdit.validate.required");
    }

    private void saveCompoundPoints(CompoundComponent c, List<StringStringPair> childPointIds) {
        User user = Common.getUser();

        for (StringStringPair kvp : childPointIds) {
            int dataPointId = -1;
            try {
                dataPointId = Integer.parseInt(kvp.getValue());
            }
            catch (NumberFormatException e) {
                // no op
            }

            DataPointVO dp = DataPointDao.getInstance().getDataPoint(dataPointId);

            if (dp == null || !Permissions.hasDataPointReadPermission(user, dp))
                c.setDataPoint(kvp.getKey(), null);
            else
                c.setDataPoint(kvp.getKey(), dp);
            c.getChildComponent(kvp.getKey()).validateDataPoint(user, false);
        }
    }

    private void resetPointComponent(PointComponent c) {
        if (c.tgetDataPoint() != null)
            c.tgetDataPoint().resetLastValue();
    }

    private ImageSet getImageSet(String id) {
        return Common.getImageSet(id);
    }

    private DynamicImage getDynamicImage(String id) {
        return Common.getDynamicImage(id);
    }

    @DwrPermission(user = true)
    public ViewComponent getViewComponent(String viewComponentId) {
        return getViewComponent(GraphicalViewsCommon.getUserEditView(Common.getUser()), viewComponentId);
    }

    private ViewComponent getViewComponent(GraphicalView view, String viewComponentId) {
        for (ViewComponent viewComponent : view.getViewComponents()) {
            if (viewComponent.getId().equals(viewComponentId))
                return viewComponent;
        }
        return null;
    }

    @DwrPermission(user = true)
    public String getBackgroundUrl() {
        return GraphicalViewsCommon.getUserEditView(Common.getUser()).getBackgroundFilename();
    }

    @DwrPermission(user = true)
    public ProcessResult clearBackground() {
    	User user = Common.getUser();
    	ProcessResult result = new ProcessResult();
    	if(Permissions.hasPermission(user, SystemSettingsDao.instance.getValue(GraphicalViewUploadPermissionDefinition.PERMISSION))){
	    	GraphicalView view = GraphicalViewsCommon.getUserEditView(user);
	        GraphicalViewsCommon.deleteImage(view.getBackgroundFilename());
	        view.setBackgroundFilename(null);
	        result.addData("hasPermission", true);
    	}else{
    		result.addData("hasPermission", false);
    	}
        
        return result;
    }

    @DwrPermission(user = true)
    public ProcessResult saveView(String name, String xid, int anonymousAccess, String readPermission, String setPermission, String editPermission) {
        ProcessResult result = new ProcessResult();

        User user = Common.getUser();
        GraphicalView view = GraphicalViewsCommon.getUserEditView(user);

        view.setName(name);
        view.setXid(xid);
        view.setAnonymousAccess(anonymousAccess);
        view.setReadPermission(readPermission);
        view.setSetPermission(setPermission);
        view.setEditPermission(editPermission);
        view.validate(result);

        if (!result.getHasMessages()) {
            view.setUserId(user.getId());
            new GraphicalViewDao().saveView(view);
            result.addData("view", view);
        }

        return result;
    }

    @DwrPermission(user = true)
    public void deleteView() {
        GraphicalView view = GraphicalViewsCommon.getUserEditView(Common.getUser());
        GraphicalViewsCommon.deleteImage(view.getBackgroundFilename());
        new GraphicalViewDao().removeView(view.getId());
    }

    private String generateViewContent(ViewComponent component, boolean update, HttpServletRequest request, String snippet, Map<String, Object> model) {
        
    	if(!update && component.getCachedContent(snippet) != null)
    		return (String)component.getCachedContent(snippet);
    	else{
    		String content =  generateContent(request, getFullSnippetName(snippet), model);
    		component.putCachedContent(snippet, content);
    		return content;
    	}
    }

    private String getFullSnippetName(String snippet) {
        return getModule().getWebPath() + "/web/snippet/" + snippet;
    }
    
    @DwrPermission(user = true)
    public void savePermissions(String readPermission, String editPermission) {
    	GraphicalView view = GraphicalViewsCommon.getUserEditView(Common.getUser());
        view.setReadPermission(readPermission);
        view.setEditPermission(editPermission);
        new GraphicalViewDao().saveView(view);
    }

    @DwrPermission(user = true)
    public ProcessResult getPermissions() {
    	GraphicalView view = GraphicalViewsCommon.getUserEditView(Common.getUser());
        ProcessResult result = new ProcessResult();
        result.addData("readPermission", view.getReadPermission());
        result.addData("editPermission", view.getEditPermission());
        result.addData("setPermission", view.getSetPermission());
        return result;
    }
}
