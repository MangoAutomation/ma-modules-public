/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.BaseRestTest;
import com.serotonin.m2m2.web.mvc.rest.test.data.DataSourceTestData;
import com.serotonin.m2m2.web.mvc.rest.test.data.UserTestData;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

/**
 * @See http://spring.io/guides/tutorials/rest/2/
 * @See http://spring.io/guides/tutorials/rest/4/
 * 
 *      on example tests
 * 
 * 
 * @author Terry Packer
 *
 */
public class PointValueFunctionalTests extends BaseRestTest {

    @InjectMocks
    protected PointValueRestController mockController;

    @Override
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.setupMvc(mockController);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGetAllAdmin() {

        List<User> users = new ArrayList<>();
        User adminUser = UserTestData.adminUser();
        users.add(adminUser);
        users.add(UserTestData.newAdminUser());
        users.add(UserTestData.standardUser());

        //Setup our Mock DS
        DataSourceVO ds = DataSourceTestData.mockDataSource();

        //Configure our REST get settings
        String xid = ds.getXid();
        //        Date to = new Date();
        //        Date from = new Date(to.getTime() - 1000 * 60 * 60);
        //        RollupEnum rollup = null;
        //        TimePeriodType timePeriodType = null;
        //        Integer timePeriods = null;

        //This will ensure that the getUsers() method returns 
        // the mock list of users
        when(userDao.getUsers()).thenReturn(users);

        //		//Mock up the permissions requests
        //		for(User user : users){
        //			for(Integer dsId : user.getDataSourcePermissions()){
        //				
        //				when(this.dataSourceDao.get(dsId)).thenReturn(ds);
        //				when(this.dataSourceDao.getByXid(ds.getXid())).thenReturn(ds);
        //				
        //			}
        //		}

        try {
            MvcResult result = this.mockMvc
                    .perform(
                            get("/v1/pointValues" + xid + ".json").sessionAttr("sessionUser", adminUser)
                                    .param("from", "2014-08-10T00:00:00.000-10:00")
                                    .param("to", "2014-08-10T00:00:00.000-10:00").accept(MediaType.APPLICATION_JSON))
                    .andDo(print()).andExpect(status().isOk()).andReturn();

            List<PointValueTimeModel> models = this.objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PointValueTimeModel.class));
            //Check the size
            assertEquals(users.size(), models.size());
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
        //Check the data

    }
}
