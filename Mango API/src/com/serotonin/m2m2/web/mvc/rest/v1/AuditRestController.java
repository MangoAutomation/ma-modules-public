/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.RQLToSQLParseException;
import com.serotonin.m2m2.db.dao.AuditEventDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.audit.AuditEventInstanceModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import net.jazdw.rql.parser.ASTNode;


/**
 * Access to Audit Tracking.  Currently View Only.
 * 
 * TODO Implement restoration, See Restorer and TemplateRestorer
 * 
 * @author Terry Packer
 */
@Api(value="Audit System", description="Restore/Read Configuration From History")
@RestController
@RequestMapping("/v1/audit")
public class AuditRestController extends MangoVoRestController<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao> {
	
	public AuditRestController() {
		super(AuditEventDao.instance);
	}

	@ApiOperation(
			value = "Query Audit Events",
			notes = "Admin access only",
			response=AuditEventInstanceModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<PageQueryStream<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao>> queryRQL(HttpServletRequest request) {
		
		RestProcessResult<PageQueryStream<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao>> result = new RestProcessResult<PageQueryStream<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		try{
    			if(!user.isAdmin()){
    				result.addRestMessage(getUnauthorizedMessage());
    				return result.createResponseEntity();
    			}else{
    				//Limit our results based on the fact that our permissions should be in the permissions strings
        			ASTNode root = this.parseRQLtoAST(request);
	    			return result.createResponseEntity(getPageStream(root));
    			}
    		}catch(UnsupportedEncodingException | RQLToSQLParseException e){
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}

	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractBasicVO)
	 */
	@Override
	public AuditEventInstanceModel createModel(AuditEventInstanceVO vo) {
		return new AuditEventInstanceModel(vo);
	}

}
