/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.permission.Permissions.DataPointAccessTypes;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserAccessModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="User Access", description="User Access")
@RestController
@RequestMapping("/access")
public class UserAccessRestController extends MangoRestController{
	
	//private static Log LOG = LogFactory.getLog(UserAccessRestController.class);
	
	public UserAccessRestController(){
	}

	
	@ApiOperation(value = "Get Data Point Access List", notes = "Returns a list of users and thier access")
	@RequestMapping(method = RequestMethod.GET, value = "/data-point/{xid}")
    public ResponseEntity<List<UserAccessModel>> getDataPointAccess(
    		@ApiParam(value = "Valid data point xid", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {
		
		RestProcessResult<List<UserAccessModel>> result = new RestProcessResult<List<UserAccessModel>>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		
    		DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
    		if(vo != null){
    			List<UserAccessModel> models = new ArrayList<UserAccessModel>();
    			List<User> allUsers = UserDao.getInstance().getUsers();
                int accessType;
                for (User mangoUser : allUsers) {
                    accessType = Permissions.getDataPointAccessType(mangoUser, vo);
                    if (accessType != Permissions.DataPointAccessTypes.NONE) {
                        models.add(new UserAccessModel(Permissions.ACCESS_TYPE_CODES.getCode(accessType), new UserModel(mangoUser)));
                    }
                }
                return result.createResponseEntity(models);
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Get Data Source Access List", notes = "Returns a list of users and thier access")
	@RequestMapping(method = RequestMethod.GET, value = "/data-source/{xid}")
    public ResponseEntity<List<UserAccessModel>> getDataSourceAccess(
    		@ApiParam(value = "Valid data point xid", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {
		
		RestProcessResult<List<UserAccessModel>> result = new RestProcessResult<List<UserAccessModel>>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		
    		DataSourceVO<?> vo = DataSourceDao.getInstance().getByXid(xid);
    		if(vo != null){
    			List<UserAccessModel> models = new ArrayList<UserAccessModel>();
    			List<User> allUsers = UserDao.getInstance().getUsers();
                for (User mangoUser : allUsers) {
                    if(Permissions.hasDataSourcePermission(mangoUser, vo)){
                        models.add(new UserAccessModel(Permissions.ACCESS_TYPE_CODES.getCode(DataPointAccessTypes.DATA_SOURCE), new UserModel(mangoUser)));
                    }
                }
                return result.createResponseEntity(models);
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
}
