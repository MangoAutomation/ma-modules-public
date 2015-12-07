/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.servlet.ReportChartServlet;
import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;

public class ReportChartHandler implements UrlHandler {
    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model)
            throws Exception {
        int instanceId = Integer.parseInt(request.getParameter("instanceId"));
        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);

        User user = Common.getUser(request);
        ReportCommon.ensureReportInstancePermission(user, instance);

        ReportChartCreator creator = new ReportChartCreator(ControllerUtils.getTranslations(request),
                user.getTimeZoneInstance());
        creator.createContent(instance, reportDao, null, false);

        Map<String, byte[]> imageData = new HashMap<String, byte[]>();
        imageData.put(creator.getChartName(), creator.getImageData());
        for (ReportChartCreator.PointStatistics pointStatistics : creator.getPointStatistics())
            imageData.put(pointStatistics.getChartName(), pointStatistics.getImageData());
        user.setAttribute(ReportChartServlet.IMAGE_DATA_KEY, imageData);

        return new ReportChartView(creator.getHtml());
    }

    static class ReportChartView implements View {
        private final String content;

        public ReportChartView(String content) {
            this.content = content;
        }

        @Override
        public String getContentType() {
        	return MediaType.TEXT_HTML_VALUE;
        }

        @Override
        public void render(@SuppressWarnings("rawtypes") Map model, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
        	response.setContentType(getContentType());
            response.getWriter().write(content);
        }
    }
}
