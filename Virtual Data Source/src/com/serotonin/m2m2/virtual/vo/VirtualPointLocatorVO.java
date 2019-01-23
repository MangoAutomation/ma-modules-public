/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.util.IntMessagePair;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.m2m2.virtual.rt.VirtualPointLocatorRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class VirtualPointLocatorVO extends AbstractPointLocatorVO<VirtualPointLocatorVO> implements
		JsonSerializable {
	private static final Log LOG = LogFactory
			.getLog(VirtualPointLocatorVO.class);

	@Override
	public TranslatableMessage getConfigurationDescription() {
		ChangeTypeVO changeType = getChangeType();
		if (changeType == null)
			throw new ShouldNeverHappenException("unknown change type");
		return changeType.getDescription();
	}

	private ChangeTypeVO getChangeType() {
		if (changeTypeId == ChangeTypeVO.Types.ALTERNATE_BOOLEAN)
			return alternateBooleanChange;
		if (changeTypeId == ChangeTypeVO.Types.BROWNIAN)
			return brownianChange;
		if (changeTypeId == ChangeTypeVO.Types.INCREMENT_ANALOG)
			return incrementAnalogChange;
		if (changeTypeId == ChangeTypeVO.Types.INCREMENT_MULTISTATE)
			return incrementMultistateChange;
		if (changeTypeId == ChangeTypeVO.Types.NO_CHANGE)
			return noChange;
		if (changeTypeId == ChangeTypeVO.Types.RANDOM_ANALOG)
			return randomAnalogChange;
		if (changeTypeId == ChangeTypeVO.Types.RANDOM_BOOLEAN)
			return randomBooleanChange;
		if (changeTypeId == ChangeTypeVO.Types.RANDOM_MULTISTATE)
			return randomMultistateChange;
		if (changeTypeId == ChangeTypeVO.Types.ANALOG_ATTRACTOR)
			return analogAttractorChange;
		if (changeTypeId == ChangeTypeVO.Types.SINUSOIDAL)
			return sinusoidalChange;
		LOG.error("Failed to resolve changeTypeId " + changeTypeId
				+ " for virtual point locator");
		return alternateBooleanChange;
	}

	@Override
	public PointLocatorRT<VirtualPointLocatorVO> createRuntime() {
		ChangeTypeRT changeType = getChangeType().createRuntime();
		String startValue = getChangeType().getStartValue();
		DataValue startObject;
		if (dataTypeId == DataTypes.BINARY)
			startObject = BinaryValue.parseBinary(startValue);
		else if (dataTypeId == DataTypes.MULTISTATE) {
			try {
				startObject = MultistateValue.parseMultistate(startValue);
			} catch (NumberFormatException e) {
				startObject = new MultistateValue(0);
			}
		} else if (dataTypeId == DataTypes.NUMERIC) {
			try {
				startObject = NumericValue.parseNumeric(startValue);
			} catch (NumberFormatException e) {
				startObject = new NumericValue(0);
			}
		} else {
			if (startValue == null)
				startObject = new AlphanumericValue("");
			else
				startObject = new AlphanumericValue(startValue);
		}
		return new VirtualPointLocatorRT(this, changeType, startObject, isSettable());
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#validate(com.serotonin.m2m2.i18n.ProcessResult, com.serotonin.m2m2.vo.DataPointVO, com.serotonin.m2m2.vo.dataSource.DataSourceVO)
	 */
	@Override
	public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO<?> dsvo) {
	    if (!(dsvo instanceof VirtualDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");     
		if (!DataTypes.CODES.isValidId(dataTypeId))
			response.addContextualMessage("dataTypeId", "validate.invalidValue");

		// Alternate boolean
		if (changeTypeId == ChangeTypeVO.Types.ALTERNATE_BOOLEAN) {
			if (StringUtils.isBlank(alternateBooleanChange.getStartValue()))
				response.addContextualMessage(
						"alternateBooleanChange.startValue",
						"validate.required");
		}

		// Brownian
		else if (changeTypeId == ChangeTypeVO.Types.BROWNIAN) {
			if (brownianChange.getMin() >= brownianChange.getMax())
				response.addContextualMessage("brownianChange.max",
						"validate.maxGreaterThanMin");
			if (brownianChange.getMaxChange() <= 0)
				response.addContextualMessage("brownianChange.maxChange",
						"validate.greaterThanZero");
			if (StringUtils.isBlank(brownianChange.getStartValue()))
				response.addContextualMessage("brownianChange.startValue",
						"validate.required");
		}

		// Increment analog
		else if (changeTypeId == ChangeTypeVO.Types.INCREMENT_ANALOG) {
			if (incrementAnalogChange.getMin() >= incrementAnalogChange
					.getMax())
				response.addContextualMessage("incrementAnalogChange.max",
						"validate.maxGreaterThanMin");
//			if (incrementAnalogChange.getChange() <= 0)
//				response.addContextualMessage("incrementAnalogChange.change",
//						"validate.greaterThanZero");
			if (StringUtils.isBlank(incrementAnalogChange.getStartValue()))
				response.addContextualMessage(
						"incrementAnalogChange.startValue", "validate.required");
		}

		// Increment multistate
		else if (changeTypeId == ChangeTypeVO.Types.INCREMENT_MULTISTATE) {
			if (ArrayUtils.isEmpty(incrementMultistateChange.getValues()))
				response.addContextualMessage(
						"incrementMultistateChange.values", "validate.atLeast1");
			if (StringUtils.isBlank(incrementMultistateChange.getStartValue()))
				response.addContextualMessage(
						"incrementMultistateChange.startValue",
						"validate.required");
		}

		// No change
		else if (changeTypeId == ChangeTypeVO.Types.NO_CHANGE) {
			if (StringUtils.isBlank(noChange.getStartValue())
					&& dataTypeId != DataTypes.ALPHANUMERIC)
				response.addContextualMessage("noChange.startValue",
						"validate.required");
		}

		// Random analog
		else if (changeTypeId == ChangeTypeVO.Types.RANDOM_ANALOG) {
			if (randomAnalogChange.getMin() >= randomAnalogChange.getMax())
				response.addContextualMessage("randomAnalogChange.max",
						"validate.maxGreaterThanMin");
			if (StringUtils.isBlank(randomAnalogChange.getStartValue()))
				response.addContextualMessage("randomAnalogChange.startValue",
						"validate.required");
		}

		// Random boolean
		else if (changeTypeId == ChangeTypeVO.Types.RANDOM_BOOLEAN) {
			if (StringUtils.isBlank(randomBooleanChange.getStartValue()))
				response.addContextualMessage("randomBooleanChange.startValue",
						"validate.required");
		}

		// Random multistate
		else if (changeTypeId == ChangeTypeVO.Types.RANDOM_MULTISTATE) {
			if (ArrayUtils.isEmpty(randomMultistateChange.getValues()))
				response.addContextualMessage("randomMultistateChange.values",
						"validate.atLeast1");
			if (StringUtils.isBlank(randomMultistateChange.getStartValue()))
				response.addContextualMessage(
						"randomMultistateChange.startValue",
						"validate.required");
		}

		// Analog attractor
		else if (changeTypeId == ChangeTypeVO.Types.ANALOG_ATTRACTOR) {
			if (analogAttractorChange.getMaxChange() <= 0)
				response.addContextualMessage(
						"analogAttractorChange.maxChange",
						"validate.greaterThanZero");
			if (analogAttractorChange.getVolatility() < 0)
				response.addContextualMessage(
						"analogAttractorChange.volatility",
						"validate.cannotBeNegative");
			if (analogAttractorChange.getAttractionPointId() < 1)
				response.addContextualMessage(
						"analogAttractorChange.attractionPointId",
						"validate.required");
			if (StringUtils.isBlank(analogAttractorChange.getStartValue()))
				response.addContextualMessage(
						"analogAttractorChange.startValue", "validate.required");
		}
		// Analog attractor
		else if (changeTypeId == ChangeTypeVO.Types.SINUSOIDAL) {
			// Nothing to validate here
		} else
			response.addContextualMessage("changeTypeId",
					"validate.invalidChoice");

		ChangeTypeVO changeType = getChangeType();
		if (changeType != null) {
			boolean found = false;
			for (IntMessagePair imp : ChangeTypeVO.getChangeTypes(dataTypeId)) {
				if (imp.getKey() == changeTypeId) {
					found = true;
					break;
				}
			}

			if (!found)
				response.addGenericMessage("virtual.changeType.incompatible");
		}
	}

	private int dataTypeId = DataTypes.BINARY;
	private int changeTypeId = ChangeTypeVO.Types.ALTERNATE_BOOLEAN;
	@JsonProperty
	private boolean settable;
	private AlternateBooleanChangeVO alternateBooleanChange = new AlternateBooleanChangeVO();
	private BrownianChangeVO brownianChange = new BrownianChangeVO();
	private IncrementAnalogChangeVO incrementAnalogChange = new IncrementAnalogChangeVO();
	private IncrementMultistateChangeVO incrementMultistateChange = new IncrementMultistateChangeVO();
	private NoChangeVO noChange = new NoChangeVO();
	private RandomAnalogChangeVO randomAnalogChange = new RandomAnalogChangeVO();
	private RandomBooleanChangeVO randomBooleanChange = new RandomBooleanChangeVO();
	private RandomMultistateChangeVO randomMultistateChange = new RandomMultistateChangeVO();
	private AnalogAttractorChangeVO analogAttractorChange = new AnalogAttractorChangeVO();
	private SinusoidalChangeVO sinusoidalChange = new SinusoidalChangeVO();

	public int getChangeTypeId() {
		return changeTypeId;
	}

	public void setChangeTypeId(int changeTypeId) {
		this.changeTypeId = changeTypeId;
	}

	@Override
	public int getDataTypeId() {
		return dataTypeId;
	}

	public void setDataTypeId(int dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	public AlternateBooleanChangeVO getAlternateBooleanChange() {
		return alternateBooleanChange;
	}

	public BrownianChangeVO getBrownianChange() {
		return brownianChange;
	}

	public IncrementAnalogChangeVO getIncrementAnalogChange() {
		return incrementAnalogChange;
	}

	public IncrementMultistateChangeVO getIncrementMultistateChange() {
		return incrementMultistateChange;
	}

	public NoChangeVO getNoChange() {
		return noChange;
	}

	public RandomAnalogChangeVO getRandomAnalogChange() {
		return randomAnalogChange;
	}

	public RandomBooleanChangeVO getRandomBooleanChange() {
		return randomBooleanChange;
	}

	public RandomMultistateChangeVO getRandomMultistateChange() {
		return randomMultistateChange;
	}

	public SinusoidalChangeVO getSinusoidalChange() {
		return sinusoidalChange;
	}

	@Override
	public boolean isSettable() {
		return settable;
	}

	public void setSettable(boolean settable) {
		this.settable = settable;
	}

	public AnalogAttractorChangeVO getAnalogAttractorChange() {
		return analogAttractorChange;
	}

	public void setAlternateBooleanChange(
			AlternateBooleanChangeVO alternateBooleanChange) {
		this.alternateBooleanChange = alternateBooleanChange;
	}

	public void setBrownianChange(BrownianChangeVO brownianChange) {
		this.brownianChange = brownianChange;
	}

	public void setIncrementAnalogChange(
			IncrementAnalogChangeVO incrementAnalogChange) {
		this.incrementAnalogChange = incrementAnalogChange;
	}

	public void setIncrementMultistateChange(
			IncrementMultistateChangeVO incrementMultistateChange) {
		this.incrementMultistateChange = incrementMultistateChange;
	}

	public void setNoChange(NoChangeVO noChange) {
		this.noChange = noChange;
	}

	public void setRandomAnalogChange(RandomAnalogChangeVO randomAnalogChange) {
		this.randomAnalogChange = randomAnalogChange;
	}

	public void setRandomBooleanChange(RandomBooleanChangeVO randomBooleanChange) {
		this.randomBooleanChange = randomBooleanChange;
	}

	public void setRandomMultistateChange(
			RandomMultistateChangeVO randomMultistateChange) {
		this.randomMultistateChange = randomMultistateChange;
	}

	public void setAnalogAttractorChange(
			AnalogAttractorChangeVO analogAttractorChange) {
		this.analogAttractorChange = analogAttractorChange;
	}

	public void setSinusoidalChange(SinusoidalChangeVO sinusoidal) {
		this.sinusoidalChange = sinusoidal;
	}

	//
	//
	// Serialization
	//
	private static final long serialVersionUID = -1;
	private static final int version = 2;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);
		out.writeInt(dataTypeId);
		out.writeInt(changeTypeId);
		out.writeBoolean(settable);
		out.writeObject(alternateBooleanChange);
		out.writeObject(brownianChange);
		out.writeObject(incrementAnalogChange);
		out.writeObject(incrementMultistateChange);
		out.writeObject(noChange);
		out.writeObject(randomAnalogChange);
		out.writeObject(randomBooleanChange);
		out.writeObject(randomMultistateChange);
		out.writeObject(analogAttractorChange);
		out.writeObject(sinusoidalChange);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            dataTypeId = in.readInt();
            changeTypeId = in.readInt();
            settable = in.readBoolean();
            alternateBooleanChange = (AlternateBooleanChangeVO) in.readObject();
            brownianChange = (BrownianChangeVO) in.readObject();
            incrementAnalogChange = (IncrementAnalogChangeVO) in.readObject();
            incrementMultistateChange = (IncrementMultistateChangeVO) in.readObject();
            noChange = (NoChangeVO) in.readObject();
            randomAnalogChange = (RandomAnalogChangeVO) in.readObject();
            randomBooleanChange = (RandomBooleanChangeVO) in.readObject();
            randomMultistateChange = (RandomMultistateChangeVO) in.readObject();
            analogAttractorChange = (AnalogAttractorChangeVO) in.readObject();
            sinusoidalChange = new SinusoidalChangeVO();
        }
        if(ver == 2){            
        	dataTypeId = in.readInt();
	        changeTypeId = in.readInt();
	        settable = in.readBoolean();
	        alternateBooleanChange = (AlternateBooleanChangeVO) in.readObject();
	        brownianChange = (BrownianChangeVO) in.readObject();
	        incrementAnalogChange = (IncrementAnalogChangeVO) in.readObject();
	        incrementMultistateChange = (IncrementMultistateChangeVO) in.readObject();
	        noChange = (NoChangeVO) in.readObject();
	        randomAnalogChange = (RandomAnalogChangeVO) in.readObject();
	        randomBooleanChange = (RandomBooleanChangeVO) in.readObject();
	        randomMultistateChange = (RandomMultistateChangeVO) in.readObject();
	        analogAttractorChange = (AnalogAttractorChangeVO) in.readObject();
        	sinusoidalChange = (SinusoidalChangeVO) in.readObject();
        }
    }

	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException,
			JsonException {
		writeDataType(writer);
		writer.writeEntry("changeType", getChangeType());
		
	}

	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject)
			throws JsonException {
		Integer value = readDataType(jsonObject, DataTypes.IMAGE);
		if (value != null)
			dataTypeId = value;

		JsonObject ctjson = jsonObject.getJsonObject("changeType");
		if (ctjson == null)
			throw new TranslatableJsonException("emport.error.missingObject",
					"changeType");

		String text = ctjson.getString("type");
		if (text == null)
			throw new TranslatableJsonException("emport.error.missing", "type",
					ChangeTypeVO.CHANGE_TYPE_CODES.getCodeList());

		changeTypeId = ChangeTypeVO.CHANGE_TYPE_CODES.getId(text);
		if (changeTypeId == -1)
			throw new TranslatableJsonException("emport.error.invalid",
					"changeType", text,
					ChangeTypeVO.CHANGE_TYPE_CODES.getCodeList());

		reader.readInto(getChangeType(), ctjson);
	}
}
