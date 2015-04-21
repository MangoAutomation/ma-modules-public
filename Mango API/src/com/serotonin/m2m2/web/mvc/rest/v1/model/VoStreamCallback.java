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
public class VoStreamCallback<VO extends AbstractVO<VO>, MODEL> extends QueryStreamCallback<VO> {

	protected MangoVoRestController<VO, MODEL> controller;
	
	/**
	 * 
	 * @param controller
	 */
	public VoStreamCallback(MangoVoRestController<VO, MODEL> controller){
		this.controller = controller;
	}
	
	/**
	 * Do the work of writing the VO
	 * @param vo
	 * @throws IOException
	 */
	@Override
	protected void writeJson(VO vo) throws IOException{
		MODEL model = this.controller.createModel(vo);
		this.jgen.writeObject(model);
	}
	@Override
	protected void writeCsv(VO vo) throws IOException{
		MODEL model = this.controller.createModel(vo);
		this.csvWriter.writeNext(model);
	}


}
