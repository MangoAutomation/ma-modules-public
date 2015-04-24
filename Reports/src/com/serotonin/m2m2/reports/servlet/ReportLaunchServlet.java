package com.serotonin.m2m2.reports.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportJob;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.servlet.BaseInfoServlet;

public class ReportLaunchServlet extends BaseInfoServlet{
	private static final long serialVersionUID = -1;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		User user = Common.getUser(request);
		if(user != null) {
			ReportDao dao = new ReportDao();
			ReportVO report = null;
			int id = getIntRequestParameter(request, "reportId", -1);
			if(id != -1)
				report = dao.getReport(id);
			String xid = request.getParameter("reportXid");
			if(xid != null)
				report = dao.getReport(xid);
			if(report != null && (user.getId() == report.getUserId() || Permissions.hasAdmin(user))) {
				ReportJob.scheduleReportJob(report);
				try {
					response.getWriter().write("Report " + report.getName() + " scheduled");
				} catch(Exception e) {
					response.setStatus(500);
				}
			}
		}
	}
}
