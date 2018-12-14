/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfig;
import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfigDao;
import com.infiniteautomation.mango.rest.v2.exception.AlreadyExistsRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.vo.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * @author Terry Packer
 */
@Api(value="Virtual Serial Ports", description="Admin only endpoints to manage Virtual ports")
@RestController()
@RequestMapping("/virtual-serial-ports")
public class VirtualSerialPortRestV2Controller extends AbstractMangoRestV2Controller{

	@PreAuthorize("isAdmin()")
	@ApiOperation(
			value = "List all Virtual Serial Ports",
			notes = "Admin Only"
			)
	@RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<VirtualSerialPortConfig>> list(HttpServletRequest request) {
		return new ResponseEntity<>(VirtualSerialPortConfigDao.getInstance().getAll(), HttpStatus.OK);
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(
			value = "Get Virtual Serial Port by XID",
			notes = "Admin Only"
			)
	@RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public ResponseEntity<VirtualSerialPortConfig> get(
    		@ApiParam(value = "Valid Configuration XID", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		HttpServletRequest request) {
		
		VirtualSerialPortConfig config  = VirtualSerialPortConfigDao.getInstance().getByXid(xid);
		if(config == null)
			throw new NotFoundRestException();
		
		return new ResponseEntity<>(config, HttpStatus.OK);
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(
			value = "Create a virtual serial port",
			notes = "Cannot already exist, admin only"
			)
	@RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<VirtualSerialPortConfig> save(
    		@ApiParam(value = "Serial Port", required = true)
    		@RequestBody(required=true)  VirtualSerialPortConfig model,
    		@AuthenticationPrincipal User user,
    		UriComponentsBuilder builder, HttpServletRequest request) {
		
		//Check to see if it already exists
		if(!StringUtils.isEmpty(model.getXid())){
			VirtualSerialPortConfig existing = VirtualSerialPortConfigDao.getInstance().getByXid(model.getXid());
			if(existing != null){
				throw new AlreadyExistsRestException(model.getXid());
 			}
		}
		
		//Validate
		model.ensureValid();

		//Save it
		VirtualSerialPortConfigDao.getInstance().save(model);
		
        //Put a link to the updated data in the header?
    	URI location = builder.path("/virtual-serial-ports/{xid}").buildAndExpand(model.getXid()).toUri();
    	return getResourceCreated(model, location);
    }
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(
			value = "Update virtual serial port",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.PUT, value={"/{xid}"})
    public ResponseEntity<VirtualSerialPortConfig> update(
    		@ApiParam(value = "Valid virtual serial port id", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		@ApiParam(value = "Virtual Serial Port", required = true)
    		@RequestBody(required=true)  VirtualSerialPortConfig model,
    		@AuthenticationPrincipal User user,
    		UriComponentsBuilder builder, HttpServletRequest request) {

		//Check to see if it already exists
		VirtualSerialPortConfig existing = VirtualSerialPortConfigDao.getInstance().getByXid(model.getXid());
		if(existing == null)
			throw new NotFoundRestException();
		
		//Validate
		model.ensureValid();

		//Save it
		VirtualSerialPortConfigDao.getInstance().save(model);
		
        //Put a link to the updated data in the header
    	URI location = builder.path("/virtual-serial-ports/{xid}").buildAndExpand(model.getXid()).toUri();

    	return getResourceUpdated(model, location);
    }
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(
			value = "Delete virtual serial port",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.DELETE, value={"/{xid}"})
    public ResponseEntity<VirtualSerialPortConfig> delete(
       		@ApiParam(value = "Valid Virtual serial port XID", required = true, allowMultiple = false)
       	 	@PathVariable String xid,
    		@AuthenticationPrincipal User user,
    		UriComponentsBuilder builder, HttpServletRequest request) {

		//Check to see if it already exists
		VirtualSerialPortConfig existing = VirtualSerialPortConfigDao.getInstance().getByXid(xid);
		if(existing == null)
			throw new NotFoundRestException();
		
		//Ensure we set the xid
		existing.setXid(xid);
		
		//Save it
		VirtualSerialPortConfigDao.getInstance().remove(existing);

        return new ResponseEntity<>(existing, HttpStatus.OK);
    }
}
