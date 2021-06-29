/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.util;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.serotonin.m2m2.Common;

/**
 * Container to Manage Temporary Resources
 * 
 * ResourceIds are used via:
 * 
 * GET/PUT
 * url-prefix/resourceId
 * 
 * @author Terry Packer
 */
public class MangoRestTemporaryResourceContainer<T extends MangoRestTemporaryResource<?>> {

	//Map for recently run imports
	private final ConcurrentHashMap<String, T> resources;
	private final String resourcePrefix;
	
	public MangoRestTemporaryResourceContainer(String prefix){
		this.resources = new ConcurrentHashMap<>();
		this.resourcePrefix = prefix;
	}
	
	/**
	 * Get a resource if exists else throw exception
	 * @param id
	 * @return
	 * @throws NotFoundRestException
	 */
	public T get(String id) throws NotFoundRestException{
		T resource =  this.resources.get(id);
		if(resource == null)
			throw new NotFoundRestException();
		else
			return resource;
	}
	
	/**
	 * Add a Resource, with an expiration date
	 * @param id
	 * @param resource
	 */
	public void put(String id, T resource, Date expiration){
		this.resources.put(id, resource);
		resource.schedule(expiration);
	}
	
	/**
	 * Add a resource, no expiration
	 * @param id
	 * @param mangoRestTemporaryResource
	 */
	public void put(String id, T mangoRestTemporaryResource){
		this.resources.put(id, mangoRestTemporaryResource);
	}
	
	/**
	 * Remove a resource and cancel its timeout if there is one
	 * @param resourceId
	 * @return
	 */
	public T remove(String resourceId){
		T resource = this.resources.remove(resourceId);
		if(resource != null){
			resource.cancelTimeout();

		}
		return resource;
	}
	
	/**
	 * Generate a unique resource Id for this container
	 * @return
	 */
	public String generateResourceId(){
		String resourceId = Common.generateXid(resourcePrefix);
        while (!isResourceIdUnique(resourceId))
        	resourceId = Common.generateXid(resourcePrefix);
        return resourceId;
	}
	
	private boolean isResourceIdUnique(String resourceId){
	    return !this.resources.containsKey(resourceId);
	}
}
