/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.infiniteautomation.mango.db.query.StreamableSqlQuery;
import com.infiniteautomation.mango.db.query.appender.SQLColumnQueryAppender;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.ILifecycle;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.provider.Providers;
import com.serotonin.util.properties.ReloadingProperties;

import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;

/**
 * @author Terry Packer
 *
 */
public class WatchlistSqlVisitorTest{
	
	protected final File baseTestDir = new File("junit");
	
	protected Map<String,String> modelMap = new HashMap<String,String>();
	//Map of Vo member/sql column to value converter
	protected Map<String, SQLColumnQueryAppender> appenders = new HashMap<String, SQLColumnQueryAppender>();
	
	@Test
	public void testRQL() throws IOException{
		
		this.configure(baseTestDir);

        //Create a User
        User user = new User();
        user.setId(1);
        user.setUsername("test");
        user.setPermissions("user,test,permission1");
        
		String rql = "limit(100,0)";
		RQLParser parser = new RQLParser();
        ASTNode root = null;
        ASTNode queryNode = parser.parse(rql);

        //Combine the existing query with an AND node
        
        if(queryNode == null){
        	root = new ASTNode("eq", "userId", user.getId());
        }else{
        	//Filter by Permissions
    		Set<String> permissions = Permissions.explodePermissionGroups(user.getPermissions());
    		ASTNode permRQL = new ASTNode("in", "readPermission", permissions);
        	
        	root = new ASTNode("or",  new ASTNode("eq", "userId", user.getId()), permRQL, queryNode);
        }
        
        MappedRowCallback<WatchListVO> selectCallback = null;
        MappedRowCallback<Long> countCallback = null;
        
        
		StreamableSqlQuery<WatchListVO> query = WatchListDao.instance.createQuery(root, selectCallback, countCallback, modelMap, appenders);
		
		System.out.println(query.toString());
        
	}
	
	
	protected void configure(File baseTestDir) throws IOException{
		
		delete(baseTestDir);
		
		Common.MA_HOME = System.getProperty("ma.home");
		if(Common.MA_HOME == null)
			throw new ShouldNeverHappenException("ma.home system property not defined.");
		
		MockLifecycle lifecycle = new MockLifecycle();
        Providers.add(ILifecycle.class, lifecycle);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Providers.get(ILifecycle.class).terminate();
            }
        });
		
		Common.envProps = new ReloadingProperties("test-env");
		Common.envProps.setDefaultValue("db.url", "jdbc:h2:" + baseTestDir.getAbsolutePath() + File.separator + "h2");
		Common.envProps.setDefaultValue("db.location", baseTestDir.getAbsolutePath() + File.separator + "h2");
		Common.envProps.setDefaultValue("db.nosql.location", baseTestDir.getAbsolutePath());
		
		Common.databaseProxy = DatabaseProxy.createDatabaseProxy();
		Common.databaseProxy.initialize(ClassLoader.getSystemClassLoader());
	}

	
	/**
	 * Delete this file or if a directory all files and directories within
	 * @param f
	 * @throws IOException
	 */
	public static void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
	    if (!f.delete())
		    throw new FileNotFoundException("Failed to delete file: " + f);
	}
}
