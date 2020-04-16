/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.v2.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.rt.event.AlarmLevels;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventModel extends AbstractVoModel<MaintenanceEventVO> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> dataSources;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> dataPoints;

    private AlarmLevels alarmLevel;
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
    private MangoPermissionModel togglePermission;

    public MaintenanceEventModel() {
        super();
    }

    public MaintenanceEventModel(MaintenanceEventVO vo) {
        fromVO(vo);
    }

    @Override
    public MaintenanceEventVO toVO() {
        MaintenanceEventVO vo = super.toVO();
        ProcessResult result = new ProcessResult();
        if(dataSources != null) {
            Set<Integer> ids = new HashSet<>();
            for(String xid : dataSources) {
                Integer id = DataSourceDao.getInstance().getIdByXid(xid);
                if(id != null)
                    ids.add(id);
                else
                    result.addContextualMessage("dataSources", "maintenanceEvents.validate.missingDataSource", xid);
            }
            vo.setDataSources(new ArrayList<>(ids));
        }
        if(dataPoints != null) {
            Set<Integer> ids = new HashSet<>();
            for(String xid : dataPoints) {
                Integer id = DataPointDao.getInstance().getIdByXid(xid);
                if(id != null)
                    ids.add(id);
                else
                    result.addContextualMessage("dataPoints", "maintenanceEvents.validate.missingDataPoint", xid);
            }
            vo.setDataPoints(new ArrayList<>(ids));
        }
        if(result.getHasMessages())
            throw new ValidationException(result);

        vo.setAlarmLevel(alarmLevel);
        vo.setScheduleType(MaintenanceEventVO.TYPE_CODES.getId(scheduleType));
        vo.setDisabled(disabled == null ? false : disabled);
        vo.setActiveYear(activeYear == null ? 0 : activeYear);
        vo.setActiveMonth(activeMonth == null ? 0 : activeMonth);
        vo.setActiveDay(activeDay == null ? 0 : activeDay);
        vo.setActiveHour(activeHour == null ? 0 : activeHour);
        vo.setActiveMinute(activeMinute == null ? 0 : activeMinute);
        vo.setActiveSecond(activeSecond == null ? 0 : activeSecond);
        vo.setActiveCron(activeCron);
        vo.setInactiveYear(inactiveYear == null ? 0 : inactiveYear);
        vo.setInactiveMonth(inactiveMonth == null ? 0 : inactiveMonth);
        vo.setInactiveDay(inactiveDay == null ? 0 : inactiveDay);
        vo.setInactiveHour(inactiveHour == null ? 0 : inactiveHour);
        vo.setInactiveMinute(inactiveMinute == null ? 0 : inactiveMinute);
        vo.setInactiveSecond(inactiveSecond == null ? 0 : inactiveSecond);
        vo.setInactiveCron(inactiveCron);
        vo.setTimeoutPeriods(timeoutPeriods == null ? 0 : timeoutPeriods);
        vo.setTimeoutPeriodType(Common.TIME_PERIOD_CODES.getId(timeoutPeriodType));
        vo.setTogglePermission(togglePermission != null ? togglePermission.getPermission() :  new MangoPermission());
        return vo;
    }

    @Override
    public void fromVO(MaintenanceEventVO vo) {
        super.fromVO(vo);
        if(vo.getDataSources().size() > 0) {
            dataSources = new ArrayList<>();
            for(int id : vo.getDataSources()) {
                String xid = DataSourceDao.getInstance().getXidById(id);
                if(xid != null)
                    dataSources.add(xid);
            }
        }

        if(vo.getDataPoints().size() > 0) {
            dataPoints = new ArrayList<>();
            for(int id : vo.getDataPoints()) {
                String xid = DataPointDao.getInstance().getXidById(id);
                if(xid != null)
                    dataPoints.add(xid);
            }
        }

        alarmLevel = vo.getAlarmLevel();
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
        togglePermission = new MangoPermissionModel(vo.getTogglePermission());
    }

    @Override
    protected MaintenanceEventVO newVO() {
        return new MaintenanceEventVO();
    }

    public List<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    public List<String> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<String> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(AlarmLevels alarmLevel) {
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

    public MangoPermissionModel getTogglePermission() {
        return togglePermission;
    }

    public void setTogglePermission(MangoPermissionModel togglePermission) {
        this.togglePermission = togglePermission;
    }
}
