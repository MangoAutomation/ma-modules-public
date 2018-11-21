/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.handler.ReportEventHandlerVO;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
public class ReportEventHandlerModel extends AbstractEventHandlerModel {

    private String activeReportXid;
    private String inactiveReportXid;
    
    public ReportEventHandlerModel() {
        super();
    }
    
    public ReportEventHandlerModel(ReportEventHandlerVO vo) {
        super(vo);
    }
    
    @Override
    public AbstractEventHandlerVO<?> toVO() {
        ReportEventHandlerVO vo = (ReportEventHandlerVO)super.toVO();
        Integer id =  ReportDao.getInstance().getIdByXid(activeReportXid);
        if(id != null)
            vo.setActiveReportId(id);
        id = ReportDao.getInstance().getIdByXid(inactiveReportXid);
        if(id != null)
            vo.setInactiveReportId(id);
        return vo;
    }
    
    @Override
    public void fromVO(AbstractEventHandlerVO<?> vo) {
        super.fromVO(vo);
        ReportEventHandlerVO hVo = (ReportEventHandlerVO)vo;
        this.activeReportXid = ReportDao.getInstance().getXidById(hVo.getActiveReportId());
        this.inactiveReportXid = ReportDao.getInstance().getXidById(hVo.getInactiveReportId());
                
    }
    
    @Override
    protected AbstractEventHandlerVO<?> newVO() {
        return new ReportEventHandlerVO();
    }

}
