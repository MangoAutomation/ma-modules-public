/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.maintenanceEvents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventModel extends AbstractVoModel<MaintenanceEventVO> {
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> dataSources;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> dataPoints;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer alarmLevel;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer scheduleType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer activeYear;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer activeMonth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer activeDay;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer activeHour;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer activeMinute;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer activeSecond;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String activeCron;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer inactiveYear;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer inactiveMonth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer inactiveDay;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer inactiveHour;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer inactiveMinute;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer inactiveSecond;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String inactiveCron;
    
    public MaintenanceEventModel() {
        super(new MaintenanceEventVO());
    }
    
    public MaintenanceEventModel(MaintenanceEventVO vo) {
        super(vo);
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.AbstractVoModel#toVO()
     */
    @Override
    public MaintenanceEventVO toVO() {
        MaintenanceEventVO vo = super.toVO();
        vo.setDataSources(dataSources);
        //TODO Fill out remaining memebers
        return vo;
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.AbstractVoModel#fromVO(com.serotonin.m2m2.vo.AbstractVO)
     */
    @Override
    public void fromVO(MaintenanceEventVO vo) {
        super.fromVO(vo);
        dataSources = vo.getDataSources();
        //TODO Fill out remaining memebers
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.AbstractVoModel#newVO()
     */
    @Override
    protected MaintenanceEventVO newVO() {
        return new MaintenanceEventVO();
    }

    public List<Integer> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<Integer> dataSources) {
        this.dataSources = dataSources;
    }

    public List<Integer> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<Integer> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public Integer getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(Integer alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public Integer getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(Integer scheduleType) {
        this.scheduleType = scheduleType;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Integer getActiveYear() {
        return activeYear;
    }

    public void setActiveYear(Integer activeYear) {
        this.activeYear = activeYear;
    }

    public Integer getActiveMonth() {
        return activeMonth;
    }

    public void setActiveMonth(Integer activeMonth) {
        this.activeMonth = activeMonth;
    }

    public Integer getActiveDay() {
        return activeDay;
    }

    public void setActiveDay(Integer activeDay) {
        this.activeDay = activeDay;
    }

    public Integer getActiveHour() {
        return activeHour;
    }

    public void setActiveHour(Integer activeHour) {
        this.activeHour = activeHour;
    }

    public Integer getActiveMinute() {
        return activeMinute;
    }

    public void setActiveMinute(Integer activeMinute) {
        this.activeMinute = activeMinute;
    }

    public Integer getActiveSecond() {
        return activeSecond;
    }

    public void setActiveSecond(Integer activeSecond) {
        this.activeSecond = activeSecond;
    }

    public String getActiveCron() {
        return activeCron;
    }

    public void setActiveCron(String activeCron) {
        this.activeCron = activeCron;
    }

    public Integer getInactiveYear() {
        return inactiveYear;
    }

    public void setInactiveYear(Integer inactiveYear) {
        this.inactiveYear = inactiveYear;
    }

    public Integer getInactiveMonth() {
        return inactiveMonth;
    }

    public void setInactiveMonth(Integer inactiveMonth) {
        this.inactiveMonth = inactiveMonth;
    }

    public Integer getInactiveDay() {
        return inactiveDay;
    }

    public void setInactiveDay(Integer inactiveDay) {
        this.inactiveDay = inactiveDay;
    }

    public Integer getInactiveHour() {
        return inactiveHour;
    }

    public void setInactiveHour(Integer inactiveHour) {
        this.inactiveHour = inactiveHour;
    }

    public Integer getInactiveMinute() {
        return inactiveMinute;
    }

    public void setInactiveMinute(Integer inactiveMinute) {
        this.inactiveMinute = inactiveMinute;
    }

    public Integer getInactiveSecond() {
        return inactiveSecond;
    }

    public void setInactiveSecond(Integer inactiveSecond) {
        this.inactiveSecond = inactiveSecond;
    }

    public String getInactiveCron() {
        return inactiveCron;
    }

    public void setInactiveCron(String inactiveCron) {
        this.inactiveCron = inactiveCron;
    }
}
