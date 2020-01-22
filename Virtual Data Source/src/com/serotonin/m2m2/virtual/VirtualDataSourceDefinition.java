/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.util.IntMessagePair;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

public class VirtualDataSourceDefinition extends DataSourceDefinition<VirtualDataSourceVO> {

    public static final String TYPE_NAME = "VIRTUAL";

    @Override
    public String getDataSourceTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "VIRTUAL.dataSource";
    }

    @Override
    public VirtualDataSourceVO createDataSourceVO() {
        return new VirtualDataSourceVO();
    }

    @Override
    public void validate(ProcessResult response, VirtualDataSourceVO ds, PermissionHolder user) { }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO dsvo, PermissionHolder user) {
        if (!(dsvo instanceof VirtualDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");

        VirtualPointLocatorVO pl = dpvo.getPointLocator();

        if (!DataTypes.CODES.isValidId(pl.getDataTypeId()))
            response.addContextualMessage("dataTypeId", "validate.invalidValue");



        // Alternate boolean
        if (pl.getChangeTypeId() == ChangeTypeVO.Types.ALTERNATE_BOOLEAN) {
            if (StringUtils.isBlank(pl.getAlternateBooleanChange().getStartValue()))
                response.addContextualMessage(
                        "alternateBooleanChange.startValue",
                        "validate.required");
        }

        // Brownian
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.BROWNIAN) {
            if (pl.getBrownianChange().getMin() >= pl.getBrownianChange().getMax())
                response.addContextualMessage("brownianChange.max",
                        "validate.maxGreaterThanMin");
            if (pl.getBrownianChange().getMaxChange() <= 0)
                response.addContextualMessage("brownianChange.maxChange",
                        "validate.greaterThanZero");
            if (StringUtils.isBlank(pl.getBrownianChange().getStartValue()))
                response.addContextualMessage("brownianChange.startValue",
                        "validate.required");
        }

        // Increment analog
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.INCREMENT_ANALOG) {
            if (pl.getIncrementAnalogChange().getMin() >= pl.getIncrementAnalogChange()
                    .getMax())
                response.addContextualMessage("incrementAnalogChange.max",
                        "validate.maxGreaterThanMin");
            if (StringUtils.isBlank(pl.getIncrementAnalogChange().getStartValue()))
                response.addContextualMessage(
                        "incrementAnalogChange.startValue", "validate.required");
        }

        // Increment multistate
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.INCREMENT_MULTISTATE) {
            if (ArrayUtils.isEmpty(pl.getIncrementMultistateChange().getValues()))
                response.addContextualMessage(
                        "incrementMultistateChange.values", "validate.atLeast1");
            if (StringUtils.isBlank(pl.getIncrementMultistateChange().getStartValue()))
                response.addContextualMessage(
                        "incrementMultistateChange.startValue",
                        "validate.required");
        }

        // No change
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.NO_CHANGE) {
            if (StringUtils.isBlank(pl.getNoChange().getStartValue())
                    && pl.getDataTypeId() != DataTypes.ALPHANUMERIC)
                response.addContextualMessage("noChange.startValue",
                        "validate.required");
        }

        // Random analog
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.RANDOM_ANALOG) {
            if (pl.getRandomAnalogChange().getMin() >= pl.getRandomAnalogChange().getMax())
                response.addContextualMessage("randomAnalogChange.max",
                        "validate.maxGreaterThanMin");
            if (StringUtils.isBlank(pl.getRandomAnalogChange().getStartValue()))
                response.addContextualMessage("randomAnalogChange.startValue",
                        "validate.required");
        }

        // Random boolean
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.RANDOM_BOOLEAN) {
            if (StringUtils.isBlank(pl.getRandomBooleanChange().getStartValue()))
                response.addContextualMessage("randomBooleanChange.startValue",
                        "validate.required");
        }

        // Random multistate
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.RANDOM_MULTISTATE) {
            if (ArrayUtils.isEmpty(pl.getRandomMultistateChange().getValues()))
                response.addContextualMessage("randomMultistateChange.values",
                        "validate.atLeast1");
            if (StringUtils.isBlank(pl.getRandomMultistateChange().getStartValue()))
                response.addContextualMessage(
                        "randomMultistateChange.startValue",
                        "validate.required");
        }

        // Analog attractor
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.ANALOG_ATTRACTOR) {
            if (pl.getAnalogAttractorChange().getMaxChange() <= 0)
                response.addContextualMessage(
                        "analogAttractorChange.maxChange",
                        "validate.greaterThanZero");
            if (pl.getAnalogAttractorChange().getVolatility() < 0)
                response.addContextualMessage(
                        "analogAttractorChange.volatility",
                        "validate.cannotBeNegative");
            if (pl.getAnalogAttractorChange().getAttractionPointId() < 1)
                response.addContextualMessage(
                        "analogAttractorChange.attractionPointId",
                        "validate.required");
            if (StringUtils.isBlank(pl.getAnalogAttractorChange().getStartValue()))
                response.addContextualMessage(
                        "analogAttractorChange.startValue", "validate.required");
        }
        // Analog attractor
        else if (pl.getChangeTypeId() == ChangeTypeVO.Types.SINUSOIDAL) {
            // Nothing to validate here
        } else
            response.addContextualMessage("changeTypeId",
                    "validate.invalidChoice");

        ChangeTypeVO changeType = pl.getChangeType();
        if (changeType != null) {
            boolean found = false;
            for (IntMessagePair imp : ChangeTypeVO.getChangeTypes(pl.getDataTypeId())) {
                if (imp.getKey() == pl.getChangeTypeId()) {
                    found = true;
                    break;
                }
            }

            if (!found)
                response.addGenericMessage("virtual.changeType.incompatible");
        }
    }

}
