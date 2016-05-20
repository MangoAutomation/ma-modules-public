/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
abstract public class CompoundComponent extends ViewComponent {

	public static final String IMAGE_CHART_KEY = "compound.component.image.chart";
	
	public CompoundComponent(){
		super();
	}
	
    @JsonProperty
    private String name;
    private List<CompoundChild> children = new ArrayList<CompoundChild>();
    
    // Runtime attributes
    private boolean visible;
    
    abstract protected void initialize();

    abstract public boolean isDisplayImageChart();

    abstract public String getImageChartData(Translations translations);

    abstract public String getStaticContent();

    abstract public boolean hasInfo();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CompoundChild> getChildComponents() {
        return children;
    }

    protected void addChild(String id, String descriptionKey, HtmlComponent htmlComponent) {
        addChildImpl(id, descriptionKey, htmlComponent, null);
    }

    protected void addChild(String id, String descriptionKey, PointComponent pointComponent, int[] dataTypesOverride) {
        addChildImpl(id, descriptionKey, pointComponent, dataTypesOverride);
    }

    private void addChildImpl(String id, String descriptionKey, ViewComponent viewComponent, int[] dataTypesOverride) {
        viewComponent.setIndex(getIndex());
        viewComponent.setIdSuffix("-" + id);
        children.add(new CompoundChild(id, new TranslatableMessage(descriptionKey), viewComponent, dataTypesOverride));
    }

    @Override
    public boolean isCompoundComponent() {
        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean containsValidVisibleDataPoint(int dataPointId) {
        if (!visible)
            return false;

        for (CompoundChild child : children) {
            if (child.getViewComponent().containsValidVisibleDataPoint(dataPointId))
                return true;
        }

        return false;
    }

    public PointComponent findPointComponent(String viewComponentId) {
        for (CompoundChild child : children) {
            ViewComponent vc = child.getViewComponent();
            if (vc.isPointComponent() && vc.getId().equals(viewComponentId))
                return (PointComponent) vc;
        }
        return null;
    }

    @Override
    public void validateDataPoint(User user, boolean makeReadOnly) {
        visible = false;

        // Validate child components
        for (CompoundChild child : children) {
            ViewComponent vc = child.getViewComponent();
            vc.validateDataPoint(user, makeReadOnly);

            // If any child component is visible, this is visible.
            if (vc.isVisible())
                visible = true;
        }
    }

    @Override
    public void setIndex(int index) {
        super.setIndex(index);
        // Make sure the child components have the same id.
        for (CompoundChild child : children)
            child.getViewComponent().setIndex(index);
    }

    public void setDataPoint(String childId, DataPointVO dataPoint) {
        CompoundChild child = getChild(childId);
        if (child != null && child.getViewComponent().isPointComponent())
            ((PointComponent) child.getViewComponent()).tsetDataPoint(dataPoint);
    }

    public ViewComponent getChildComponent(String childId) {
        CompoundChild child = getChild(childId);
        if (child == null)
            return null;
        return child.getViewComponent();
    }

    private CompoundChild getChild(String childId) {
        for (CompoundChild child : children) {
            if (child.getId().equals(childId))
                return child;
        }
        return null;
    }

    protected String generateImageChartData(Translations translations, long duration, String... childIds) {
        return generateImageChartData(translations, duration, 500, 250, childIds);
    }

    protected String generateImageChartData(Translations translations, long duration, int width, int height,
            String... childIds) {
    	
    	//Only get new data if we are supposed to
        if((this.cachedContent.get(IMAGE_CHART_KEY) != null)&&(Common.backgroundProcessing.currentTimeMillis() < lastUpdated + Common.getMillis(updatePeriodType, updatePeriods)))
        	return (String)this.cachedContent.get(IMAGE_CHART_KEY);
        
        long ts = 0;
        for (String childId : childIds) {
            PointComponent comp = (PointComponent) getChild(childId).getViewComponent();
            if (comp.isValid() && comp.isVisible() && comp.tgetDataPoint().lastValue() != null) {
                long cts = comp.tgetDataPoint().lastValue().getTime();
                if (ts < cts)
                    ts = cts;
            }
        }
        
        StringBuilder htmlData = new StringBuilder();
        htmlData.append("<img src=\"chart/");
        htmlData.append(ts);
        htmlData.append('_');
        htmlData.append(duration);

        //Define a list of colours to use for the chart, each must be unique
        //currently using the JFree Chart method of determining these
        ChartColourGenerator colors = new DefaultJFreeChartColourGenerator();

        //Allow charts with 1 point to use the default color
        int activeComponentCount = 0;
        for (String childId : childIds) {
            PointComponent comp = (PointComponent) getChild(childId).getViewComponent();
            if (comp.isValid() && comp.isVisible()) {
            	activeComponentCount++;
            }
        }
        boolean usePointChartColour = false;
        if(activeComponentCount == 1)
        	usePointChartColour = true;
        
        for (String childId : childIds) {
            PointComponent comp = (PointComponent) getChild(childId).getViewComponent();
            if (comp.isValid() && comp.isVisible()) {
                htmlData.append('_');
                htmlData.append(comp.tgetDataPoint().getId());
                //Assign Colour or let chart servlet use point settings
                if(!usePointChartColour){
                	htmlData.append("|");
                	htmlData.append(colors.getNextHexColour());
                }
            }
        }

        htmlData.append("_w");
        htmlData.append(width);
        htmlData.append("_h");
        htmlData.append(height);

        htmlData.append(".png");
        htmlData.append("\" alt=\"" + translations.translate("common.imageChart") + "\"/>");

        String output = htmlData.toString();
        this.cachedContent.put(IMAGE_CHART_KEY, output);
        this.lastUpdated = Common.backgroundProcessing.currentTimeMillis();
        
        return output;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, name);

        int len = 0;
        for (CompoundChild child : children) {
            if (child.getViewComponent().isPointComponent())
                len++;
        }
        out.writeInt(len);

        for (CompoundChild child : children) {
            if (child.getViewComponent().isPointComponent()) {
                out.writeUTF(child.getId());
                writeDataPoint(out, ((PointComponent) child.getViewComponent()).tgetDataPoint());
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        children = new ArrayList<CompoundChild>();
        initialize();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            name = SerializationHelper.readSafeUTF(in);

            int len = in.readInt();
            for (int i = 0; i < len; i++) {
                String childId = in.readUTF();
                DataPointVO dataPoint = readDataPoint(in);
                setDataPoint(childId, dataPoint);
            }
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);

        Map<String, Object> jsonChildren = new HashMap<String, Object>();
        for (CompoundChild child : children) {
            if (child.getViewComponent().isPointComponent())
                jsonWriteDataPoint(jsonChildren, child.getId(), (PointComponent) child.getViewComponent());
        }
        writer.writeEntry("children", jsonChildren);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        JsonObject jsonChildren = jsonObject.getJsonObject("children");
        if (jsonChildren != null) {
            for (Map.Entry<String, JsonValue> jsonChild : jsonChildren.entrySet()) {
                CompoundChild child = getChild(jsonChild.getKey());
                if (child == null || !child.getViewComponent().isPointComponent())
                    throw new TranslatableJsonException("emport.error.compound.invalidChildId", jsonChild.getKey(),
                            definition().getId(), getPointComponentChildIds());
                jsonReadDataPoint(jsonChild.getValue(), (PointComponent) child.getViewComponent());
            }
        }
    }

    private List<String> getPointComponentChildIds() {
        List<String> result = new ArrayList<String>();
        for (CompoundChild child : children) {
            if (child.getViewComponent().isPointComponent())
                result.add(child.getId());
        }
        return result;
    }
}
