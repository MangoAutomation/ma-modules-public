/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

/**
 * Simple class to handle limits for point value query logic.
 *
 * Will not limit the results if limit is null or <= 0
 *
 * @author Terry Packer
 */
public class LimitCounter {

    private final int limit;
    private int count;
    private final boolean use;

    public LimitCounter(Integer limit){
        if((limit == null) || (limit <= 0)){
            this.use = false;
            this.limit = 0;
        }else{
            this.use = true;
            this.limit = limit;
        }
        this.count = 0;
    }

    /**
     * Is the result set to be limited now, i.e. don't return any more values
     * @return
     */
    public boolean limited(){
        if(!use)
            return false;
        else if(count >= limit)
            return true;
        else{
            count++;
            return false;
        }
    }

}
