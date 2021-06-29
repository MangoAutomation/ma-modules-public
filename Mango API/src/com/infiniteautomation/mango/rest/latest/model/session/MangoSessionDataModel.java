/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.session;

import java.util.Date;

/**
 *
 * @author Terry Packer
 */
public class MangoSessionDataModel {

    private String sessionId;
    private String contextPath;
    private String virtualHost;
    private String lastNode;
    private Date accessTime;
    private Date lastAccessTime;
    private Date createTime;
    private Date cookieTime;
    private long lastSavedTime;
    private Date expiryTime;
    private long maxInterval;
    private String username;

    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getContextPath() {
        return contextPath;
    }
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    public String getVirtualHost() {
        return virtualHost;
    }
    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }
    public String getLastNode() {
        return lastNode;
    }
    public void setLastNode(String lastNode) {
        this.lastNode = lastNode;
    }
    public Date getAccessTime() {
        return accessTime;
    }
    public void setAccessTime(Date accessTime) {
        this.accessTime = accessTime;
    }
    public Date getLastAccessTime() {
        return lastAccessTime;
    }
    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public Date getCookieTime() {
        return cookieTime;
    }
    public void setCookieTime(Date cookieTime) {
        this.cookieTime = cookieTime;
    }
    public long getLastSavedTime() {
        return lastSavedTime;
    }
    public void setLastSavedTime(long lastSavedTime) {
        this.lastSavedTime = lastSavedTime;
    }
    public Date getExpiryTime() {
        return expiryTime;
    }
    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }
    public long getMaxInterval() {
        return maxInterval;
    }
    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
