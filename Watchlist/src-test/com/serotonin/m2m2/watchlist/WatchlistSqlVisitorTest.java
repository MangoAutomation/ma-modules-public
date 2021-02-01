/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.BeforeClass;
import org.junit.Test;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.dao.WatchListDao;
import com.infiniteautomation.mango.spring.service.WatchListService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.MangoTestBase;
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
        loadModules();
    }

    @Override
    public void before() {
        super.before();
        this.service = Common.getBean(WatchListService.class);
    }

    @Test
    public void testRQL() throws IOException {

        //Create a User
        List<User> users = createUsers(1, PermissionHolder.SUPERADMIN_ROLE, PermissionHolder.USER_ROLE);

        //Insert some watchlists
        for (int i = 0; i < 5; i++) {
            WatchListVO wl = new WatchListVO();
            wl.setXid(WatchListDao.getInstance().generateUniqueXid());
            wl.setName("Watchlist " + i);
            wl.setReadPermission(MangoPermission.requireAnyRole(users.get(0).getRoles()));
            WatchListDao.getInstance().insert(wl);
        }

        //Insert a 6th odd one
        WatchListVO odd = new WatchListVO();
        odd.setXid(WatchListDao.getInstance().generateUniqueXid());
        odd.setName("Not a Watchlist ");
        odd.setReadPermission(MangoPermission.requireAnyRole(users.get(0).getRoles()));
        WatchListDao.getInstance().insert(odd);

        String rql = "like(name,Watchlist *)&limit(3)";
        RQLParser parser = new RQLParser();
        ASTNode query = parser.parse(rql);

        final AtomicLong selectCounter = new AtomicLong();

        service.customizedQuery(query, (wl) -> {
            selectCounter.incrementAndGet();
            assertTrue(wl.getName().startsWith("Watchlist "));
            assertEquals(2, wl.getReadPermission().getRoles().stream().mapToLong(Collection::size).sum());
        });
        assertEquals(5, service.customizedCount(query));
        assertEquals(3, selectCounter.get());
    }

}
