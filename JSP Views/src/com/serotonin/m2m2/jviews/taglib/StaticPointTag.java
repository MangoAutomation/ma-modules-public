/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.jviews.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.jviews.component.JspView;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.dwr.BaseDwr;

/**
 * @author Matthew Lohbihler
 */
public class StaticPointTag extends ViewTagSupport {
    private static final long serialVersionUID = -1;

    private String xid;
    private boolean raw;
    private String disabledValue;

    public void setXid(String xid) {
        this.xid = xid;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public void setDisabledValue(String disabledValue) {
        this.disabledValue = disabledValue;
    }

    @Override
    public int doStartTag() throws JspException {
        // Find the custom view.
        JspView view = getJspView();

        // Find the point.
        DataPointVO dataPointVO = getDataPointVO(view, xid);

        // Write the value into the page.
        JspWriter out = pageContext.getOut();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        DataPointRT dataPointRT = Common.runtimeManager.getDataPoint(dataPointVO.getId());
        if (dataPointRT == null)
            write(out, disabledValue);
        else {
            PointValueTime pvt = dataPointRT.getPointValue();

            if (pvt != null && pvt.getValue() instanceof ImageValue) {
                // Text renderers don't help here. Create a thumbnail.
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("point", dataPointVO);
                model.put("pointValue", pvt);
                write(out, BaseDwr.generateContent(request, "imageValueThumbnail.jsp", model));
            }
            else {
                int hint = raw ? TextRenderer.HINT_RAW : TextRenderer.HINT_FULL;
                write(out, dataPointVO.getTextRenderer().getText(pvt, hint));
            }
        }

        return EVAL_BODY_INCLUDE;
    }

    private void write(JspWriter out, String content) throws JspException {
        try {
            out.append(content);
        }
        catch (IOException e) {
            throw new JspException(e);
        }
    }

    @Override
    public void release() {
        super.release();
        xid = null;
        raw = false;
        disabledValue = null;
    }
}
