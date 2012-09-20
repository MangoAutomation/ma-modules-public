/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.component;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.serotonin.m2m2.jviews.JspComponentState;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.dwr.BaseDwr;

/**
 * @author Matthew Lohbihler
 */
public class JspViewPoint extends JspViewComponent {
    private final DataPointVO dataPointVO;
    private final boolean raw;
    private final String disabledValue;
    private final boolean time;

    public JspViewPoint(int id, DataPointVO dataPointVO, boolean raw, String disabledValue, boolean time) {
        super(id);
        this.dataPointVO = dataPointVO;
        this.raw = raw;
        if (disabledValue == null)
            this.disabledValue = "";
        else
            this.disabledValue = disabledValue;
        this.time = time;
    }

    // public DataPointVO getDataPointVO() {
    // return dataPointVO;
    // }
    //
    // public boolean isRaw() {
    // return raw;
    // }
    //
    // public String getDisabledValue() {
    // return disabledValue;
    // }
    //
    // public boolean isTime() {
    // return time;
    // }

    @Override
    protected void createStateImpl(RuntimeManager rtm, HttpServletRequest request, JspComponentState state) {
        String value;
        DataPointRT dataPointRT = rtm.getDataPoint(dataPointVO.getId());
        if (dataPointRT == null)
            value = disabledValue;
        else {
            PointValueTime pvt = dataPointRT.getPointValue();

            if (pvt != null && pvt.getValue() instanceof ImageValue) {
                // Text renderers don't help here. Create a thumbnail.
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("point", dataPointVO);
                model.put("pointValue", pvt);
                value = BaseDwr.generateContent(request, "imageValueThumbnail.jsp", model);
            }
            else {
                int hint = raw ? TextRenderer.HINT_RAW : TextRenderer.HINT_FULL;
                value = dataPointVO.getTextRenderer().getText(pvt, hint);
                if (pvt != null && time)
                    state.setTime(pvt.getTime());
            }
        }
        state.setValue(value);
    }
}
