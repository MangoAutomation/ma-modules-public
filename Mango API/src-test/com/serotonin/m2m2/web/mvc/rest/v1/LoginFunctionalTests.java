/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.BaseRestTest;
import com.serotonin.m2m2.web.mvc.rest.test.data.UserTestData;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;

/**
 * @author Terry Packer
 *
 */
public class LoginFunctionalTests extends BaseRestTest{

	
	@Mock
	protected UserDao userDao;

	@InjectMocks
	protected LoginRestController mockController;
	
	@Before
    public void setup() {
    	MockitoAnnotations.initMocks(this);
    	this.setupMvc(mockController);
    	
        //Mock our Daos so they
        // return exactly what we want.
    	DaoRegistry.userDao = this.userDao;
    }
	
	@Test
	public void testLogin(){
		User standardUser = UserTestData.standardUser();

		//Mock the Dao Get User Call
		when(userDao.getUser(standardUser.getUsername())).thenReturn(standardUser);
		
		
		try{
			MvcResult result = this.mockMvc.perform(
					post("/v1/login/{username}",standardUser.getUsername())
							.param("password", UserTestData.standardPassword)
					.accept(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andReturn();
			
			UserModel loggedInUserModel = this.objectMapper.readValue(result.getResponse().getContentAsString(), UserModel.class);
			User loggedInUser = loggedInUserModel.getData();

			//Check to see that the User is correct
			assertEquals(standardUser.getUsername(), loggedInUser.getUsername());
			
			//Check to see that the Proper URI is in the Response
			String defaultLoginUri = result.getResponse().getHeader(LoginRestController.LOGIN_DEFAULT_URI_HEADER).toString();
			assertEquals(standardUser.getHomeUrl(), defaultLoginUri);
			
			//Ensure the User is in the Session
			User sessionUser = (User) result.getRequest().getSession().getAttribute("sessionUser"); //Because Common.SESSION_USER is not public
			assertEquals(standardUser.getUsername(), sessionUser.getUsername());
		
		
		}catch(Exception e){
			fail(e.getMessage());
		}
		
	}
	
}
