/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.log4jreset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.SystemActionDefinition;
import com.serotonin.m2m2.util.timeout.SystemActionTask;
import com.serotonin.timer.OneTimeTrigger;

/**
 * Test/Reset the Log4J configuration.  Acceptable Input actions as String:
 * 
 * RESET
 * TEST_DEBUG
 * TEST_INFO
 * TEST_WARN
 * TEST_ERROR
 * TEST_FATAL
 * 
 * @author Terry Packer
 */
public class Log4JResetActionDefinition extends SystemActionDefinition{

	private final String KEY = "log4JUtil";
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#getKey()
	 */
	@Override
	public String getKey() {
		return KEY;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#getWorkItem(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public SystemActionTask getTaskImpl(final JsonNode input) {
		return new Action(input.get("action").asText());
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#getPermissionTypeName()
	 */
	@Override
	protected String getPermissionTypeName() {
		return Log4JResetActionPermissionDefinition.PERMISSION;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#validate(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	protected void validate(JsonNode input) throws ValidationException {
		ProcessResult result = new ProcessResult();
		
		JsonNode node = input.get("action");
		if(node == null)
			result.addContextualMessage("action", "validate.required");
		else{
			switch(node.asText()){
			case "RESET":
			case "TEST_DEBUG":
			case "TEST_INFO":
			case "TEST_WARN":
			case "TEST_ERROR":
			case "TEST_FATAL":
				break;
			default:
				result.addContextualMessage("action", "validate.invalidValue");
			}
		}
		result.ensureValid();;
	}
	
	/**
	 * Class to allow purging data in ordered tasks with a queue 
	 * of up to 5 waiting purges
	 * 
	 * @author Terry Packer
	 */
	class Action extends SystemActionTask{
		
		private String action;
		
		public Action(String action){
			super(new OneTimeTrigger(0l), "Reset Log4J", "RESET_LOG4J", 5);
			this.action = action;
		}

		/* (non-Javadoc)
		 * @see com.serotonin.timer.Task#run(long)
		 */
		@Override
		public void runImpl(long runtime) {
			switch(action){
			case "RESET":
		        LOG.info("Reloading Log4J configuration");
		    	((LoggerContext)LogManager.getContext(false)).reconfigure();
		        LOG.info("Finished reloading Log4J configuration");
		        break;
			case "TEST_DEBUG":
		        LOG.debug("Log4JReset module test debug message");
		        break;
			case "TEST_INFO":
		        LOG.info("Log4JReset module test info message");
		        break;
			case "TEST_WARN":
		        LOG.warn("Log4JReset module test warn message");
		        break;	
			case "TEST_ERROR":
				LOG.error("Log4JReset module test error message");
				break;
			case "TEST_FATAL":
		        LOG.fatal("Log4JReset module test fatal message");
				break;
			}
		}
	}
}