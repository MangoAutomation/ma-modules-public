/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

/**
 * Model for typical RQL query results, an object with two members, the array of items and a total.
 *
 * @author Jared Wiltshire
 */
public interface StreamedArrayWithTotal extends ArrayWithTotal<StreamedArray> {
}
