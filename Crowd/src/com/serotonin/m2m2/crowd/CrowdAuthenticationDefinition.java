package com.serotonin.m2m2.crowd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;

import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.module.AuthenticationDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.form.LoginForm;
import com.serotonin.util.ValidationUtils;

public class CrowdAuthenticationDefinition extends AuthenticationDefinition {
    @Override
    public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response, User user) {
        if (CrowdUtils.isCrowdAuthenticated(user))
            // The user may not have been authenticated by Crowd, so only check with Crowd if it was.
            return CrowdUtils.isAuthenticated(request, response);
        return true;
    }

    @Override
    public User preLoginForm(HttpServletRequest request, HttpServletResponse response, LoginForm loginForm,
            BindException errors) {
    	if(loginForm == null)
    		return null;
    	
        String username = CrowdUtils.getCrowdUsername(request);

        if (username != null) {
            loginForm.setUsername(username);

            if (getModule().license() == null)
                ValidationUtils.reject(errors, "crowd.license");

            // The user is logged into Crowd. Make sure the username is valid in this instance.
            User user = new UserDao().getUser(username);
            if (user == null)
                ValidationUtils.rejectValue(errors, "username", "login.validation.noSuchUser");
            else {
                // Validate some stuff about the user.
                if (user.isDisabled())
                    ValidationUtils.reject(errors, "login.validation.accountDisabled");
                else {
                    if (CrowdUtils.isAuthenticated(request, response)) {
                        CrowdUtils.setCrowdAuthenticated(user);
                        return user;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response, User user, String password,
            BindException errors) {
        boolean authenticate = CrowdUtils.authenticate(request, response, user.getUsername(), password);
        if (authenticate)
            CrowdUtils.setCrowdAuthenticated(user);
        return authenticate;
    }

    @Override
    public void postLogin(User user) {
        // no op
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, User user) {
        CrowdUtils.logout(request, response);
    }
}
