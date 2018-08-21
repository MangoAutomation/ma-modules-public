/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/v1/websocket/data-sources")
public class DataSourceWebSocketHandler extends DaoNotificationWebSocketHandler<DataSourceVO<?>> {

    @Override
    protected boolean hasPermission(User user, DataSourceVO<?> vo) {
        if(user.hasAdminPermission())
            return true;
        else
            return Permissions.hasDataSourcePermission(user, vo);
    }

    @Override
    protected Object createModel(DataSourceVO<?> vo) {
        return vo.asModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends DataSourceVO<?>> supportedClass() {
        return (Class<? extends DataSourceVO<?>>) DataSourceVO.class;
    }

}
