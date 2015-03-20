/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;

import com.infiniteautomation.mango.db.query.QueryComparison;
import com.infiniteautomation.mango.db.query.SortOption;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.ResourceCreatedMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;

/**
 * @author Terry Packer
 * 
 */
public abstract class MangoRestController{

	
	/**
	 * Check to see if a User is logged in
	 * 
	 * TODO could potentially use the @SessionAttributes({"user"}) annotation for this
	 * 
	 * @param request
	 * @param result
	 * @return User that is logged in, null if none are
	 */
	protected User checkUser(HttpServletRequest request, @SuppressWarnings("rawtypes") RestProcessResult  result) {
		User user = Common.getUser(request);
		if(user == null){
			result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("common.default", "User not logged in"));
		}
		
		return user;
	}
	
	/**
	 * Helper to easily change stock messages
	 * @return
	 */
	public RestMessage getUnauthorizedMessage(){
		return new RestMessage(HttpStatus.UNAUTHORIZED,new TranslatableMessage("common.default", "Unauthorized access"));
	}
	
	/**
	 * Helper to easily change stock messages for trying to create something that exists
	 * @return
	 */
	public RestMessage getAlreadyExistsMessage(){
		return new RestMessage(HttpStatus.CONFLICT, new TranslatableMessage("common.default", "Item already exists"));
	}
	
	/**
	 * Helper to easily change stock messages for failing to find something
	 * @return
	 */
	public RestMessage getDoesNotExistMessage(){
		return new RestMessage(HttpStatus.NOT_FOUND, new TranslatableMessage("common.default", "Item does not exist"));
	}
	/**
	 * Helper to easily change stock messages for successful operations
	 * @return
	 */
	public RestMessage getSuccessMessage(){
		return new RestMessage(HttpStatus.OK, new TranslatableMessage("common.default", "Success"));
	}
	
	/**
	 * Helper to create Validation Failed Rest Messages
	 * @return
	 */
	public RestMessage getValidationFailedError(){
		return new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "Validation error"));
	}
	
	public RestMessage getResourceCreatedMessage(URI location){
		return new ResourceCreatedMessage(HttpStatus.CREATED, new TranslatableMessage("common.default", "Created"), location);
	}
	
	public RestMessage getResourceUpdatedMessage(URI location){
		return new ResourceCreatedMessage(HttpStatus.OK, new TranslatableMessage("common.default", "Updated"), location);
	}

	public RestMessage getInternalServerErrorMessage(String content){
		return new RestMessage(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("common.default", content));
	}

	
	/**
	 * @param query
	 * @return
	 */
	protected QueryModel parseRQL(HttpServletRequest request) {
		String query = request.getQueryString();
		try {
			query = URLDecoder.decode(query, Common.UTF8);
			System.out.println("query: " + query);
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		String[] parts = query.split("&");
		QueryModel model = new QueryModel();
		List<QueryComparison> orComparisons = new ArrayList<QueryComparison>();
		List<QueryComparison> andComparisons = new ArrayList<QueryComparison>();
		List<SortOption> sorts = new ArrayList<SortOption>();
		for(String part : parts){
			System.out.println("part: " + part);
		
			if(part.startsWith("sort(")){
				//Sort starts with sort(
				Pattern pattern = Pattern.compile("sort\\(([\\+-]{1})(.*)\\)"); //TODO only works for single sort
				Matcher matcher = pattern.matcher(part);
				if(matcher.matches()){
					//int groupCount = matcher.groupCount();
					boolean desc = false;
					if(matcher.group(1).equals("-"))
						desc = true;
					SortOption sort = new SortOption(matcher.group(2), desc);
					sorts.add(sort);
				}
			}else if(part.startsWith("limit(")){
				Pattern pattern = Pattern.compile("limit\\((.*)\\)"); //TODO only works for single sort
				Matcher matcher = pattern.matcher(part);
				if(matcher.matches()){
					String limit = matcher.group(1);
					if((limit != null)&&(!limit.isEmpty())){
						model.setLimit(Integer.parseInt(limit));
					}
				}
			}else{
				//Must be Comparison
				String [] currentComparisons;
				String[] comparisonParts;
				
				if(part.contains("=gte=")){
					//Greater than
					comparisonParts = part.split("=gte=");
					QueryComparison comparison = new QueryComparison(comparisonParts[0], QueryComparison.GREATER_THAN_EQUAL_TO, comparisonParts[1]);
					andComparisons.add(comparison);
				}else if(part.contains("=gt=")){
					comparisonParts = part.split("=gt=");
					QueryComparison comparison = new QueryComparison(comparisonParts[0], QueryComparison.GREATER_THAN_EQUAL_TO, comparisonParts[1]);
					andComparisons.add(comparison);
				}else if(part.contains("=lte=")){
					comparisonParts = part.split("=lte=");
					QueryComparison comparison = new QueryComparison(comparisonParts[0], QueryComparison.LESS_THAN_EQUAL_TO, comparisonParts[1]);
					andComparisons.add(comparison);
				}else if(part.contains("=lt=")){
					comparisonParts = part.split("=lt=");
					QueryComparison comparison = new QueryComparison(comparisonParts[0], QueryComparison.LESS_THAN, comparisonParts[1]);
					andComparisons.add(comparison);
				}else{
					//Simple Equals
					if(part.contains("|")){
						//Going to use OR
						currentComparisons = part.split("\\|");
						for(String currentComparison : currentComparisons){
							comparisonParts = currentComparison.split("=");
							QueryComparison comparison = new QueryComparison(comparisonParts[0], QueryComparison.EQUAL_TO, comparisonParts[1]);
							orComparisons.add(comparison);
						}
					}else{
						comparisonParts = part.split("=");
						if(comparisonParts.length == 2){
							//Must be simple equals
							QueryComparison comparison = new QueryComparison(comparisonParts[0], QueryComparison.EQUAL_TO, comparisonParts[1]);
							andComparisons.add(comparison);
						}
					}
				}
			}
		}
		
		model.setSort(sorts);
		model.setOrComparisons(orComparisons);
		model.setAndComparisons(andComparisons);
		
		return model;
	}
	
}
