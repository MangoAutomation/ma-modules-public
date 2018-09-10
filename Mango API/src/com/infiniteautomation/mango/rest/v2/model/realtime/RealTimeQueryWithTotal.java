/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.realtime;

import java.util.List;

/**
 *
 * @author Terry Packer
 */
public class RealTimeQueryWithTotal {

    private int total;
    private List<RealTimeDataPointValueModel> items;
    
    public RealTimeQueryWithTotal() { }

    public RealTimeQueryWithTotal(int total, List<RealTimeDataPointValueModel> items) {
        super();
        this.total = total;
        this.items = items;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public List<RealTimeDataPointValueModel> getItems() {
        return items;
    }
    public void setItems(List<RealTimeDataPointValueModel> items) {
        this.items = items;
    }
    
    
}
