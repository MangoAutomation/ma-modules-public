/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.List;

/**
 *
 * @author Terry Packer
 */
public class SqlQueryResult {

    private List<String> headers;
    private List<List<Object>> data;
    
    public SqlQueryResult(List<String> headers, List<List<Object>> data) {
        this.headers = headers;
        this.data = data;
    }
    
    public SqlQueryResult() { }

    public List<String> getHeaders() {
        return headers;
    }
    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
    public List<List<Object>> getData() {
        return data;
    }
    public void setData(List<List<Object>> data) {
        this.data = data;
    }
}
