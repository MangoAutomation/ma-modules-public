/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController;

/**
 * @author Terry Packer
 *
 */
public class VoJsonStreamCallback<VO extends AbstractVO<VO>, MODEL> extends AbstractJsonStreamCallback<VO> {

	private MangoVoRestController<VO, MODEL> controller;
	
	public VoJsonStreamCallback(MangoVoRestController<VO, MODEL> controller){
		this.controller = controller;
		
	}
	
	/**
	 * Do the work of writing the VO
	 * @param vo
	 * @throws IOException
	 */
	protected void write(VO vo) throws IOException{
		MODEL model = this.controller.createModel(vo);
		this.jgen.writeObject(model);
	}


}
