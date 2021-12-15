/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Base Rest Controller for V2 of the REST api
 * 
 * @author Terry Packer
 */
public abstract class AbstractMangoRestController {

	/**
	 * For created resources
     */
	public static <N> ResponseEntity<N> getResourceCreated(N body, URI location) {
		return getResourceModified(body, location, HttpStatus.CREATED);
	}

	public static <N> ResponseEntity<N> getResourceUpdated(N body, URI location) {
		return getResourceModified(body, location, HttpStatus.OK);
	}

	/**
	 * To modify a resource with a Location header
     */
	protected static <N> ResponseEntity<N> getResourceModified(N body, URI location, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(location);
		if (body == null)
			return new ResponseEntity<N>(headers, status);
		else
			return new ResponseEntity<N>(body, headers, status);
	}
}
