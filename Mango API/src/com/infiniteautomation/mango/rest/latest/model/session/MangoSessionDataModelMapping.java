/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.session;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.MangoSessionDataVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@ConditionalOnProperty(value = {"${testing.enabled:false}", "${testing.restApi.enabled:false}"})
@Component
public class MangoSessionDataModelMapping implements RestModelMapping<MangoSessionDataVO, MangoSessionDataModel> {

    private final UserDao dao;

    @Autowired
    public MangoSessionDataModelMapping(UserDao dao) {
        this.dao = dao;
    }

    @Override
    public Class<? extends MangoSessionDataVO> fromClass() {
        return MangoSessionDataVO.class;
    }

    @Override
    public Class<? extends MangoSessionDataModel> toClass() {
        return MangoSessionDataModel.class;
    }

    @Override
    public MangoSessionDataModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        MangoSessionDataVO source = (MangoSessionDataVO)from;
        MangoSessionDataModel result = new MangoSessionDataModel();

        result.setSessionId(source.getSessionId());
        result.setContextPath(source.getContextPath());
        result.setVirtualHost(source.getVirtualHost());
        result.setLastNode(source.getLastNode());
        result.setAccessTime(new Date(source.getAccessTime()));
        result.setLastAccessTime(new Date(source.getLastAccessTime()));
        result.setCreateTime(new Date(source.getCreateTime()));
        result.setCookieTime(new Date(source.getCookieTime()));
        result.setLastSavedTime(source.getLastSavedTime());
        result.setExpiryTime(new Date(source.getExpiryTime()));
        result.setMaxInterval(source.getMaxInterval());

        User owner = dao.get(source.getUserId());
        if(user != null) {
            result.setUsername(owner.getUsername());
        }
        return result;
    }

    @Override
    public MangoSessionDataVO unmap(Object from, PermissionHolder user, RestModelMapper mapper) {
        MangoSessionDataModel source = (MangoSessionDataModel)from;
        MangoSessionDataVO result = new MangoSessionDataVO();

        result.setSessionId(source.getSessionId());
        result.setContextPath(source.getContextPath());
        result.setVirtualHost(source.getVirtualHost());
        result.setLastNode(source.getLastNode());
        result.setAccessTime(source.getAccessTime() != null ? source.getAccessTime().getTime() : 0l);
        result.setLastAccessTime(source.getLastAccessTime() != null ? source.getLastAccessTime().getTime() : 0l);
        result.setCreateTime(source.getCreateTime() != null ? source.getCreateTime().getTime() : 0l);
        result.setCookieTime(source.getCookieTime() != null ? source.getCookieTime().getTime() : 0l);
        result.setLastSavedTime(source.getLastSavedTime());
        result.setExpiryTime(source.getExpiryTime() != null ? source.getExpiryTime().getTime() : 0l);
        result.setMaxInterval(source.getMaxInterval());

        User owner = dao.getByXid(source.getUsername());
        if(user != null) {
            result.setUserId(owner.getId());
        }
        return result;
    }

}
