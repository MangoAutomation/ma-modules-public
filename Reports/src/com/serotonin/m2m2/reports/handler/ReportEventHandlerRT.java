/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.mango.spring.dao.ReportDao;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportWorkItem;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.handlers.EventHandlerRT;

/**
 * Fire Reports on event active or inactive
 * 
 * TODO need a way to get HOST and PORT of current system
 * 
 * @author Terry Packer
 */
public class ReportEventHandlerRT extends EventHandlerRT<ReportEventHandlerVO>{

	private final Log LOG = LogFactory.getLog(ReportEventHandlerRT.class);
	
	/**
	 * @param vo
	 */
	public ReportEventHandlerRT(ReportEventHandlerVO vo) {
		super(vo);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.event.handlers.EventHandlerRT#eventRaised(com.serotonin.m2m2.rt.event.EventInstance)
	 */
	@Override
	public void eventRaised(EventInstance evt) {
    	try {
		   	if(vo.getActiveReportId() != Common.NEW_ID){
				//Schedule the Active Report To Run
				ReportVO report = ReportDao.instance.get(vo.getActiveReportId());
				if(report != null){
					String host = InetAddress.getLocalHost().getHostName();
				   	int port = Common.envProps.getInt("web.port", 8080);
					ReportWorkItem.queueReport(host, port, report);
				}
		   	}
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
		}
 		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.event.handlers.EventHandlerRT#eventInactive(com.serotonin.m2m2.rt.event.EventInstance)
	 */
	@Override
	public void eventInactive(EventInstance evt) {
		try{
			if(vo.getInactiveReportId() != Common.NEW_ID){
				//Schedule the Inactive Report to run
				ReportVO report = ReportDao.instance.get(vo.getInactiveReportId());
				if(report != null){
					String host = InetAddress.getLocalHost().getHostName();
					int port = Common.envProps.getInt("web.port", 8080);
					ReportWorkItem.queueReport(host, port, report);
				}
			}
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
		}

	}

}
