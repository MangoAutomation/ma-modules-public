/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import com.serotonin.m2m2.db.dao.AbstractBasicDao;
import com.serotonin.m2m2.vo.AbstractBasicVO;
import com.serotonin.m2m2.web.mvc.rest.IMangoVoRestController;

/**
 * @author Terry Packer
 *
 */
public class FilteredVoStreamCallback<VO extends AbstractBasicVO, MODEL, DAO extends AbstractBasicDao<VO>> extends FilteredQueryStreamCallback<VO> {

	protected IMangoVoRestController<VO, MODEL, DAO> controller;
	
	/**
	 * 
	 * @param controller
	 */
	public FilteredVoStreamCallback(IMangoVoRestController<VO, MODEL, DAO> controller){
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
