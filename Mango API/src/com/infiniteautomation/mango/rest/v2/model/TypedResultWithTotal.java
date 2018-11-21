/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.List;

/**
 * @author Terry Packer
 *
 */
public interface TypedResultWithTotal <T> {

    List<T> getItems();
    
    int getTotal();
    
}
