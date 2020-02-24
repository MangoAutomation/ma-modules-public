/*
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer.BaseTextRendererModel;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.role.Role;


/**
 * Data point REST model v2
 *
 * @author Jared Wiltshire
 *
 */
public class DataPointModel extends AbstractVoModel<DataPointVO> {

    Integer id;
    String xid;
    String name;
    Boolean enabled;

    String deviceName;
    String readPermission;
    String setPermission;
    Boolean purgeOverride;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    TimePeriodModel purgePeriod;
    String unit;
    Boolean useIntegralUnit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String integralUnit;
    Boolean useRenderedUnit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String renderedUnit;
    AbstractPointLocatorModel<?> pointLocator;
    String chartColour;
    String plotType;
    LoggingPropertiesModel loggingProperties;
    BaseTextRendererModel<?> textRenderer;
    String rollup;
    String simplifyType;
    Double simplifyTolerance;
    Integer simplifyTarget;
    Boolean preventSetExtremeValues;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Double setExtremeLowLimit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Double setExtremeHighLimit;

    Integer dataSourceId;
    String dataSourceXid;
    String dataSourceName;
    String dataSourceTypeName;
    Set<String> dataSourceEditRoles;

    boolean mergeTags = false;
    Map<String, String> tags;

    //For display purposes
    String extendedName;

    public DataPointModel() {
    }

    /**
     * For writing out JSON
     * @param point
     */
    public DataPointModel(DataPointVO point) {
        fromVO(point);
    }

    @Override
    public void fromVO(DataPointVO point) {
        this.id = point.getId();
        this.xid = point.getXid();
        this.name = point.getName();
        this.enabled = point.isEnabled();

        this.deviceName = point.getDeviceName();
        this.readPermission = PermissionService.implodeRoles(point.getReadRoles());
        this.setPermission = PermissionService.implodeRoles(point.getSetRoles());
        this.purgeOverride = point.isPurgeOverride();
        if (this.purgeOverride) {
            this.purgePeriod = new TimePeriodModel(point.getPurgePeriod(), point.getPurgeType());
        }
        this.unit = UnitUtil.formatLocal(point.getUnit());
        this.useIntegralUnit = point.isUseIntegralUnit();
        if (this.useIntegralUnit) {
            this.integralUnit = UnitUtil.formatLocal(point.getIntegralUnit());
        }
        this.useRenderedUnit = point.isUseRenderedUnit();
        if (this.useRenderedUnit) {
            this.renderedUnit = UnitUtil.formatLocal(point.getRenderedUnit());
        }
        //The Point Locator will be set by the RestModelMapper after
        this.chartColour = point.getChartColour();
        this.plotType = DataPointVO.PLOT_TYPE_CODES.getCode(point.getPlotType());
        this.tags = point.getTags();

        this.loggingProperties = new LoggingPropertiesModel(point);

        this.dataSourceId = point.getDataSourceId();
        this.dataSourceXid = point.getDataSourceXid();
        this.dataSourceName = point.getDataSourceName();
        this.dataSourceTypeName = point.getDataSourceTypeName();

        this.rollup = Common.ROLLUP_CODES.getCode(point.getRollup());
        this.simplifyType = DataPointVO.SIMPLIFY_TYPE_CODES.getCode(point.getSimplifyType());
        this.simplifyTolerance = point.getSimplifyTolerance();
        this.simplifyTarget = point.getSimplifyTarget();

        this.preventSetExtremeValues = point.isPreventSetExtremeValues();
        if (this.preventSetExtremeValues) {
            this.setExtremeLowLimit = point.getSetExtremeLowLimit();
            this.setExtremeHighLimit = point.getSetExtremeHighLimit();
        }
        if(point.getDataSourceEditRoles() != null) {
            this.dataSourceEditRoles = new HashSet<>();
            for(Role role : point.getDataSourceEditRoles()) {
                this.dataSourceEditRoles.add(role.getXid());
            }
        }
        this.extendedName = point.getExtendedName();
    }

