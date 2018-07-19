/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.maintenanceEvents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.v2.model.PatchableField;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.rt.event.AlarmLevels;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventModel extends AbstractVoModel<MaintenanceEventVO> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> dataSources;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> dataPoints;
    
    private String alarmLevel;
    private String scheduleType;
    private Boolean disabled;
    private Integer activeYear;
    private Integer activeMonth;
    private Integer activeDay;
    private Integer activeHour;
    private Integer activeMinute;
    private Integer activeSecond;
    private String activeCron;
    private Integer inactiveYear;
    private Integer inactiveMonth;
    private Integer inactiveDay;
    private Integer inactiveHour;
    private Integer inactiveMinute;
    private Integer inactiveSecond;
    private String inactiveCron;
    private Integer timeoutPeriods;
    private String timeoutPeriodType;
    @PatchableField()
    private String togglePermission;
    
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
        if(dataSources != null)
            vo.setDataSources(dataSources);
        if(dataPoints != null)
            vo.setDataPoints(dataPoints);
        vo.setAlarmLevel(AlarmLevels.CODES.getId(alarmLevel));
        vo.setScheduleType(MaintenanceEventVO.TYPE_CODES.getId(scheduleType));
        vo.setDisabled(disabled);
        vo.setActiveYear(activeYear);
        vo.setActiveMonth(activeMonth);
        vo.setActiveDay(activeDay);
        vo.setActiveHour(activeHour);
        vo.setActiveMinute(activeMinute);
        vo.setActiveSecond(activeSecond);
        vo.setActiveCron(activeCron);
        vo.setInactiveYear(inactiveYear);
        vo.setInactiveMonth(inactiveMonth);
        vo.setInactiveDay(inactiveDay);
        vo.setInactiveHour(inactiveHour);
        vo.setInactiveMinute(inactiveMinute);
        vo.setInactiveSecond(inactiveSecond);
        vo.setInactiveCron(inactiveCron);
        vo.setTimeoutPeriods(timeoutPeriods);
        vo.setTimeoutPeriodType(Common.TIME_PERIOD_CODES.getId(timeoutPeriodType));
        vo.setTogglePermission(togglePermission);
        return vo;
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.AbstractVoModel#fromVO(com.serotonin.m2m2.vo.AbstractVO)
     */
    @Override
    public void fromVO(MaintenanceEventVO vo) {
        super.fromVO(vo);
        dataSources = vo.getDataSources().size() > 0 ? vo.getDataSources() : null;
        dataPoints = vo.getDataPoints().size() > 0 ? vo.getDataPoints() : null;
        alarmLevel = AlarmLevels.CODES.getCode(vo.getAlarmLevel());
        scheduleType = MaintenanceEventVO.TYPE_CODES.getCode(vo.getScheduleType());
        disabled = vo.isDisabled();
        activeYear = vo.getActiveYear();
        activeMonth = vo.getActiveMonth();
        activeDay = vo.getActiveDay();
        activeHour = vo.getActiveHour();
        activeMinute = vo.getActiveMinute();
        activeSecond = vo.getActiveMinute();
        activeCron = vo.getActiveCron();
        inactiveYear = vo.getInactiveYear();
        inactiveMonth = vo.getInactiveMonth();
        inactiveDay = vo.getInactiveDay();
        inactiveHour = vo.getInactiveHour();
        inactiveMinute = vo.getInactiveMinute();
        inactiveSecond = vo.getInactiveSecond();
        inactiveCron = vo.getInactiveCron();
        timeoutPeriods = vo.getTimeoutPeriods();
        timeoutPeriodType = Common.TIME_PERIOD_CODES.getCode(vo.getTimeoutPeriodType());
        togglePermission = vo.getTogglePermission();
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

    public String getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
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

    public Integer getTimeoutPeriods() {
        return timeoutPeriods;
    }

    public void setTimeoutPeriods(Integer timeoutPeriods) {
        this.timeoutPeriods = timeoutPeriods;
    }

    public String getTimeoutPeriodType() {
        return timeoutPeriodType;
    }

    public void setTimeoutPeriodType(String timeoutPeriodType) {
        this.timeoutPeriodType = timeoutPeriodType;
    }

    public String getTogglePermission() {
        return togglePermission;
    }

    public void setTogglePermission(String togglePermission) {
        this.togglePermission = togglePermission;
    }
    
}
