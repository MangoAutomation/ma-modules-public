/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointSummaryStreamCallback;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 * 
 */
@Api(value="Data Point Summary", description="Data Point Summaries")
@RestController(value="DataPointSummaryRestControllerV1")
@RequestMapping("/data-point-summaries")
public class DataPointSummaryRestController extends MangoVoRestController<DataPointVO, DataPointSummary, DataPointDao>{

	private static Log LOG = LogFactory.getLog(DataPointSummaryRestController.class);
	public DataPointSummaryRestController(){
		super(DataPointDao.getInstance());
	}
	
	@ApiOperation(
			value = "Query Data Points",
			notes = "",
			response=DataPointSummary.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.POST, value = "/query")
    public ResponseEntity<QueryDataPageStream<DataPointVO>> query(
    		@ApiParam(value="Query", required=true)
    		@RequestBody(required=true) ASTNode query, 
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<DataPointVO>> result = new RestProcessResult<QueryDataPageStream<DataPointVO>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		DataPointSummaryStreamCallback callback = new DataPointSummaryStreamCallback(this, user);
    		return result.createResponseEntity(getPageStream(query, callback));
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query Data Points",
			notes = "",
			response=DataPointSummary.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<DataPointVO>> queryRQL(HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<DataPointVO>> result = new RestProcessResult<QueryDataPageStream<DataPointVO>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		try{
	    		ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
	    		DataPointSummaryStreamCallback callback = new DataPointSummaryStreamCallback(this, user);
	    		
	    		return result.createResponseEntity(getPageStream(query,callback));
    		}catch(InvalidRQLRestException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractVO)
	 */
	@Override
	public DataPointSummary createModel(DataPointVO vo) {
		return new DataPointSummary(vo);
	}
	
}
