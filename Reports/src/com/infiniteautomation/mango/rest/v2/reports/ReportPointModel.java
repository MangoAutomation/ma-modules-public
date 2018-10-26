/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.reports.vo.ReportPointVO;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class ReportPointModel {

    private String pointXid;
    private String pointKey;
    private String colour;
    private float weight = 1;
    private boolean consolidatedChart;
    private String plotType;
    
    public ReportPointModel() { }
    public ReportPointModel(ReportPointVO vo) {
        pointXid = DataPointDao.getInstance().getXidById(vo.getPointId());
        pointKey = vo.getPointKey();
        colour = vo.getColour();
        weight = vo.getWeight();
        consolidatedChart = vo.isConsolidatedChart();
        plotType = DataPointVO.PLOT_TYPE_CODES.getCode(vo.getPlotType());
    }
    /**
     * @return the pointXid
     */
    public String getPointXid() {
        return pointXid;
    }
    /**
     * @param pointXid the pointXid to set
     */
    public void setPointXid(String pointXid) {
        this.pointXid = pointXid;
    }
    /**
     * @return the pointKey
     */
    public String getPointKey() {
        return pointKey;
    }
    /**
     * @param pointKey the pointKey to set
     */
    public void setPointKey(String pointKey) {
        this.pointKey = pointKey;
    }
    /**
     * @return the colour
     */
    public String getColour() {
        return colour;
    }
    /**
     * @param colour the colour to set
     */
    public void setColour(String colour) {
        this.colour = colour;
    }
    /**
     * @return the weight
     */
    public float getWeight() {
        return weight;
    }
    /**
     * @param weight the weight to set
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }
    /**
     * @return the consolidatedChart
     */
    public boolean isConsolidatedChart() {
        return consolidatedChart;
    }
    /**
     * @param consolidatedChart the consolidatedChart to set
     */
    public void setConsolidatedChart(boolean consolidatedChart) {
        this.consolidatedChart = consolidatedChart;
    }
    /**
     * @return the plotType
     */
    public String getPlotType() {
        return plotType;
    }
    /**
     * @param plotType the plotType to set
     */
    public void setPlotType(String plotType) {
        this.plotType = plotType;
    }
    
    public ReportPointVO toVO() {
        ReportPointVO vo = new ReportPointVO();
        Integer pointId = DataPointDao.getInstance().getIdByXid(pointXid);
        if(pointId != null)
            vo.setPointId(pointId);
        vo.setPointKey(pointKey);
        vo.setColour(colour);
        vo.setWeight(weight);
        vo.setConsolidatedChart(consolidatedChart);
        vo.setPlotType(DataPointVO.PLOT_TYPE_CODES.getId(plotType));
        
        return vo;
    }
    
}
