package com.infiniteautomation.mangoApi.websocket;

import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.config.JsonConfigImportWebSocketHandler;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;

public class JsonConfigImportWebSocketDefinition extends WebSocketDefinition{
	
	public static final String TYPE_NAME = "JSON_IMPORT_HANDLER";

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getHandlerSingleton()
	 */
	@Override
	protected MangoWebSocketPublisher getHandler() {
		return new JsonConfigImportWebSocketHandler();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
	 */
	@Override
	public String getUrl() {
		return "/v1/websocket/json-import";
	}
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#perConnection()
	 */
	@Override
	public boolean perConnection() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

}
