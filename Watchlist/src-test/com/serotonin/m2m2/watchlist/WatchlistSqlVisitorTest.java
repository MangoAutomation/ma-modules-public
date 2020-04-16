/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jooq.Field;
import org.junit.BeforeClass;
import org.junit.Test;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.dao.WatchListDao;
import com.infiniteautomation.mango.spring.db.UserTableDefinition;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.WatchListService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.module.ModuleElementDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;

/**
 *
 * Simple RQL test for watchlist Dao
 *
 * @author Terry Packer
 *
 */
public class WatchlistSqlVisitorTest extends MangoTestBase {

    protected Map<String,String> modelMap = new HashMap<String,String>();
    protected WatchListService service;

    @BeforeClass
    public static void setupModule() {
        List<ModuleElementDefinition> definitions = new ArrayList<>();
        definitions.add(new WatchListSchemaDefinition());
        definitions.add(new AuditEvent());
        addModule("watchlist", definitions);
    }

    @Override
    public void before() {
        super.before();
        this.service = Common.getBean(WatchListService.class);
    }

    @Test
    public void testRQL() throws IOException{

        //Create a User
        List<User> users = createUsers(1, PermissionHolder.SUPERADMIN_ROLE, PermissionHolder.USER_ROLE);

        //Insert some watchlists
        for(int i=0; i<5; i++) {
            WatchListVO wl = new WatchListVO();
            wl.setXid(WatchListDao.getInstance().generateUniqueXid());
            wl.setName("Watchilst " + i);
            wl.setUserId(users.get(0).getId());
            wl.setReadPermission(MangoPermission.createOrSet(users.get(0).getRoles()));
            WatchListDao.getInstance().insert(wl);
        }


        String rql = "eq(username," + users.get(0).getUsername() + ")&limit(3)";
        RQLParser parser = new RQLParser();
        ASTNode query = parser.parse(rql);

        final AtomicLong selectCounter = new AtomicLong();

        Map<String, Field<?>> fieldMap = new HashMap<>();
        fieldMap.put("username", Common.getBean(UserTableDefinition.class).getXidAlias());

        Common.getBean(PermissionService.class).runAsSystemAdmin(() -> {

            service.customizedQuery(query, (wl,index) -> {
                selectCounter.incrementAndGet();
                assertEquals(users.get(0).getId(), wl.getUserId());
                assertEquals(2, wl.getReadPermission().getUniqueRoles().size());
            });
            assertEquals(5, service.customizedCount(query));
        });
        assertEquals(3, selectCounter.get());
    }

}
