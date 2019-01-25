/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.scheduledEvents.ScheduledEventVO;

/**
 * @author Terry Packer
 *
 */
public class ScheduledEventModel extends AbstractVoModel<ScheduledEventVO>{

    private String alias;
    private AlarmLevels alarmLevel;
    private String scheduleType;
    private boolean returnToNormal;
    private boolean disabled;
    
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
    
    public ScheduledEventModel() {
        super();
    }
    public ScheduledEventModel(ScheduledEventVO vo) {
        super(vo);
    }
    
    @Override
    public void fromVO(ScheduledEventVO vo) {
        super.fromVO(vo);
        this.alias = vo.getAlias();
        this.scheduleType = ScheduledEventVO.TYPE_CODES.getCode(vo.getScheduleType());
        this.returnToNormal = vo.isReturnToNormal();
        this.disabled = vo.isDisabled();
        this.activeYear = vo.getActiveYear();
        this.activeMonth = vo.getActiveMonth();
        this.activeDay = vo.getActiveDay();
        this.activeHour = vo.getActiveHour();
        this.activeMinute = vo.getActiveMinute();
        this.activeSecond = vo.getActiveSecond();
        
        this.activeCron = vo.getActiveCron();
        
        this.activeYear = vo.getInactiveYear();
        this.activeMonth = vo.getInactiveMonth();
        this.activeDay = vo.getInactiveDay();
        this.activeHour = vo.getInactiveHour();
        this.activeMinute = vo.getInactiveMinute();
        this.activeSecond = vo.getInactiveSecond();
        
        this.inactiveCron = vo.getInactiveCron();
    }
    
    @Override
    public ScheduledEventVO toVO() {
        ScheduledEventVO vo = super.toVO();
        vo.setAlias(alias);
        vo.setScheduleType(ScheduledEventVO.TYPE_CODES.getId(scheduleType));
        vo.setReturnToNormal(returnToNormal);
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
        return vo;
    }
    
    @Override
    protected ScheduledEventVO newVO() {
        return new ScheduledEventVO();
    }
    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }
    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
    /**
     * @return the alarmLevel
     */
    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }
    /**
     * @param alarmLevel the alarmLevel to set
     */
    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
    /**
     * @return the scheduleType
     */
    public String getScheduleType() {
        return scheduleType;
    }
    /**
     * @param scheduleType the scheduleType to set
     */
    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }
    /**
     * @return the returnToNormal
     */
    public boolean isReturnToNormal() {
        return returnToNormal;
    }
    /**
     * @param returnToNormal the returnToNormal to set
     */
    public void setReturnToNormal(boolean returnToNormal) {
        this.returnToNormal = returnToNormal;
    }
    /**
     * @return the disabled
     */
    public boolean isDisabled() {
        return disabled;
    }
    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    /**
     * @return the activeYear
     */
    public Integer getActiveYear() {
        return activeYear;
    }
    /**
     * @param activeYear the activeYear to set
     */
    public void setActiveYear(Integer activeYear) {
        this.activeYear = activeYear;
    }
    /**
     * @return the activeMonth
     */
    public Integer getActiveMonth() {
        return activeMonth;
    }
    /**
     * @param activeMonth the activeMonth to set
     */
    public void setActiveMonth(Integer activeMonth) {
        this.activeMonth = activeMonth;
    }
    /**
     * @return the activeDay
     */
    public Integer getActiveDay() {
        return activeDay;
    }
    /**
     * @param activeDay the activeDay to set
     */
    public void setActiveDay(Integer activeDay) {
        this.activeDay = activeDay;
    }
    /**
     * @return the activeHour
     */
    public Integer getActiveHour() {
        return activeHour;
    }
    /**
     * @param activeHour the activeHour to set
     */
    public void setActiveHour(Integer activeHour) {
        this.activeHour = activeHour;
    }
    /**
     * @return the activeMinute
     */
    public Integer getActiveMinute() {
        return activeMinute;
    }
    /**
     * @param activeMinute the activeMinute to set
     */
    public void setActiveMinute(Integer activeMinute) {
        this.activeMinute = activeMinute;
    }
    /**
     * @return the activeSecond
     */
    public Integer getActiveSecond() {
        return activeSecond;
    }
    /**
     * @param activeSecond the activeSecond to set
     */
    public void setActiveSecond(Integer activeSecond) {
        this.activeSecond = activeSecond;
    }
    /**
     * @return the activeCron
     */
    public String getActiveCron() {
        return activeCron;
    }
    /**
     * @param activeCron the activeCron to set
     */
    public void setActiveCron(String activeCron) {
        this.activeCron = activeCron;
    }
    /**
     * @return the inactiveYear
     */
    public Integer getInactiveYear() {
        return inactiveYear;
    }
    /**
     * @param inactiveYear the inactiveYear to set
     */
    public void setInactiveYear(Integer inactiveYear) {
        this.inactiveYear = inactiveYear;
    }
    /**
     * @return the inactiveMonth
     */
    public Integer getInactiveMonth() {
        return inactiveMonth;
    }
    /**
     * @param inactiveMonth the inactiveMonth to set
     */
    public void setInactiveMonth(Integer inactiveMonth) {
        this.inactiveMonth = inactiveMonth;
    }
    /**
     * @return the inactiveDay
     */
    public Integer getInactiveDay() {
        return inactiveDay;
    }
    /**
     * @param inactiveDay the inactiveDay to set
     */
    public void setInactiveDay(Integer inactiveDay) {
        this.inactiveDay = inactiveDay;
    }
    /**
     * @return the inactiveHour
     */
    public Integer getInactiveHour() {
        return inactiveHour;
    }
    /**
     * @param inactiveHour the inactiveHour to set
     */
    public void setInactiveHour(Integer inactiveHour) {
        this.inactiveHour = inactiveHour;
    }
    /**
     * @return the inactiveMinute
     */
    public Integer getInactiveMinute() {
        return inactiveMinute;
    }
    /**
     * @param inactiveMinute the inactiveMinute to set
     */
    public void setInactiveMinute(Integer inactiveMinute) {
        this.inactiveMinute = inactiveMinute;
    }
    /**
     * @return the inactiveSecond
     */
    public Integer getInactiveSecond() {
        return inactiveSecond;
    }
    /**
     * @param inactiveSecond the inactiveSecond to set
     */
    public void setInactiveSecond(Integer inactiveSecond) {
        this.inactiveSecond = inactiveSecond;
    }
    /**
     * @return the inactiveCron
     */
    public String getInactiveCron() {
        return inactiveCron;
    }
    /**
     * @param inactiveCron the inactiveCron to set
     */
    public void setInactiveCron(String inactiveCron) {
        this.inactiveCron = inactiveCron;
    }

}
