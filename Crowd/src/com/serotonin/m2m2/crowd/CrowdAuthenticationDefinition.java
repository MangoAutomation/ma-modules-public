package com.serotonin.m2m2.crowd;

import org.springframework.security.authentication.AuthenticationProvider;

import com.serotonin.m2m2.module.AuthenticationDefinition;
import com.serotonin.m2m2.vo.User;

public class CrowdAuthenticationDefinition extends AuthenticationDefinition {
    
	private final CrowdAuthenticationProvider provider = new CrowdAuthenticationProvider();
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.AuthenticationDefinition#authenticationProvider()
	 */
	@Override
	public AuthenticationProvider authenticationProvider() {
		return provider;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.AuthenticationDefinition#logout(com.serotonin.m2m2.vo.User)
	 */
	@Override
	public void logout(User user) {
		
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.AuthenticationDefinition#postLogin(com.serotonin.m2m2.vo.User)
	 */
	@Override
	public void postLogin(User user) {
		// TODO Auto-generated method stub
		
	}
	
}
