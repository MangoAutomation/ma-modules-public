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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.BeforeClass;
import org.junit.Test;

import com.infiniteautomation.mango.db.query.StreamableRowCallback;
import com.infiniteautomation.mango.db.query.StreamableSqlQuery;
import com.infiniteautomation.mango.db.query.appender.SQLColumnQueryAppender;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.module.ModuleElementDefinition;
import com.serotonin.m2m2.vo.User;

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
    //Map of Vo member/sql column to value converter
    protected Map<String, SQLColumnQueryAppender> appenders = new HashMap<String, SQLColumnQueryAppender>();

    @BeforeClass
    public static void setupModule() {
        List<ModuleElementDefinition> definitions = new ArrayList<>();
        definitions.add(new WatchListSchemaDefinition());
        definitions.add(new AuditEvent());
        addModule("watchlist", definitions);
    }

    @Test
    public void testRQL() throws IOException{

        //Create a User
        User user = new User();
        user.setUsername("test");
        user.setName("test");
        user.setEmail("test@test.com");
        user.setPassword(Common.encrypt("usernametest"));
        user.setPermissions("user,test,permission1");
        validate(user);
        UserDao.getInstance().saveUser(user);

        //Insert some watchlists
        for(int i=0; i<120; i++) {
            WatchListVO wl = new WatchListVO();
            wl.setXid(WatchListDao.getInstance().generateUniqueXid());
            wl.setName("Watchilst " + i);
            wl.setUserId(user.getId());
            wl.setReadPermission("permission1");
            WatchListDao.getInstance().saveWatchList(wl);
        }


        String rql = "limit(100,0)";
        RQLParser parser = new RQLParser();
        ASTNode root = null;
        ASTNode queryNode = parser.parse(rql);

        //Combine the existing query with an AND node
        if(queryNode == null){
            root = new ASTNode("eq", "userId", user.getId());
        }else{
            //Filter by Permissions
            Set<String> permissions = user.getPermissionsSet();
            ASTNode permRQL = new ASTNode("in", "readPermission", permissions);

            root = new ASTNode("or",  new ASTNode("eq", "userId", user.getId()), permRQL, queryNode);
        }

        final AtomicLong selectCounter = new AtomicLong();
        final AtomicLong countValue = new AtomicLong();

        StreamableRowCallback<WatchListVO> selectCallback = new StreamableRowCallback<WatchListVO>() {

            @Override
            public void row(WatchListVO row, int index) throws Exception {
                selectCounter.incrementAndGet();
            }

        };

        StreamableRowCallback<Long> countCallback = new StreamableRowCallback<Long>() {

            @Override
            public void row(Long row, int index) throws Exception {
                countValue.set(row);
            }

        };


        StreamableSqlQuery<WatchListVO> query = WatchListDao.getInstance().createQuery(root, selectCallback, countCallback, modelMap, appenders, true);
        query.query();
        query.count();

        assertEquals(100L, selectCounter.get());
        assertEquals(120L, countValue.get());
    }

}