    @Override
    public DataPointVO toVO() {
        DataPointVO point = new DataPointVO();
        PermissionService service = Common.getBean(PermissionService.class);

        if (xid != null) {
            point.setXid(xid);
        }
        if (name != null) {
            point.setName(name);
        }
        if (enabled != null) {
            point.setEnabled(enabled);
        }
        if (deviceName != null) {
            point.setDeviceName(deviceName);
        }
        //TODO Mango 4.0 Use ModelMapper.unmap
        if (readPermission != null) {
            point.setReadRoles(service.explodeLegacyPermissionGroupsToRoles(readPermission));
        }
        //TODO Mango 4.0 Use ModelMapper.unmap
        if (setPermission != null) {
            point.setSetRoles(service.explodeLegacyPermissionGroupsToRoles(setPermission));
        }
        //TODO Mango 4.0 Use ModelMapper.unmap
        if(StringUtils.isNotEmpty(dataSourceXid)) {
            DataSourceVO ds = DataSourceDao.getInstance().getByXid(dataSourceXid);
            if(ds!= null) {
                point.setDataSourceId(ds.getId());
                point.setDataSourceName(ds.getName());
                point.setDataSourceXid(ds.getXid());
                point.setDataSourceTypeName(ds.getTypeKey());
            }
        }
        //TODO Mango 4.0 Use ModelMapper.unmap
        if(point.getDataSourceId() <= 0 && dataSourceId != null && dataSourceId > 0) {
            point.setDataSourceId(dataSourceId);
        }

        if (purgeOverride != null) {
            point.setPurgeOverride(purgeOverride);
            //Ensure that a purge period must be supplied
            if(purgeOverride) {
                point.setPurgePeriod(-1);
                point.setPurgeType(-1);
            }

        }
        if (purgePeriod != null) {
            point.setPurgePeriod(purgePeriod.getPeriods());
            point.setPurgeType(Common.TIME_PERIOD_CODES.getId(purgePeriod.getPeriodType()));
        }
        if (unit != null) {
            try {
                point.setUnit(UnitUtil.parseLocal(unit));
            } catch(IllegalArgumentException e) {
                point.setUnit(null); //Signal to use the unit string
            }
        }
        if (useIntegralUnit != null) {
            point.setUseIntegralUnit(useIntegralUnit);
        }
        if (integralUnit != null) {
            try {
                point.setIntegralUnit(UnitUtil.parseLocal(integralUnit));
            } catch(IllegalArgumentException e) {
                point.setIntegralUnit(null);
            }
        }
        if (useRenderedUnit != null) {
            point.setUseRenderedUnit(useRenderedUnit);
        }
        if (this.renderedUnit != null) {
            try {
                point.setRenderedUnit(UnitUtil.parseLocal(renderedUnit));
            } catch(IllegalArgumentException e) {
                point.setRenderedUnit(null);
            }
        }
        if (chartColour != null) {
            point.setChartColour(chartColour);
        }
        if (plotType != null) {
            point.setPlotType(DataPointVO.PLOT_TYPE_CODES.getId(plotType));
        }
        if (this.tags != null) {
            Map<String, String> existingTags = point.getTags();
            if (!this.mergeTags || existingTags == null) {
                // existingTags is only null if someone tried to use mergeTags when creating a data point
                point.setTags(this.tags);
            } else {
                Map<String, String> mergedTags = new HashMap<>(existingTags);
                for (Entry<String, String> entry : this.tags.entrySet()) {
                    String tagKey = entry.getKey();
                    String tagValue = entry.getValue();

                    if (tagValue == null) {
                        mergedTags.remove(tagKey);
                    } else {
                        mergedTags.put(tagKey, tagValue);
                    }
                }
                point.setTags(mergedTags);
            }
        }else {
            //TODO Mango 4.0 unmap
            if(id != null && id > 0) {
                point.setTags(DataPointTagsDao.getInstance().getTagsForDataPointId(id));
            }else if(xid != null) {
                Integer id = DataPointDao.getInstance().getIdByXid(xid);
                if(id != null) {
                    point.setTags(DataPointTagsDao.getInstance().getTagsForDataPointId(id));
                }
            }
        }
        if (this.loggingProperties != null) {
            loggingProperties.copyPropertiesTo(point);
        }
        if (this.textRenderer != null) {
            point.setTextRenderer(textRenderer.toVO());
        }

        if (pointLocator != null) {
            point.setPointLocator(pointLocator.toVO());
        }

        if (this.rollup != null) {
            point.setRollup(Common.ROLLUP_CODES.getId(this.rollup));
        }
        if(this.simplifyType != null) {
            point.setSimplifyType(DataPointVO.SIMPLIFY_TYPE_CODES.getId(this.simplifyType));
        }
        if(this.simplifyTolerance != null) {
            point.setSimplifyTolerance(simplifyTolerance);
        }
        if(this.simplifyTarget != null) {
            point.setSimplifyTarget(simplifyTarget);
        }
        if (this.preventSetExtremeValues != null) {
            point.setPreventSetExtremeValues(this.preventSetExtremeValues);
        }
        if (this.setExtremeLowLimit != null) {
            point.setSetExtremeLowLimit(this.setExtremeLowLimit);
        }
        if (this.setExtremeHighLimit != null) {
            point.setSetExtremeHighLimit(this.setExtremeHighLimit);
        }
        return point;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getXid() {
        return xid;
    }

    @Override
    public void setXid(String xid) {
        this.xid = xid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getReadPermission() {
        return readPermission;
    }

    public void setReadPermission(String readPermission) {
        this.readPermission = readPermission;
    }

    public String getSetPermission() {
        return setPermission;
    }

    public void setSetPermission(String setPermission) {
        this.setPermission = setPermission;
    }

    public Boolean getPurgeOverride() {
        return purgeOverride;
    }

    public void setPurgeOverride(Boolean purgeOverride) {
        this.purgeOverride = purgeOverride;
    }

    public TimePeriodModel getPurgePeriod() {
        return purgePeriod;
    }

    public void setPurgePeriod(TimePeriodModel purgePeriod) {
        this.purgePeriod = purgePeriod;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getUseIntegralUnit() {
        return useIntegralUnit;
    }

    public void setUseIntegralUnit(Boolean useIntegralUnit) {
        this.useIntegralUnit = useIntegralUnit;
    }

    public String getIntegralUnit() {
        return integralUnit;
    }

    public void setIntegralUnit(String integralUnit) {
        this.integralUnit = integralUnit;
    }

    public Boolean getUseRenderedUnit() {
        return useRenderedUnit;
    }

    public void setUseRenderedUnit(Boolean useRenderedUnit) {
        this.useRenderedUnit = useRenderedUnit;
    }

    public String getRenderedUnit() {
        return renderedUnit;
    }

    public void setRenderedUnit(String renderedUnit) {
        this.renderedUnit = renderedUnit;
    }

    public AbstractPointLocatorModel<?> getPointLocator() {
        return pointLocator;
    }

    public void setPointLocator(AbstractPointLocatorModel<?> pointLocator) {
        this.pointLocator = pointLocator;
    }

    public String getChartColour() {
        return chartColour;
    }

    public void setChartColour(String chartColour) {
        this.chartColour = chartColour;
    }

    public String getPlotType() {
        return plotType;
    }

    public void setPlotType(String plotType) {
        this.plotType = plotType;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public LoggingPropertiesModel getLoggingProperties() {
        return loggingProperties;
    }

    public void setLoggingProperties(LoggingPropertiesModel loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public BaseTextRendererModel<?> getTextRenderer() {
        return textRenderer;
    }

    public void setTextRenderer(BaseTextRendererModel<?> textRenderer) {
        this.textRenderer = textRenderer;
    }

    public Integer getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Integer dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceXid() {
        return dataSourceXid;
    }

    public void setDataSourceXid(String dataSourceXid) {
        this.dataSourceXid = dataSourceXid;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getDataSourceTypeName() {
        return dataSourceTypeName;
    }

    public void setDataSourceTypeName(String dataSourceTypeName) {
        this.dataSourceTypeName = dataSourceTypeName;
    }

    public String getRollup() {
        return rollup;
    }

    public void setRollup(String rollup) {
        this.rollup = rollup;
    }

    protected void setMergeTags(boolean mergeTags) {
        this.mergeTags = mergeTags;
    }

    public String getSimplifyType() {
        return simplifyType;
    }

    public void setSimplifyType(String simplifyType) {
        this.simplifyType = simplifyType;
    }

    public Double getSimplifyTolerance() {
        return simplifyTolerance;
    }

    public void setSimplifyTolerance(Double simplifyTolerance) {
        this.simplifyTolerance = simplifyTolerance;
    }

    public Integer getSimplifyTarget() {
        return simplifyTarget;
    }

    public void setSimplifyTarget(Integer simplifyTarget) {
        this.simplifyTarget = simplifyTarget;
    }

    public Boolean getPreventSetExtremeValues() {
        return preventSetExtremeValues;
    }

    public void setPreventSetExtremeValues(Boolean preventSetExtremeValues) {
        this.preventSetExtremeValues = preventSetExtremeValues;
    }

    public Double getSetExtremeLowLimit() {
        return setExtremeLowLimit;
    }

    public void setSetExtremeLowLimit(Double setExtremeLowLimit) {
        this.setExtremeLowLimit = setExtremeLowLimit;
    }

    public Double getSetExtremeHighLimit() {
        return setExtremeHighLimit;
    }

    public void setSetExtremeHighLimit(Double setExtremeHighLimit) {
        this.setExtremeHighLimit = setExtremeHighLimit;
    }

    public Set<String> getDataSourceEditRoles() {
        return dataSourceEditRoles;
    }

    public void setDataSourceEditRoles(Set<String> dataSourceEditRoles) {
        //No-op
    }

    public String getExtendedName() {
        return extendedName;
    }

    @Override
    protected DataPointVO newVO() {
        return new DataPointVO();
    }
}
