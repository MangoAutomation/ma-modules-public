/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.test.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
public class UserTestData {

    //Freetext password for standard user
    public static String standardPassword = "standard";
    public static String adminPassword = "admin";

    /**
     * Create a standard user that has previously loggged In
     * 
     * @return
     */
    public static User standardUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("standard");
        //Store encrypted passwords
        user.setPassword(Common.encrypt(standardPassword));
        user.setEmail("email@address.com");
        user.setPhone("808-000-0000");
        user.setDisabled(false);
        user.setHomeUrl("/home.shtm");
        user.setLastLogin(new Date().getTime() - 10000);
        user.setReceiveAlarmEmails(1);
        user.setReceiveOwnAuditEvents(true);
        user.setTimezone("UTC");
        user.setMuted(false);
        user.setPermissions("myGroup");
        return user;
    }

    /**
     * Create a standard user that has previously loggged In
     * 
     * @return
     */
    public static User newStandardUser() {
        User user = new User();
        user.setId(2);
        user.setUsername("newStandard");
        //Store encrypted passwords
        user.setPassword(Common.encrypt(standardPassword));
        user.setEmail("email@address.com");
        user.setPhone("808-000-0000");
        user.setDisabled(false);
        user.setHomeUrl("/home.shtm");
        user.setLastLogin(0); //Never logged in before
        user.setReceiveAlarmEmails(1);
        user.setReceiveOwnAuditEvents(true);
        user.setTimezone("UTC");
        user.setMuted(false);
        user.setPermissions("myGroup");
        return user;
    }

    /**
     * Create a standard user that has previously loggged In
     * 
     * @return
     */
    public static User newDisabledAdminUser() {
        User user = new User();
        user.setId(3);
        user.setUsername("newDisabledAdmin");
        //Store encrypted passwords
        user.setPassword(Common.encrypt(adminPassword));
        user.setEmail("email@address.com");
        user.setPhone("808-000-0000");
        user.setDisabled(true);
        user.setHomeUrl("/home.shtm");
        user.setLastLogin(0); //Never logged in before
        user.setReceiveAlarmEmails(1);
        user.setReceiveOwnAuditEvents(true);
        user.setTimezone("UTC");
        user.setMuted(false);
        user.setPermissions("myGroup");
        return user;
    }

    /**
     * Create a standard user that has previously loggged In
     * 
     * @return
     */
    public static User newAdminUser() {
        User user = new User();
        user.setId(4);
        user.setUsername("newAdmin");
        //Store encrypted passwords
        user.setPassword(Common.encrypt(adminPassword));
        user.setEmail("email@address.com");
        user.setPhone("808-000-0000");
        user.setDisabled(false);
        user.setHomeUrl("/home.shtm");
        user.setLastLogin(0); //Never logged in before
        user.setReceiveAlarmEmails(1);
        user.setReceiveOwnAuditEvents(true);
        user.setTimezone("UTC");
        user.setMuted(false);
        user.setPermissions("myGroup");
        return user;
    }

    /**
     * Create a standard user that has previously loggged In
     * 
     * @return
     */
    public static User adminUser() {
        User user = new User();
        user.setId(4);
        user.setUsername("admin");
        //Store encrypted passwords
        user.setPassword(Common.encrypt(adminPassword));
        user.setEmail("email@address.com");
        user.setPhone("808-000-0000");

        user.setDisabled(false);
        user.setHomeUrl("/home.shtm");
        user.setLastLogin(new Date().getTime() - 1000);
        user.setReceiveAlarmEmails(1);
        user.setReceiveOwnAuditEvents(true);
        user.setTimezone("UTC");
        user.setMuted(false);
        user.setPermissions("myGroup");
        return user;
    }

    public static List<User> getAllUsers() {
        List<User> all = new ArrayList<>();
        all.add(standardUser());
        all.add(newStandardUser());
        all.add(newDisabledAdminUser());
        all.add(newAdminUser());

        return all;
    }
}
