/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

/**
 * Cache control for streaming cached values in Point Value Time requests
 * @author Terry Packer
 */
public enum PointValueTimeCacheControl {
    
    CACHE_ONLY, //Only use the cached values
    NONE, //Use no cache 
    BOTH //Use both cache and saved values

}
