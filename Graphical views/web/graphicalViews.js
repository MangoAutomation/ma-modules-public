/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
*/
//
// Anonymous views
mango.view.initAnonymousView = function(viewId) {
    mango.view.setPoint = mango.view.anon.setPoint;
    // Tell the long poll request that we're interested in anonymous view data, and not max alarm.
    mango.longPoll.pollRequest.maxAlarm = false;
    mango.longPoll.pollRequest.refId = viewId;
    mango.view.anon.viewId = viewId;
    mango.longPoll.addHandler("graphicalViewAnon", function(response) {
    	if (response.viewStates)
    		mango.view.setData(response.viewStates);
    });
};

mango.view.anon = {};
mango.view.anon.setPoint = function(pointId, viewComponentId, value) {
    show("c"+ viewComponentId +"Changing");
    mango.view.hideChange("c"+ viewComponentId +"Change");
    GraphicalViewDwr.setViewPointAnon(mango.view.anon.viewId, viewComponentId, value, function(viewComponentId) {
        hide("c"+ viewComponentId +"Changing");
        MiscDwr.notifyLongPoll(mango.longPoll.pollSessionId);
    });
};


//
// Normal views
mango.view.initNormalView = function() {
    mango.view.setPoint = mango.view.norm.setPoint;
    // Tell the long poll request that we're interested in view data.
    mango.longPoll.addHandler("graphicalView", function(response) {
    	if (response.viewStates)
    		mango.view.setData(response.viewStates);
    });
};

mango.view.norm = {};
mango.view.norm.setPoint = function(pointId, viewComponentId, value) {
    show("c"+ viewComponentId +"Changing");
    mango.view.hideChange("c"+ viewComponentId +"Change");
    GraphicalViewDwr.setViewPoint(viewComponentId, value, function(viewComponentId) {
        hide("c"+ viewComponentId +"Changing");
        MiscDwr.notifyLongPoll(mango.longPoll.pollSessionId);
    });
};


//
// View editing
mango.view.initEditView = function() {
    // Tell the long poll request that we're interested in view editing data.
    mango.view.setData = mango.view.edit.setData;
    mango.longPoll.addHandler("graphicalViewEdit", function(response) {
    	if (response.viewStates)
    		mango.view.setData(response.viewStates);
    });
};

mango.view.edit = {};
mango.view.edit.iconize = false;
mango.view.edit.setData = function(stateArr) {
    var state, node;
    for (var i=0; i<stateArr.length; i++) {
        state = stateArr[i];
        
        // Check that the point exists. Ignore if it doesn't.
        if (!$("c"+ state.id))
            continue;
            //throw "Can't find point view c"+ state.id;
        
        if (state.content != null) {
            if (!state.content)
                state.content = "<img src='images/logo_icon.gif'/>";
            
            if (mango.view.edit.iconize)
                $("c"+ state.id).savedState = state;
            else
                mango.view.setContent(state);
        }
        
        if (state.info != null) {
            node = $("c"+ state.id +"Info");
            if (node)
                node.innerHTML = state.info;
        }
        mango.view.setMessages(state);
    }
};
