/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.infiniteautomation.mango.rest.latest.model.pointValue.XidPointValueTimeModel;

/**
 * @author Jared Wiltshire
 */
@Order(0)
@Component
public class XidPointValueTimeModelCsvConverter extends StreamCsvConverter<XidPointValueTimeModel> {

    public XidPointValueTimeModelCsvConverter(CsvMapper mapper) {
        super(mapper, XidPointValueTimeModel.class);
    }

}
