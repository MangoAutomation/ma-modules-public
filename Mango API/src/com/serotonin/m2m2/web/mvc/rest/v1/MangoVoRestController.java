/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.infiniteautomation.mango.db.query.QueryComparison;
import com.infiniteautomation.mango.db.query.QueryModel;
import com.infiniteautomation.mango.db.query.TableModel;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.VoStreamCallback;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * @author Terry Packer
 *
 */
public abstract class MangoVoRestController<VO extends AbstractVO<VO>, MODEL> extends MangoRestController{

	protected AbstractDao<VO> dao;
	protected VoStreamCallback<VO, MODEL> callback;


	/**
	 * Construct a Controller using the default callback
	 * @param dao
	 */
	public MangoVoRestController(AbstractDao<VO> dao){
		this.dao = dao;
		this.callback = new VoStreamCallback<VO, MODEL>(this);
	}

	/**
	 * Construct a Controller
	 * @param dao
	 * @param callback
	 */
	public MangoVoRestController(AbstractDao<VO> dao, VoStreamCallback<VO, MODEL> callback){
		this.dao = dao;
		this.callback = callback;
	}
	
	/**
	 * Get the Query Stream for Streaming an array of data
	 * @param query
	 * @return
	 */
	protected QueryStream<VO, MODEL> getStream(QueryModel query){
		return this.getStream(query, this.callback);
	}
	
	/**
	 * Get the Query Stream for Streaming an array of data
	 * @param query
	 * @return
	 */
	protected QueryStream<VO, MODEL> getStream(QueryModel query, VoStreamCallback<VO, MODEL> callback){

		QueryStream<VO, MODEL> stream = new QueryStream<VO, MODEL>(dao, this, query, callback);
		//Ensure its ready
		stream.setupQuery();
		return stream;
	}
	
	/**
	 * Get a Stream that is more like a result set with a count
	 * @param query
	 * @return
	 */
	protected PageQueryStream<VO, MODEL> getPageStream(QueryModel query){
		return getPageStream(query, this.callback);
	}
	/**
	 * Get a Stream that is more like a result set with a count
	 * @param query
	 * @return
	 */
	protected PageQueryStream<VO, MODEL> getPageStream(QueryModel query, VoStreamCallback<VO, MODEL> callback){
		PageQueryStream<VO, MODEL> stream = new PageQueryStream<VO, MODEL>(dao, this, query, callback);
		//Ensure its ready
		stream.setupQuery();
		return stream;
	}

	@ApiOperation(
			value = "Get Explaination For Query",
			notes = "What is Query-able on this model"
			)
	@ApiResponses(value = { 
	@ApiResponse(code = 200, message = "Ok"),
	@ApiResponse(code = 403, message = "User does not have access")
	})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/explain-query")
    public ResponseEntity<TableModel> getTableModel(HttpServletRequest request) {
        
        RestProcessResult<TableModel> result = new RestProcessResult<TableModel>(HttpStatus.OK);
        
        this.checkUser(request, result);
        if(result.isOk()){
 	        result.addRestMessage(getSuccessMessage());
	        return result.createResponseEntity(this.getQueryAttributeModel());
        }
        
        return result.createResponseEntity();
    }
	
	/**
	 * Get the Table Model
	 * @return
	 */
	protected TableModel getQueryAttributeModel(){
		TableModel model = this.dao.getTableModel();
		this.fillTableModel(model);
		return model;
	}
	
	/**
	 * Fill out any additional Aliases for the Table that may exist
	 * in the Resulting Model for this class
	 * @param model
	 */
	protected abstract void fillTableModel(TableModel model);
	
	/**
	 * Map any Model members to VO Properties
	 * @param list
	 */
	public abstract void mapComparisons(List<QueryComparison> list);
	
	/**
	 * Create a Model
	 * @param vo
	 * @return
	 */
	public abstract MODEL createModel(VO vo);
	
}
